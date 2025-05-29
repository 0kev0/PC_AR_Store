package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pcarstore.ModelsDB.GiftCard;
import com.example.pcarstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GiftCardDialog extends DialogFragment {
    /*************************************************************VARIABLES******************************************************************************************/
    private final Context context;
    private final FirebaseAuth mAuth;
    private final DatabaseReference databaseRef;
    private AlertDialog dialog;
    private GiftCardCallback callback;

    public GiftCardDialog(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public interface GiftCardCallback {
        void onSuccess(double amount);
        void onFailure(String error);
    }

    public void setGiftCardCallback(GiftCardCallback callback) {
        this.callback = callback;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_discount, null);

        EditText etCode = dialogView.findViewById(R.id.etDiscountCode);
        Button btnRedeem = dialogView.findViewById(R.id.btnRedeem);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        builder.setView(dialogView);

        dialog = builder.create();

        btnRedeem.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (code.isEmpty()) {
                etCode.setError("Ingrese un código");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnRedeem.setEnabled(false);

            validateAndRedeemGiftCard(code, new GiftCardCallback() {
                @Override
                public void onSuccess(double amount) {
                    saveCreditToUser(amount, new CreditUpdateCallback() {
                        @Override
                        public void onSuccess(double addedAmount, Double newBalance) {
                            progressBar.setVisibility(View.GONE);
                            btnRedeem.setEnabled(true);
                            if (callback != null) {
                                callback.onSuccess(addedAmount);
                            }
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailure(String error) {
                            progressBar.setVisibility(View.GONE);
                            btnRedeem.setEnabled(true);
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    progressBar.setVisibility(View.GONE);
                    btnRedeem.setEnabled(true);
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void validateAndRedeemGiftCard(String code, GiftCardCallback callback) {
        if (!code.startsWith("PDM-") || code.length() != 10) {
            callback.onFailure("Formato de código inválido");
            return;
        }

        Query query = databaseRef.child("giftCards").orderByChild("code").equalTo(code);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure("Gift Card no encontrada");
                    return;
                }

                for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                    GiftCard giftCard = cardSnapshot.getValue(GiftCard.class);
                    if (giftCard == null) {
                        callback.onFailure("Error al leer Gift Card");
                        return;
                    }

                    validateGiftCard(giftCard, callback, cardSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure("Error de conexión: " + error.getMessage());
            }
        });
    }

    private void validateGiftCard(GiftCard giftCard, GiftCardCallback callback, DataSnapshot cardSnapshot) {
        if ("redeemed".equals(giftCard.getStatus())) {
            callback.onFailure("Esta Gift Card ya fue canjeada");
            return;
        }

        if (giftCard.getExpirationDate() != null && giftCard.getExpirationDate().before(new Date())) {
            callback.onFailure("Gift Card expirada");
            return;
        }

        if (!"active".equals(giftCard.getStatus())) {
            callback.onFailure("Gift Card no está activa");
            return;
        }

        updateGiftCardStatus(cardSnapshot, giftCard.getAmount(), callback);
    }

    private void updateGiftCardStatus(DataSnapshot cardSnapshot, double amount, GiftCardCallback callback) {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "redeemed");
        updates.put("redeemedBy", currentUserId);
        updates.put("redeemedDate", ServerValue.TIMESTAMP);

        cardSnapshot.getRef().updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(amount))
                .addOnFailureListener(e -> callback.onFailure("Error al canjear Gift Card: " + e.getMessage()));
    }

    private void saveCreditToUser(double amount, CreditUpdateCallback callback) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        DatabaseReference userRef = databaseRef.child("users").child(userId).child("saldo");

        userRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Double currentSaldo = currentData.getValue(Double.class);
                if (currentSaldo == null) {
                    currentData.setValue(amount);
                } else {
                    currentData.setValue(currentSaldo + amount);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    callback.onFailure("Error al actualizar saldo: " + error.getMessage());
                } else if (committed) {
                    callback.onSuccess(amount, currentData.getValue(Double.class));
                }
            }
        });
    }

    private interface CreditUpdateCallback {
        void onSuccess(double addedAmount, Double newBalance);
        void onFailure(String error);
    }

}