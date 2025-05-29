package com.example.pcarstore.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.pcarstore.Fragments.AdminFragment;
import com.example.pcarstore.Fragments.BalanceAdminFragment;
import com.example.pcarstore.Fragments.SalesRecordsFragment;
import com.example.pcarstore.R;
import com.example.pcarstore.helpers.FingerprintAuthListener;
import com.example.pcarstore.helpers.FingerprintHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AdminActivity extends AppCompatActivity implements FingerprintAuthListener {

    private static final String TAG = "AdminActivity";
    private static final String KEY_NAME = "admin_config_key";
    private static final long AUTH_VALID_DURATION = 30000;
    private FingerprintManager fingerprintManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private Fragment currentFragment;
    private BottomNavigationView bottomNavigationView;
    private long lastAuthTime = 0;
    private boolean isAuthInProgress = false;
    private MenuItem previousSelectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initializeFingerprintManager();
        setupUI();
        loadInitialFragment();
    }

    private void initializeFingerprintManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = getSystemService(FingerprintManager.class);
        }
    }

    private void setupUI() {
        setupToolbar();
        setupBottomNavigation();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Administración");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.admin_bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(this::handleNavigationItemSelected);
        previousSelectedItem = bottomNavigationView.getMenu().getItem(0); // Inicial con el primer ítem
    }

    private boolean handleNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == previousSelectedItem.getItemId()) {
            return true;
        }

        int itemId = menuItem.getItemId();

        if (itemId == R.id.nav_dashboard) {
            currentFragment = new AdminFragment();
        } else if (itemId == R.id.nav_ventas) {
            currentFragment = new SalesRecordsFragment();
        } else if (itemId == R.id.nav_balance) {
            if (isRecentlyAuthenticated()) {
                currentFragment = new BalanceAdminFragment();
            } else if (isFingerprintAuthAvailable() && !isAuthInProgress) {
                showFingerprintAuthDialog(menuItem);
                return false; // No confirmar la selección aún
            } else {
                showFingerprintError();
                return false;
            }
        }

        if (currentFragment != null) {
            loadFragment(currentFragment);
            previousSelectedItem = menuItem;
            return true;
        }
        return false;
    }

    private void showFingerprintAuthDialog(MenuItem targetItem) {
        new AlertDialog.Builder(this)
                .setTitle("Autenticación Requerida")
                .setMessage("Por favor autentícate con tu huella digital para acceder a esta sección")
                .setPositiveButton("Autenticar", (dialog, which) -> {
                    isAuthInProgress = true;
                    initFingerprint(targetItem);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // Mantener la selección anterior
                    bottomNavigationView.setSelectedItemId(previousSelectedItem.getItemId());
                })
                .setOnCancelListener(dialog -> {
                    // Mantener la selección anterior si se cancela
                    bottomNavigationView.setSelectedItemId(previousSelectedItem.getItemId());
                })
                .show();
    }

    private void initFingerprint(MenuItem targetItem) {
        try {
            if (generateKey() && cipherInit()) {
                FingerprintManager.CryptoObject cryptoObject =
                        new FingerprintManager.CryptoObject(cipher);

                FingerprintHelper helper = new FingerprintHelper(this);
                helper.setTargetMenuItem(targetItem);
                helper.startAuth(fingerprintManager, cryptoObject);
            } else {
                isAuthInProgress = false;
                Toast.makeText(this,
                        "Error al configurar autenticación por huella",
                        Toast.LENGTH_SHORT).show();
                bottomNavigationView.setSelectedItemId(previousSelectedItem.getItemId());
            }
        } catch (Exception e) {
            isAuthInProgress = false;
            Log.e(TAG, "Fingerprint init error: " + e.getMessage());
            Toast.makeText(this,
                    "Error en autenticación por huella",
                    Toast.LENGTH_SHORT).show();
            bottomNavigationView.setSelectedItemId(previousSelectedItem.getItemId());
        }
    }

    private boolean isRecentlyAuthenticated() {
        return System.currentTimeMillis() - lastAuthTime < AUTH_VALID_DURATION;
    }

    private void loadFragment(Fragment fragment) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
            transaction.replace(R.id.fragmentContainerView, fragment);
            transaction.commit();
            currentFragment = fragment;
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment: " + e.getMessage());
            Toast.makeText(this, "Error al cargar la pantalla", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInitialFragment() {
        loadFragment(new AdminFragment());
    }

    private boolean isFingerprintAuthAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return fingerprintManager != null
                    && fingerprintManager.isHardwareDetected()
                    && fingerprintManager.hasEnrolledFingerprints()
                    && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void showFingerprintError() {
        Toast.makeText(this,
                "Autenticación por huella no disponible. Configure huellas en su dispositivo.",
                Toast.LENGTH_LONG).show();
    }

    private boolean generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            keyGenerator.generateKey();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Key generation failed: " + e.getMessage());
            return false;
        }
    }

    private boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" +
                            KeyProperties.BLOCK_MODE_CBC + "/" +
                            KeyProperties.ENCRYPTION_PADDING_PKCS7);

            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Cipher initialization failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onFingerprintAuthSuccess() {
        runOnUiThread(() -> {
            lastAuthTime = System.currentTimeMillis();
            isAuthInProgress = false;

            Toast.makeText(this, "Autenticación exitosa", Toast.LENGTH_LONG).show();

            currentFragment = new BalanceAdminFragment();
            loadFragment(currentFragment);

            bottomNavigationView.setSelectedItemId(R.id.nav_balance);
            previousSelectedItem = bottomNavigationView.getMenu().findItem(R.id.nav_balance);
        });
    }

    @Override
    public void onFingerprintAuthError(String error) {
        runOnUiThread(() -> {
            isAuthInProgress = false;
            Toast.makeText(this, "Error de autenticación: " + error, Toast.LENGTH_SHORT).show();

            bottomNavigationView.setSelectedItemId(previousSelectedItem.getItemId());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar recursos
        fingerprintManager = null;
        keyStore = null;
        keyGenerator = null;
        cipher = null;
    }
}