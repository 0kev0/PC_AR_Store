package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePasswordDialog {

    public interface OnPasswordResetListener {
        void onResetEmailSent(String email);
        void onResetError(String error);
    }

    public static void show(Context context, OnPasswordResetListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Restablecer Contraseña");
        builder.setMessage("Ingrese su correo electrónico para recibir el enlace de restablecimiento");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("correo@ejemplo.com");
        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (validateEmail(context, email)) {
                sendPasswordResetEmail(context, email, listener);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }

    private static boolean validateEmail(Context context, String email) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private static void sendPasswordResetEmail(Context context, String email, OnPasswordResetListener listener) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (listener != null) {
                            listener.onResetEmailSent(email);
                        }
                        Toast.makeText(context, "Correo de restablecimiento enviado a " + email, Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() :
                                "Error desconocido";
                        if (listener != null) {
                            listener.onResetError(error);
                        }
                        Toast.makeText(context, "Error al enviar correo: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}