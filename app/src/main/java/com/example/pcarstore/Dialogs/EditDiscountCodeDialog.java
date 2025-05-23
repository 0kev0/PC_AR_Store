package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.example.pcarstore.ModelsDB.DiscountCode;
import com.example.pcarstore.R;
import com.google.firebase.database.DatabaseReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class EditDiscountCodeDialog {

    public interface OnDiscountCodeUpdatedListener {
        void onCodeUpdated();
        void onUpdateError(String error);
    }

    public static void show(Context context, DiscountCode code,
                            DatabaseReference databaseRef, SimpleDateFormat dateFormat,
                            OnDiscountCodeUpdatedListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar código de descuento");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_discount_code, null);
        builder.setView(dialogView);

        EditText etCode = dialogView.findViewById(R.id.et_code);
        EditText etDiscount = dialogView.findViewById(R.id.et_discount);
        EditText etExpiryDate = dialogView.findViewById(R.id.et_expiry_date);

        // Configurar valores actuales
        etCode.setText(code.getCode());
        etDiscount.setText(String.valueOf(code.getDiscountPercentage()));
        if (code.getExpirationDate() != null) {
            etExpiryDate.setText(dateFormat.format(code.getExpirationDate()));
        }

        Calendar calendar = Calendar.getInstance();
        if (code.getExpirationDate() != null) {
            calendar.setTime(code.getExpirationDate());
        }

        etExpiryDate.setOnClickListener(v -> showDatePicker(context, calendar, etExpiryDate, dateFormat));

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            try {
                String newCode = etCode.getText().toString();
                double discountPercentage = Double.parseDouble(etDiscount.getText().toString());
                String expiryDate = etExpiryDate.getText().toString();

                if (!validateInputs(context, newCode, discountPercentage, expiryDate)) {
                    return;
                }

                updateDiscountCode(context, code, newCode, discountPercentage,
                        expiryDate, databaseRef, dateFormat, listener);

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Ingrese valores válidos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);

        builder.create().show();
    }

    private static void showDatePicker(Context context, Calendar calendar, EditText etExpiryDate,
                                       SimpleDateFormat dateFormat) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etExpiryDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private static boolean validateInputs(Context context, String code, double discountPercentage, String expiryDate) {
        if (code.isEmpty() || expiryDate.isEmpty()) {
            Toast.makeText(context, "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (discountPercentage <= 0 || discountPercentage > 100) {
            Toast.makeText(context, "El descuento debe ser entre 0.1% y 100%", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private static void updateDiscountCode(Context context, DiscountCode originalCode,
                                           String newCode, double discountPercentage,
                                           String expiryDate, DatabaseReference databaseRef,
                                           SimpleDateFormat dateFormat,
                                           OnDiscountCodeUpdatedListener listener) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("code", newCode);
        updates.put("discountPercentage", discountPercentage);

        try {
            Date expirationDate = dateFormat.parse(expiryDate);
            updates.put("expirationDate", expirationDate);
        } catch (Exception e) {
            Toast.makeText(context, "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.child(originalCode.getCodeId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Código actualizado", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onCodeUpdated();
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