package com.example.pcarstore.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.pcarstore.Activities.LoginActivity;
import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConfiguracionAdminFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;

    private TextView tvName, tvEmail, tvRole;
    private Button btnEditInfo, changePassword ,btnLogout;

    private String storedVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracion_admin, container, false);
        changePassword = view.findViewById(R.id.btnChangePassword);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Inicializar Firebase
        initFirebase();

        // Inicializar vistas
        initViews(view);

        // Configurar listeners
        setupListeners();

        // Cargar datos del admin
        loadAdminData();

        return view;
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tvAdminName);
        tvEmail = view.findViewById(R.id.tvAdminEmail);
        tvRole = view.findViewById(R.id.tvAdminRole);
        btnEditInfo = view.findViewById(R.id.btnEditInfo);

        // Verificar que las vistas no sean nulas
        if (tvName == null || tvEmail == null || tvRole == null ||
                btnEditInfo == null ) {
            Toast.makeText(getContext(), "Error al inicializar vistas", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        btnEditInfo.setOnClickListener(v -> showEditDialog());
        changePassword.setOnClickListener(v -> ChangePasswordAdmin(v));
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Editar Información");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_admin, null);
        builder.setView(dialogView);

        TextInputEditText etName = dialogView.findViewById(R.id.etEditName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEditEmail);
        TextInputEditText etRole = dialogView.findViewById(R.id.etEditRole);

        // Rellenar con datos actuales
        etName.setText(tvName.getText().toString());
        etEmail.setText(tvEmail.getText().toString());
        etRole.setText(tvRole.getText().toString());

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newRole = etRole.getText().toString().trim();

            if (validateInputs(newName, newEmail, newRole)) {
                updateAdminInfo(newName, newEmail, newRole);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadAdminData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());

            mDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User admin = snapshot.getValue(User.class);
                        if (admin != null) {
                            tvName.setText(admin.getName());
                            tvRole.setText(admin.getRole());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showError("Error al cargar datos: " + error.getMessage());
                }
            });
        }
    }

    private boolean validateInputs(String name, String email, String role) {
        if (name.isEmpty()) {
            showError("El nombre no puede estar vacío");
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Ingrese un email válido");
            return false;
        }

        if (role.isEmpty()) {
            showError("El rol no puede estar vacío");
            return false;
        }

        return true;
    }

    private void updateAdminInfo(String name, String email, String role) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("role", role);

        mDatabase.child(currentUserId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && !user.getEmail().equals(email)) {
                        updateUserEmail(user, email);
                    } else {
                        showSuccess("Información actualizada");
                        loadAdminData();
                    }
                })
                .addOnFailureListener(e -> showError("Error al actualizar: " + e.getMessage()));
    }

    private void updateUserEmail(FirebaseUser user, String newEmail) {
        user.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSuccess("Información actualizada");
                        loadAdminData();
                    } else {
                        showError("Error al actualizar email: " + task.getException().getMessage());
                    }
                });
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void ChangePasswordAdmin(View view) {
        // Crear diálogo para solicitar correo electrónico
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Restablecer Contraseña");
        builder.setMessage("Ingrese su correo electrónico para recibir el enlace de restablecimiento");

        // Configurar campo de entrada
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("correo@ejemplo.com");
        builder.setView(input);

        // Configurar botones
        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(getContext(), "Ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        // Mostrar diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void sendPasswordResetEmail(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Correo de restablecimiento enviado a " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            Toast.makeText(getContext(),
                                    "No existe una cuenta con este correo",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(),
                                    "Error al enviar correo: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}