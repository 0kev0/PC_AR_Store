package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.example.pcarstore.ModelsDB.GiftCard;
import com.example.pcarstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import java.util.Date;

public class CreateGiftCardDialog {
    public interface OnGiftCardCreatedListener {
        void onGiftCardCreated(GiftCard giftCard);
        void onCreationError(String error);
    }


    public static void show(Context context, DatabaseReference databaseRef, OnGiftCardCreatedListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Debes iniciar sesión para crear gift cards", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Crear nueva Gift Card");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_giftcard, null);
        builder.setView(dialogView);

        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etCurrency = dialogView.findViewById(R.id.et_currency);
        EditText etExpiryDays = dialogView.findViewById(R.id.et_expiry_days);
        EditText etRecipientEmail = dialogView.findViewById(R.id.et_recipient_email);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            try {
                double amount = Double.parseDouble(etAmount.getText().toString());
                String currency = etCurrency.getText().toString();
                int expiryDays = Integer.parseInt(etExpiryDays.getText().toString());
                String recipientEmail = etRecipientEmail.getText().toString();

                if (!validateInputs(context, amount, currency, expiryDays)) {
                    return;
                }

                createGiftCard(context, databaseRef, currentUser.getUid(),
                        amount, currency, expiryDays, recipientEmail, listener);

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Ingrese valores válidos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);

        builder.create().show();
    }

    private static boolean validateInputs(Context context, double amount, String currency, int expiryDays) {
        if (amount <= 0) {
            Toast.makeText(context, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (currency == null || currency.isEmpty()) {
            Toast.makeText(context, "La moneda es requerida", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (expiryDays <= 0) {
            Toast.makeText(context, "Los días de validez deben ser mayores a 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private static void createGiftCard(Context context, DatabaseReference databaseRef,
                                       String userId, double amount, String currency,
                                       int expiryDays, String recipientEmail,
                                       OnGiftCardCreatedListener listener) {

        // Generar un ID único para la gift card
        String cardId = databaseRef.child("giftCards").push().getKey();
        if (cardId == null) {
            Toast.makeText(context, "Error al generar ID para la gift card", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear la gift card usando el constructor adecuado
        GiftCard giftCard = new GiftCard(GiftCard.generateGiftCardCode(), amount, userId);
        giftCard.setCardId(cardId);
        giftCard.setCurrency(currency);
        giftCard.setExpirationDate(calculateExpirationDate(expiryDays));
        giftCard.setRecipientEmail(recipientEmail.isEmpty() ? null : recipientEmail);

        // Determinar la referencia correcta
        DatabaseReference targetRef;
        if (databaseRef.getKey() == null || !databaseRef.getKey().equals("giftCards")) {
            // Si es la referencia raíz o no apunta a giftCards
            targetRef = databaseRef.child("giftCards").child(cardId);
        } else {
            // Si ya apunta al nodo giftCards
            targetRef = databaseRef.child(cardId);
        }

        // Guardar en Firebase
        targetRef.setValue(giftCard.toMap())
                .addOnSuccessListener(aVoid -> {
                    String message = String.format("Gift Card creada!\nCódigo: %s", giftCard.getCode());
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    if (listener != null) {
                        listener.onGiftCardCreated(giftCard);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al crear gift card: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onCreationError(e.getMessage());
                    }
                });
    }

    private static Date calculateExpirationDate(int days) {
        long daysInMillis = days * 24L * 60 * 60 * 1000;
        return new Date(System.currentTimeMillis() + daysInMillis);
    }

}