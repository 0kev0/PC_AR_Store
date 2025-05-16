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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Validación en tiempo real
        setupTextWatchers();

        // Configurar el botón de registro
        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void setupTextWatchers() {
        // Validación para el nombre completo
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFullName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Validación para la contraseña
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
                validateConfirmPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Validación para la confirmación de contraseña
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validateFullName(String fullName) {
        if (fullName.isEmpty()) {
            etFullName.setError("El nombre completo es requerido");
        } else if (containsNumbers(fullName)) {
            etFullName.setError("El nombre no debe contener números");
        } else {
            etFullName.setError(null);
        }
        updateRegisterButtonState();
    }

    private void validatePassword(String password) {
        if (password.isEmpty()) {
            etPassword.setError("La contraseña es requerida");
        } else if (!isPasswordSecure(password)) {
            etPassword.setError("La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas y números");
        } else {
            etPassword.setError(null);
        }
        updateRegisterButtonState();
    }

    private void validateConfirmPassword() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!confirmPassword.equals(password)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
        } else {
            etConfirmPassword.setError(null);
        }
        updateRegisterButtonState();
    }

    private void updateRegisterButtonState() {
        boolean fieldsNotEmpty = !etFullName.getText().toString().isEmpty() &&
                !etEmail.getText().toString().isEmpty() &&
                !etPassword.getText().toString().isEmpty() &&
                !etConfirmPassword.getText().toString().isEmpty();

        boolean noErrors = etFullName.getError() == null &&
                etPassword.getError() == null &&
                etConfirmPassword.getError() == null;

        btnRegister.setEnabled(fieldsNotEmpty && noErrors);
        btnRegister.setAlpha(btnRegister.isEnabled() ? 1.0f : 0.5f);
    }

    private void validateAndRegister() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();

        // Validar email
        if (email.isEmpty()) {
            etEmail.setError("El correo electrónico es requerido");
            return;
        } else if (!isValidEmail(email)) {
            etEmail.setError("Ingrese un correo electrónico válido");
            return;
        }

        // Validar contraseña
        if (password.isEmpty()) {
            etPassword.setError("La contraseña es requerida");
            return;
        } else if (!isPasswordSecure(password)) {
            etPassword.setError("La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas y números");
            return;
        }

        // Validar confirmación de contraseña
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (!confirmPassword.equals(password)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        registerUser(email, password, fullName);
    }

    private void registerUser(String email, String password, String fullName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Actualizar nombre en Firebase Auth
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Crear objeto User con Builder
                                            User user = new User.Builder(email)
                                                    .setName(fullName)
                                                    .setRole("client")
                                                    .setSaldo(0.0)
                                                    .setMembresiaPrime(false)
                                                    .build();

                                            // Guardar en Realtime Database
                                            mDatabase.child("users").child(firebaseUser.getUid())
                                                    .setValue(user)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(RegisterActivity.this,
                                                                "¡Registro exitoso!", Toast.LENGTH_LONG).show();
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(RegisterActivity.this,
                                                                "Error al guardar datos adicionales", Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Error al actualizar perfil", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Error en el registro: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Métodos auxiliares
    private boolean containsNumbers(String text) {
        return text.matches(".*\\d.*");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private boolean isPasswordSecure(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        return pattern.matcher(password).matches();
    }

    private void registerSuccess() {
        Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_LONG).show();
    }

    public void goToLogin(View view) {
        finish();
    }
}