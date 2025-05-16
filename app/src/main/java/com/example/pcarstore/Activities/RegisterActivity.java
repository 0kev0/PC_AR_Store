package com.example.pcarstore.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.example.pcarstore.Services.ValidationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicialización de Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicialización de vistas
        initViews();
        setupTextWatchers();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void setupTextWatchers() {
        etFullName.addTextChangedListener(createTextWatcher(() ->
                validateFullName(etFullName.getText().toString())));

        etPassword.addTextChangedListener(createTextWatcher(() -> {
            validatePassword(etPassword.getText().toString());
            validateConfirmPassword();
        }));

        etConfirmPassword.addTextChangedListener(createTextWatcher(() ->
                validateConfirmPassword()));
    }

    private TextWatcher createTextWatcher(Runnable validationAction) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validationAction.run();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private void validateFullName(String fullName) {
        if (!ValidationService.isValidName(fullName)) {
            etFullName.setError("Nombre completo inválido (mín. 3 caracteres, sin números)");
        } else {
            etFullName.setError(null);
        }
        updateRegisterButtonState();
    }

    private void validatePassword(String password) {
        if (!ValidationService.isPasswordSecure(password)) {
            etPassword.setError("La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas y números");
        } else {
            etPassword.setError(null);
        }
    }

    private void validateConfirmPassword() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!ValidationService.doPasswordsMatch(password, confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
        } else {
            etConfirmPassword.setError(null);
        }
        updateRegisterButtonState();
    }

    private void updateRegisterButtonState() {
        boolean isValid = etFullName.getError() == null &&
                etPassword.getError() == null &&
                etConfirmPassword.getError() == null &&
                !etFullName.getText().toString().isEmpty() &&
                !etEmail.getText().toString().isEmpty() &&
                !etPassword.getText().toString().isEmpty() &&
                !etConfirmPassword.getText().toString().isEmpty();

        btnRegister.setEnabled(isValid);
        btnRegister.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void validateAndRegister() {
        String email = etEmail.getText().toString().trim();

        if (!ValidationService.isValidEmail(email)) {
            etEmail.setError("Ingrese un correo electrónico válido");
            return;
        }

        registerUser(
                email,
                etPassword.getText().toString().trim(),
                etFullName.getText().toString().trim()
        );
    }

    private void registerUser(String email, String password, String fullName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        handleRegistrationSuccess(email, fullName);
                    } else {
                        Toast.makeText(this,
                                "Error en el registro: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleRegistrationSuccess(String email, String fullName) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(profileTask -> {
                    if (profileTask.isSuccessful()) {
                        saveUserToDatabase(email, fullName, firebaseUser.getUid());
                    } else {
                        Toast.makeText(this,
                                "Error al actualizar perfil",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String email, String fullName, String userId) {
        User user = new User.Builder(email)
                .setName(fullName)
                .setRole("client")
                .setSaldo(0.0)
                .setMembresiaPrime(false)
                .build();

        mDatabase.child("users").child(userId)
                .setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error al guardar datos adicionales",
                            Toast.LENGTH_SHORT).show();
                });
    }

    public void goToLogin(View view) {
        finish();
    }
}