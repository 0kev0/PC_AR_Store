package com.example.pcarstore.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.example.pcarstore.Services.DatabaseSeederService;
import com.example.pcarstore.helpers.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private Button loginGoogle;
    private Button test;
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private ProgressDialog progressDialog;

    private CredentialManager credentialManager;
    private SessionManager sessionManager;
    private DatabaseReference mDatabase;
    private static final int RC_SIGN_IN = 9001;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseDatabase.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseSeederService databaseSeederService = new DatabaseSeederService();
        databaseSeederService.initializeDatabase();

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        sessionManager = new SessionManager(this);
        checkCurrentSession();

        Button catalogo = findViewById(R.id.ContinueWithoutAcount);
        catalogo.setOnClickListener(v -> VerCatologo(v));

        Button registerGoogle = findViewById(R.id.RegisterGoogle);
        credentialManager = CredentialManager.create(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.LoginGoogle).setOnClickListener(v -> loginUser());

        registerGoogle.setOnClickListener(v -> registerGoogle());

        isFingerprintAuthAvailable();
    }

    private void verifyUserRole(FirebaseUser user) {
        mDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User dbUser = snapshot.getValue(User.class);
                if (dbUser != null && dbUser.getRole() != null) {
                    sessionManager.createSession(user.getUid(), user.getEmail(), dbUser.getRole());
                    redirectBasedOnRole(dbUser.getRole());
                } else {
                    Toast.makeText(LoginActivity.this, "Error: Rol no definido", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error al verificar datos", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        });
    }

    private void checkCurrentSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                    } else {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.getRole() != null) {
                            String storedRole = sessionManager.getUserRole();
                            if (!user.getRole().equals(storedRole)) {
                                sessionManager.createSession(
                                        currentUser.getUid(),
                                        currentUser.getEmail(),
                                        user.getRole()
                                );
                            }
                            redirectBasedOnRole(user.getRole());
                        } else {
                            handleInvalidRole();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    sessionManager.logoutUser();
                }
            });
        } else {
            sessionManager.logoutUser();
        }
    }

    private void redirectBasedOnRole(String role) {
        Class<?> destinationActivity;

        switch (role.toLowerCase()) {
            case "admin":
                destinationActivity = AdminActivity.class;
                break;
            case "client":
                destinationActivity = InicioActivity.class;
                break;
            case "user":
                destinationActivity = InicioActivity.class;
                break;
            default:
                destinationActivity = LoginActivity.class;
                sessionManager.logoutUser();
        }

        startActivity(new Intent(this, destinationActivity));
        finish();
    }


    public void VerCatologo(View view) {
        startActivity(new Intent(this, InicioActivity.class));
    }


    public void ARtest(View view) {
        startActivity(new Intent(this, AR_test.class));
    }

    private void registerGoogle() {
        GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
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
                        handleSignIn(getCredentialResponse.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Error en login con Google", e);
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this,
                                        "Error en autenticación: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
                    }
                }
        );
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
                        Log.d(TAG, "Usuario autenticado con éxito");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserDataAndRedirect(user);
                        }
                    } else {
                        handleGoogleAuthError(task.getException());
                    }
                });
    }

    private void handleGoogleAuthError(Exception exception) {
        Log.w(TAG, "Error en autenticación Google", exception);
        String errorMessage = "Error en autenticación";

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            errorMessage = "Error Google: " + apiException.getStatusCode();
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void saveUserDataAndRedirect(FirebaseUser user) {
        showProgressDialog("Configurando tu cuenta...");

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("displayName", user.getDisplayName());
        userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
        userData.put("provider", "google");
        userData.put("lastLogin", ServerValue.TIMESTAMP);

        mDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    userData.put("createdAt", ServerValue.TIMESTAMP);
                    userData.put("role", "client");
                    userData.put("membresiaPrime", false);
                }

                mDatabase.child(user.getUid()).updateChildren(userData)
                        .addOnSuccessListener(aVoid -> {
                            dismissProgressDialog();
                            Log.d(TAG, "Datos guardados correctamente");
                            verifyUserRole(user);
                        })
                        .addOnFailureListener(e -> {
                            dismissProgressDialog();
                            Log.e(TAG, "Error al guardar datos", e);
                            Toast.makeText(LoginActivity.this,
                                    "Error al guardar datos de usuario",
                                    Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissProgressDialog();
                Log.e(TAG, "Error en base de datos", error.toException());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        showProgressDialog("Verificando credenciales...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (!user.isEmailVerified()) {
                                dismissProgressDialog();
                                showEmailNotVerifiedDialog(user);
                                mAuth.signOut();
                            } else {
                                verifyUserRole(user.getUid());
                            }
                        } else {
                            dismissProgressDialog();
                            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    private void showEmailNotVerifiedDialog(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Correo no verificado");
        builder.setMessage("Debes verificar tu dirección de correo electrónico antes de poder iniciar sesión. ¿Deseas que te enviemos otro correo de verificación?");

        builder.setPositiveButton("Reenviar verificación", (dialog, which) -> {
            sendEmailVerification(user);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void sendEmailVerification(FirebaseUser user) {
        showProgressDialog("Enviando correo de verificación...");

        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    dismissProgressDialog();

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Correo de verificación enviado a " + user.getEmail(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Error al enviar correo de verificación: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Ingrese su correo electrónico");
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Ingrese su contraseña");
            return false;
        }
        return true;
    }

    private void verifyUserRole(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dismissProgressDialog();

                if (!snapshot.exists()) {
                    registerNewUser(userId);
                    return;
                }

                try {
                    String role = snapshot.child("role").getValue(String.class);
                    if (role != null && !role.isEmpty()) {
                        navigateToRoleScreen(role);
                    } else {
                        handleInvalidRole();
                    }
                } catch (Exception e) {
                    Log.e("ROLE_ERROR", "Error al leer rol", e);
                    handleInvalidRole();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissProgressDialog();
                Log.e("DB_ERROR", "Error en base de datos: " + error.getMessage());
                Toast.makeText(LoginActivity.this,
                        "Error al verificar información del usuario",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerNewUser(String userId) {
        showProgressDialog("Registrando nuevo usuario...");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            dismissProgressDialog();
            Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", currentUser.getEmail());
        userData.put("role", "client");
        userData.put("createdAt", ServerValue.TIMESTAMP);

        FirebaseDatabase.getInstance().getReference("users").child(userId)
                .setValue(userData)
                .addOnCompleteListener(task -> {
                    dismissProgressDialog();
                    if (task.isSuccessful()) {
                        navigateToRoleScreen("client");
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Error al registrar usuario",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToRoleScreen(String role) {
        Log.d("NAVIGATION", "Rol detectado: " + role);
        if (role.equals("admin")) {
            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
        } else if (role.equals("client")) {
            startActivity(new Intent(LoginActivity.this, InicioActivity.class));
        }
    }

    private void handleInvalidRole() {
        Toast.makeText(this,
                "Rol de usuario no válido. Contacte al administrador" ,
                Toast.LENGTH_LONG).show();
        mAuth.signOut();
    }

    private void handleLoginError(Exception exception) {
        dismissProgressDialog();
        String errorMessage = "Error de autenticación";

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "Usuario no registrado";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Correo o contraseña incorrectos";
        } else {
            Log.e("LOGIN_ERROR", "Error en login", exception);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void ChangePassword(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Restablecer Contraseña");
        builder.setMessage("Ingrese su correo electrónico para recibir el enlace de restablecimiento");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("correo@ejemplo.com");
        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(this, "Ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendPasswordResetEmail(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Correo de restablecimiento enviado a " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            Toast.makeText(this,
                                    "No existe una cuenta con este correo",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this,
                                    "Error al enviar correo: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isFingerprintAuthAvailable() {
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                return false;
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                return false;
            }
        }

        return true;
    }

}
