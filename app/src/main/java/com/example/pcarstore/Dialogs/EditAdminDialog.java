package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.example.pcarstore.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import java.util.HashMap;
import java.util.Map;

public class EditAdminDialog {

    public interface OnAdminInfoUpdatedListener {
        void onAdminInfoUpdated();
    }

    public static void show(Context context,
                            String currentName,
                            String currentEmail,
                            String currentRole,
                            DatabaseReference databaseRef,
                            String currentUserId,
                            OnAdminInfoUpdatedListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar Información");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_admin, null);
        builder.setView(dialogView);

        TextInputEditText etName = dialogView.findViewById(R.id.etEditName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEditEmail);
        TextInputEditText etRole = dialogView.findViewById(R.id.etEditRole);

        // Rellenar con datos actuales
        etName.setText(currentName);
        etEmail.setText(currentEmail);
        etRole.setText(currentRole);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newRole = etRole.getText().toString().trim();

            if (validateInputs(context, newName, newEmail, newRole)) {
                updateAdminInfo(context, newName, newEmail, newRole, databaseRef, currentUserId, listener);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static boolean validateInputs(Context context, String name, String email, String role) {
        if (name.isEmpty()) {
            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Ingrese un email válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (role.isEmpty()) {
            Toast.makeText(context, "El rol no puede estar vacío", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private static void updateAdminInfo(Context context,
                                        String name,
                                        String email,
                                        String role,
                                        DatabaseReference databaseRef,
                                        String currentUserId,
                                        OnAdminInfoUpdatedListener listener) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("role", role);

        databaseRef.child(currentUserId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null && !user.getEmail().equals(email)) {
                        updateUserEmail(context, user, email, listener);
                    } else {
                        Toast.makeText(context, "Información actualizada", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onAdminInfoUpdated();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private static void updateUserEmail(Context context,
                                        FirebaseUser user,
                                        String newEmail,
                                        OnAdminInfoUpdatedListener listener) {

        user.updateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Información actualizada", Toast.LENGTH_SHORT).show();
                        if (listener != null) {
                            listener.onAdminInfoUpdated();
                        }
                    } else {
                        Toast.makeText(context,
                                "Error al actualizar email: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}