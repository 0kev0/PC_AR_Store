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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddDiscountCodeDialog {

    public interface OnDiscountCodeCreatedListener {
        void onCodeCreated(DiscountCode code);
        void onCreationError(String error);
    }

    public static void show(Context context, DatabaseReference databaseRef, OnDiscountCodeCreatedListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Debes iniciar sesión para crear códigos", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Crear nuevo código de descuento");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_discount_code, null);
        builder.setView(dialogView);

        EditText etCode = dialogView.findViewById(R.id.et_code);
        EditText etDiscount = dialogView.findViewById(R.id.et_discount);
        EditText etExpiryDate = dialogView.findViewById(R.id.et_expiry_date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etCode.setText("DISC" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());

        Calendar calendar = Calendar.getInstance();
        etExpiryDate.setOnClickListener(v -> showDatePicker(context, calendar, etExpiryDate, dateFormat));

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            try {
                String code = etCode.getText().toString();
                double discountPercentage = Double.parseDouble(etDiscount.getText().toString());
                String expiryDate = etExpiryDate.getText().toString();

                if (!validateInputs(context, code, discountPercentage, expiryDate)) {
                    return;
                }

                createDiscountCode(context, databaseRef, currentUser.getUid(),
                        code, discountPercentage, expiryDate, dateFormat, listener);

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Ingrese valores válidos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);

        builder.create().show();
    }

    private static void showDatePicker(Context context, Calendar calendar, EditText etExpiryDate,
                                       SimpleDateFormat dateFormat) {
        new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etExpiryDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
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

    private static void createDiscountCode(Context context, DatabaseReference databaseRef,
                                           String createdBy, String code, double discountPercentage,
                                           String expiryDate, SimpleDateFormat dateFormat,
                                           OnDiscountCodeCreatedListener listener) {
        String codeId = databaseRef.push().getKey();
        Date creationDate = new Date();

        Date expirationDate;
        try {
            expirationDate = dateFormat.parse(expiryDate);
        } catch (Exception e) {
            expirationDate = new Date(creationDate.getTime() + (30L * 24 * 60 * 60 * 1000));
        }

        DiscountCode discountCode = new DiscountCode();
        discountCode.setCodeId(codeId);
        discountCode.setCode(code);
        discountCode.setDiscountPercentage(discountPercentage);
        discountCode.setCreationDate(creationDate);
        discountCode.setExpirationDate(expirationDate);
        discountCode.setStatus("active");
        discountCode.setCreatedBy(createdBy);

        databaseRef.child(codeId).setValue(discountCode)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Código creado: " + code, Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onCodeCreated(discountCode);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al crear código: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onCreationError(e.getMessage());
                    }
                });
    }
}