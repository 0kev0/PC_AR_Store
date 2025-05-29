package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;
import com.example.pcarstore.ModelsDB.DiscountCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DeactivateDiscountCodeDialog {
    public interface OnDiscountCodeDeactivatedListener {
        void onCodeDeactivated();
        void onDeactivationError(String error);
    }


    public static void show(Context context, DiscountCode code,
                            DatabaseReference databaseRef,
                            OnDiscountCodeDeactivatedListener listener) {

        new AlertDialog.Builder(context)
                .setTitle("Desactivar código")
                .setMessage("¿Estás seguro de marcar este código como inactivo?")
                .setPositiveButton("Desactivar", (dialog, which) ->
                        deactivateCode(context, code, databaseRef, listener))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private static void deactivateCode(Context context, DiscountCode code,
                                       DatabaseReference databaseRef,
                                       OnDiscountCodeDeactivatedListener listener) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "inactive");
        updates.put("usedDate", new Date().getTime());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            updates.put("usedBy", currentUser.getUid());
        }

        databaseRef.child(code.getCodeId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Código desactivado", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onCodeDeactivated();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al desactivar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onDeactivationError(e.getMessage());
                    }
                });
    }

}