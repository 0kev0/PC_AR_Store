package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.example.pcarstore.ModelsDB.GiftCard;
import com.example.pcarstore.R;
import com.google.firebase.database.DatabaseReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditGiftCardDialog {

    public interface OnGiftCardUpdatedListener {
        void onGiftCardUpdated();
        void onUpdateError(String error);
    }


    public static void show(Context context, GiftCard giftCard,
                            DatabaseReference databaseRef,
                            SimpleDateFormat dateFormatter,
                            OnGiftCardUpdatedListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar Gift Card");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_giftcard, null);
        builder.setView(dialogView);

        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etCurrency = dialogView.findViewById(R.id.et_currency);
        EditText etExpiryDate = dialogView.findViewById(R.id.et_expiry_date);
        EditText etStatus = dialogView.findViewById(R.id.et_status);
        EditText etRecipientEmail = dialogView.findViewById(R.id.et_recipient_email);

        etAmount.setText(String.valueOf(giftCard.getAmount()));
        etCurrency.setText(giftCard.getCurrency() != null ? giftCard.getCurrency() : "USD");
        etStatus.setText(giftCard.getStatus());
        etRecipientEmail.setText(giftCard.getRecipientEmail() != null ? giftCard.getRecipientEmail() : "");

        Date expirationDate = giftCard.getExpirationDate() != null ?
                giftCard.getExpirationDate() :
                new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000));
        etExpiryDate.setText(dateFormatter.format(expirationDate));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expirationDate);

        etExpiryDate.setOnClickListener(v ->
                showDatePicker(context, calendar, etExpiryDate, dateFormatter));

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            try {
                double newAmount = Double.parseDouble(etAmount.getText().toString());
                String newCurrency = etCurrency.getText().toString();
                String newStatus = etStatus.getText().toString();
                String newRecipientEmail = etRecipientEmail.getText().toString();
                Date newExpiryDate = dateFormatter.parse(etExpiryDate.getText().toString());

                if (!validateInputs(context, newAmount, newCurrency, newStatus)) {
                    return;
                }

                updateGiftCard(context, giftCard, newAmount, newCurrency,
                        newExpiryDate, newStatus, newRecipientEmail,
                        databaseRef, listener);

            } catch (Exception e) {
                Toast.makeText(context, "Error en los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private static void showDatePicker(Context context, Calendar calendar,
                                       EditText etExpiryDate, SimpleDateFormat dateFormatter) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etExpiryDate.setText(dateFormatter.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private static boolean validateInputs(Context context, double amount, String currency, String status) {
        if (amount <= 0) {
            Toast.makeText(context, "Monto debe ser positivo", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (currency == null || currency.isEmpty()) {
            Toast.makeText(context, "Moneda es requerida", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private static void updateGiftCard(Context context, GiftCard originalGiftCard,
                                       double newAmount, String newCurrency,
                                       Date newExpiryDate, String newStatus,
                                       String newRecipientEmail,
                                       DatabaseReference databaseRef,
                                       OnGiftCardUpdatedListener listener) {

        GiftCard updatedGiftCard = new GiftCard(
                originalGiftCard.getCode(),
                newAmount,
                originalGiftCard.getCreatedBy()
        );

        updatedGiftCard.setCardId(originalGiftCard.getCardId());
        updatedGiftCard.setCurrency(newCurrency);
        updatedGiftCard.setExpirationDate(newExpiryDate);
        updatedGiftCard.setStatus(newStatus);
        updatedGiftCard.setRecipientEmail(newRecipientEmail.isEmpty() ? null : newRecipientEmail);
        updatedGiftCard.setCreationDate(originalGiftCard.getCreationDate());

        DatabaseReference targetRef;
        String refKey = databaseRef.getKey();

        if (refKey == null) {
            targetRef = databaseRef.child("giftCards").child(originalGiftCard.getCardId());
        } else if (refKey.equals("giftCards")) {
            targetRef = databaseRef.child(originalGiftCard.getCardId());
        } else {
            targetRef = databaseRef.child("giftCards").child(originalGiftCard.getCardId());
        }

        targetRef.setValue(updatedGiftCard.toMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Gift Card actualizada", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onGiftCardUpdated();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onUpdateError(e.getMessage());
                    }
                });
    }

}