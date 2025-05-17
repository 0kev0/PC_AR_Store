package com.example.pcarstore.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import androidx.credentials.exceptions.GetCredentialException;

import com.example.pcarstore.R;
import com.example.pcarstore.Services.DatabaseSeederService;
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
    private Button catalogo, loginGoogle, RegisterGoogle, test;
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private ProgressDialog progressDialog;

    private CredentialManager credentialManager;
    private GoogleSignInClient mGoogleSignInClient;
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

        // Initialize the DatabaseService
        DatabaseSeederService dbService = new DatabaseSeederService();

        // Initialize the database with sample data
        dbService.initializeDatabase();

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, InicioActivity.class));
            finish();
            return;
        }

        catalogo = findViewById(R.id.ContinueWithoutAcount);
        catalogo.setOnClickListener(v -> VerCatologo(v));


        test = findViewById(R.id.test);
        test.setOnClickListener(v -> ARtest(v));

        RegisterGoogle = findViewById(R.id.RegisterGoogle);
        credentialManager = CredentialManager.create(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.LoginGoogle).setOnClickListener(v -> loginUser());

        RegisterGoogle.setOnClickListener(v -> registerGoogle());
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
                        Log.d(TAG, "Usuario registrado con éxito");
                        startActivity(new Intent(LoginActivity.this, InicioActivity.class));
                        finish();
                    } else if (task == null) {
                        Log.w(TAG, "Error de autenticación", task.getException());
                        Toast.makeText(LoginActivity.this,
                                "Error en autenticación con Firebase",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Error de registro", task.getException());
                        Toast.makeText(LoginActivity.this,
                                "Error en autenticación con Firebase",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Manejo del resultado si usas startActivityForResult
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
                            verifyUserRole(user.getUid());
                        } else {
                            dismissProgressDialog();
                            Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        handleLoginError(task.getException());
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
        userData.put("role", "client"); // Rol por defecto
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
}
