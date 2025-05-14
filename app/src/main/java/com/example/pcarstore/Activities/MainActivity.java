package com.example.pcarstore.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.example.pcarstore.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Button catalogo, loginGoogle, test;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private static final String TAG = "MainActivity";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private int signInAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Verificar Google Play Services con Toast
        checkGooglePlayServices();

        mAuth = FirebaseAuth.getInstance();
        catalogo = findViewById(R.id.ContinueWithoutAcount);
        loginGoogle = findViewById(R.id.LoginGoogle);

        credentialManager = CredentialManager.create(this);

        // Configurar listeners
        loginGoogle.setOnClickListener(v -> registerGoogle());
    }

    private void checkGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            // Mostrar diálogo de error estándar
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000).show();
            }

            // Mostrar Toast adicional con mensaje descriptivo
            String errorMessage;
            switch (resultCode) {
                case ConnectionResult.SERVICE_MISSING:
                    errorMessage = "Google Play Services no está instalado";
                    break;
                case ConnectionResult.SERVICE_UPDATING:
                    errorMessage = "Google Play Services se está actualizando";
                    break;
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                    errorMessage = "Se requiere actualizar Google Play Services";
                    break;
                case ConnectionResult.SERVICE_DISABLED:
                    errorMessage = "Google Play Services está deshabilitado";
                    break;
                case ConnectionResult.SERVICE_INVALID:
                    errorMessage = "Versión no válida de Google Play Services";
                    break;
                default:
                    errorMessage = "Error con Google Play Services (Código: " + resultCode + ")";
            }

            Toast.makeText(this,
                    "Error: " + errorMessage + ". La aplicación puede no funcionar correctamente",
                    Toast.LENGTH_LONG).show();

            Log.e(TAG, "Google Play Services Error: " + errorMessage);
        }
    }

    private void registerGoogle() {
        // Reset sign-in attempts if it's been a while since last attempt
        if (signInAttempts >= MAX_RETRY_ATTEMPTS) {
            Toast.makeText(this,
                    "Demasiados intentos de inicio de sesión. Intente más tarde o use otro método de acceso.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        signInAttempts++;

        try {
            GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(true)
                    .setNonce(null)
                    .build();

            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(getGoogleIdOption)
                    .build();

            credentialManager.getCredentialAsync(
                    this,
                    request,
                    new CancellationSignal(),
                    Executors.newSingleThreadExecutor(),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse getCredentialResponse) {
                            // Reset attempts counter on success
                            signInAttempts = 0;
                            handleSignIn(getCredentialResponse.getCredential());
                        }

                        @Override
                        public void onError(@NonNull GetCredentialException e) {
                            Log.e(TAG, "Google Sign-In Error", e);

                            // Handle specific error types
                            runOnUiThread(() -> {
                                if (e instanceof NoCredentialException) {
                                    // User disabled feature or no accounts available
                                    if (e.getMessage() != null && e.getMessage().contains("User disabled the feature")) {
                                        showSignInAlternatives();
                                    } else {
                                        Toast.makeText(MainActivity.this,
                                                "No se encontraron cuentas disponibles. Por favor intente con otro método.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                } else if (e instanceof GetCredentialCancellationException) {
                                    // User cancelled the flow - just log, no need for error message
                                    Log.d(TAG, "User cancelled sign-in");
                                } else {
                                    // Other errors
                                    Toast.makeText(MainActivity.this,
                                            "Error de inicio de sesión: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Google Sign-In Initialization Error", e);
            Toast.makeText(this, "Error al inicializar Google Sign-In", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSignInAlternatives() {
        // Show a more helpful message guiding the user
        Toast.makeText(MainActivity.this,
                "La función de inicio rápido está desactivada. Intente habilitar Google One Tap en la configuración de su cuenta Google o use la opción 'Continuar sin cuenta'.",
                Toast.LENGTH_LONG).show();

        // Highlight the "Continue without account" option
        if (catalogo != null) {
            catalogo.setAlpha(1.0f);
        }
    }

    private void handleSignIn(Credential credential) {
        if (credential instanceof CustomCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(
                    ((CustomCredential) credential).getType())) {
                GoogleIdTokenCredential googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());
                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
            }
        } else {
            Log.e(TAG, "Credencial no esperada: " + credential.getClass().getName());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Usuario registrado con éxito");
                        startActivity(new Intent(MainActivity.this, CatalogoActivity.class));
                        finish();
                    } else {
                        Log.w(TAG, "Error de registro", task.getException());
                        Toast.makeText(MainActivity.this,
                                "Error en autenticación con Firebase",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void VerCatologo(View view) {
        startActivity(new Intent(this, CatalogoActivity.class));
    }

    public void ARtest(View view) {
        startActivity(new Intent(this, AR_test.class));
    }
}