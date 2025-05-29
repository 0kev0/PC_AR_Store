package com.example.pcarstore.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PrimeMembershipDialog extends Dialog {
/*************************************************************VARIABLES******************************************************************************************/
    private static final double PRIME_PRICE = 7.00;
    private User currentUser;
    private final OnPrimePurchaseListener purchaseListener;
    private TextView tvCurrentBalance;
    private MaterialButton btnPurchase, btnCancel;
    private ProgressBar progressBar;
    private DatabaseReference userRef;
    private DatabaseReference transactionsRef;
    private final FirebaseUser firebaseUser;

    public interface OnPrimePurchaseListener {
        void onPrimePurchased(boolean success);
    }

    public PrimeMembershipDialog(@NonNull Context context, FirebaseUser firebaseUser, OnPrimePurchaseListener listener) {
        super(context);
        this.firebaseUser = firebaseUser;
        this.purchaseListener = listener;

        if (firebaseUser != null) {
            this.userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(firebaseUser.getUid());

            this.transactionsRef = FirebaseDatabase.getInstance()
                    .getReference("transactions");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_buy_prime);

        initializeViews();
        loadUserData();
        setupListeners();
    }

    private void initializeViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        btnPurchase = findViewById(R.id.btn_Prime);
        btnCancel = findViewById(R.id.btnCancel_prime);
        progressBar = findViewById(R.id.progressBar);

        if (btnPurchase == null || btnCancel == null) {
            Log.e("PrimeMembershipDialog", "Error: Botones no encontrados en el layout");
            dismiss();
        }
    }

    private void loadUserData() {
        if (userRef == null) {
            dismiss();
            return;
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(User.class);
                    if (currentUser != null) {
                        updateUI();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    private void updateUI() {
        String balanceText = String.valueOf(currentUser.getSaldo());
        tvCurrentBalance.setText(balanceText);

        if (currentUser.isMembresiaPrime()) {
            btnPurchase.setText("ya posees prime :)");
            btnPurchase.setEnabled(false);
            btnPurchase.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.gray));
        } else if (currentUser.getSaldo() < PRIME_PRICE) {
            btnPurchase.setText("insuficiente saldo");
            btnPurchase.setEnabled(false);
            btnPurchase.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.gray));
        }
    }

    private void setupListeners() {
        btnPurchase.setOnClickListener(v -> attemptPrimePurchase());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void attemptPrimePurchase() {
        progressBar.setVisibility(View.VISIBLE);
        btnPurchase.setEnabled(false);
        btnCancel.setEnabled(false);

        userRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                User user = currentData.getValue(User.class);
                if (user == null || user.getSaldo() < PRIME_PRICE || user.isMembresiaPrime()) {
                    return Transaction.abort();
                }

                // Actualizar saldo y membresía
                user.setSaldo(user.getSaldo() - PRIME_PRICE);
                user.setMembresiaPrime(true);
                currentData.setValue(user);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    handleTransactionError();
                    return;
                }

                if (committed) {
                    // Guardar registro de la transacción
                    saveTransaction(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "¡Membresía Prime adquirida con éxito :)", Toast.LENGTH_SHORT).show();
                        if (purchaseListener != null) {
                            purchaseListener.onPrimePurchased(true);
                        }
                        dismiss();
                    });
                } else {
                    handleTransactionError();
                }
            }
        });
    }

    private void handleTransactionError() {
        progressBar.setVisibility(View.GONE);
        btnPurchase.setEnabled(true);
        btnCancel.setEnabled(true);
        Toast.makeText(getContext(), "Error al procesar la compra", Toast.LENGTH_SHORT).show();
        if (purchaseListener != null) {
            purchaseListener.onPrimePurchased(false);
        }
    }

    private void saveTransaction(Runnable onComplete) {
        if (firebaseUser == null || transactionsRef == null) {
            Log.e("Transaction", "Usuario no autenticado o referencia nula");
            onComplete.run();
            return;
        }

        String transactionId = transactionsRef.push().getKey();
        if (transactionId == null) {
            Log.e("Transaction", "No se pudo generar ID de transacción");
            onComplete.run();
            return;
        }

        // Crear objeto de transacción
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", PRIME_PRICE);
        transaction.put("date", ServerValue.TIMESTAMP);
        transaction.put("description", "Compra de membresía Prime");
        transaction.put("status", "completed");
        transaction.put("type", "prime_membership");
        transaction.put("userId", firebaseUser.getUid());
        transaction.put("membershipDuration", "30 días"); // Duración de la membresía

        // Guardar la transacción en Firebase
        transactionsRef.child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Transaction", "Transacción Prime guardada exitosamente");
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("Transaction", "Error al guardar transacción Prime", e);
                    onComplete.run(); // Continuamos aunque falle el registro
                });
    }

}