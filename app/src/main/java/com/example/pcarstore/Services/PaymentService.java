package com.example.pcarstore.Services;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import com.google.firebase.database.DatabaseReference;

public class PaymentService {

    public interface PaymentCallback {
        void onSuccess(double newBalance);
        void onFailure(String errorMessage);
    }

    /**
     * Procesa el pago con tarjeta
     */
    public static void processCardPayment(Context context, DatabaseReference userBalanceRef, double amount, PaymentCallback callback) {

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Procesando pago con tarjeta...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Simular procesamiento de tarjeta (3 segundos)
        new Handler().postDelayed(() -> {
            userBalanceRef.get().addOnCompleteListener(task -> {
                progressDialog.dismiss();

                if (!task.isSuccessful()) {
                    callback.onFailure("Error al verificar saldo: " + task.getException());
                    return;
                }

                Double currentBalance = task.getResult().getValue(Double.class);
                if (currentBalance == null) currentBalance = 0.0;

                double newBalance = currentBalance + amount;
                userBalanceRef.setValue(newBalance)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(newBalance))
                        .addOnFailureListener(e -> callback.onFailure("Error al actualizar saldo"));
            });
        }, 3000);
    }
}