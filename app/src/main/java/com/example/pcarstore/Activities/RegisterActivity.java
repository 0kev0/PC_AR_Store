package com.example.pcarstore.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pcarstore.R;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar vistas
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // validacion tiempo real
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
        // Validar email solo al presionar el botón
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("El correo electrónico es requerido");
            return;
        } else if (!isValidEmail(email)) {
            etEmail.setError("Ingrese un correo electrónico válido");
            return;
        }

        // Si todo está correcto, proceder con el registro
        if (btnRegister.isEnabled()) {
            registerSuccess();
        }
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