package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.SummaryProductAdapter;
import com.example.pcarstore.ModelsDB.DiscountCode;
import com.example.pcarstore.ModelsDB.OrderItem;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PaymentConfirmationDialog extends DialogFragment {

    private List<OrderItem> orderItems;
    private double cartTotal;
    private PaymentConfirmationListener listener;
    private double currentDiscount = 0.0;
    private String appliedDiscountCode = null;
    private Button btnConfirmPayment;
    private TextView tvAvailableBalance;
    private double userBalance = 0.0;

    public interface PaymentConfirmationListener {
        void onPaymentConfirmed(double discountApplied); // Añadido parámetro
        void onPaymentCancelled();
        void onInsufficientBalance(double missingAmount);
    }

    public PaymentConfirmationDialog(List<OrderItem> orderItems, double cartTotal, PaymentConfirmationListener listener) {
        this.orderItems = orderItems;
        this.cartTotal = cartTotal;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_payment_confirmation, null);
        builder.setView(dialogView);

        // Inicializar vistas
         tvAvailableBalance = dialogView.findViewById(R.id.tv_available_balance);
        EditText etDiscountCode = dialogView.findViewById(R.id.et_discount_code);
        Button btnApplyDiscount = dialogView.findViewById(R.id.btn_apply_discount);
        RecyclerView rvSummaryProducts = dialogView.findViewById(R.id.rv_summary_products);
        TextView tvSubtotal = dialogView.findViewById(R.id.tv_subtotal);
        TextView tvShipping = dialogView.findViewById(R.id.tv_shipping);
        TextView tvDiscount = dialogView.findViewById(R.id.tv_discount);
        TextView tvTotal = dialogView.findViewById(R.id.tv_total);
         btnConfirmPayment = dialogView.findViewById(R.id.btn_confirm_payment);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Configurar RecyclerView
        rvSummaryProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        SummaryProductAdapter adapter = new SummaryProductAdapter(orderItems);
        rvSummaryProducts.setAdapter(adapter);

        // Cargar saldo del usuario
        loadUserBalance();

        // Configurar montos iniciales
        double shipping = calculateShipping(cartTotal);
        double discount = 0.0;

        updatePaymentSummary(tvSubtotal, tvShipping, tvDiscount, tvTotal, cartTotal, shipping, discount);

        // Configurar botones
        btnApplyDiscount.setOnClickListener(v -> {
            String discountCode = etDiscountCode.getText().toString().trim();
            if (!discountCode.isEmpty()) {
                validateAndApplyDiscount(discountCode, tvDiscount, tvTotal, cartTotal, shipping);
            } else {
                Toast.makeText(getContext(), "Ingrese un código de descuento", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirmPayment.setOnClickListener(v -> {
            verifyAndProcessPayment();
        });

        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentCancelled();
            }
            dismiss();
        });

        return builder.create();
    }

    private void verifyAndProcessPayment() {
        double shipping = calculateShipping(cartTotal);
        double totalToPay = cartTotal + shipping - currentDiscount;

        // Usamos la variable userBalance que ya está cargada
        if (userBalance >= totalToPay) {
            processPayment(totalToPay);
        } else {
            double missingAmount = totalToPay - userBalance;
            if (listener != null) {
                listener.onInsufficientBalance(missingAmount);
            }
            Toast.makeText(getContext(),
                    String.format(Locale.getDefault(),
                            "Saldo insuficiente. Faltan %.2f €", missingAmount),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void processPayment(double amount) {
        if (appliedDiscountCode != null) {
            markDiscountAsUsed(appliedDiscountCode, () -> {
                updateUserBalance(amount, () -> {
                    if (listener != null) {
                        listener.onPaymentConfirmed(currentDiscount); // Pasar el descuento
                    }
                    dismiss();
                });
            });
        } else {
            updateUserBalance(amount, () -> {
                if (listener != null) {
                    listener.onPaymentConfirmed(0.0); // Sin descuento aplicado
                }
                dismiss();
            });
        }
    }
    private void loadUserBalance() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("saldo");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double balance = snapshot.getValue(Double.class);
                if (balance != null) {
                    userBalance = balance;
                    tvAvailableBalance.setText(String.format(Locale.getDefault(),
                            "Saldo disponible: %.2f €", userBalance));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvAvailableBalance.setText("Error al cargar saldo");
            }
        });
    }

    private void updateUserBalance(double amount, Runnable onComplete) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("saldo");

        userRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Double currentBalance = currentData.getValue(Double.class);
                if (currentBalance == null) {
                    currentBalance = 0.0;
                }
                currentData.setValue(currentBalance - amount);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Toast.makeText(getContext(),
                            "Error al actualizar saldo",
                            Toast.LENGTH_SHORT).show();
                }
                onComplete.run();
            }
        });
    }

    private void validateAndApplyDiscount(String code, TextView discountView, TextView totalView,
                                          double subtotal, double shipping) {
        DatabaseReference discountsRef = FirebaseDatabase.getInstance().getReference("discountCodes");

        Query discountQuery = discountsRef.orderByChild("code").equalTo(code);

        discountQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getContext(), "Código no encontrado", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot discountSnapshot : snapshot.getChildren()) {
                    DiscountCode discount = discountSnapshot.getValue(DiscountCode.class);
                    if (discount == null) continue;

                    // Validar el código
                    if (!discount.isValid()) {
                        Toast.makeText(getContext(),
                                getDiscountValidationError(discount),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Validar mínimo de compra
                    if (subtotal < discount.getMinPurchaseRequired()) {
                        String message = String.format(Locale.getDefault(),
                                "Requiere compra mínima de %.2f €",
                                discount.getMinPurchaseRequired());
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Aplicar descuento
                    applyValidDiscount(discount, discountSnapshot.getKey(),
                            discountView, totalView, subtotal, shipping);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al verificar descuento", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updatePaymentSummary(TextView subtotalView, TextView shippingView,
                                      TextView discountView, TextView totalView,
                                      double subtotal, double shipping, double discount) {
        subtotalView.setText(String.format(Locale.getDefault(), "%.2f €", subtotal));
        shippingView.setText(String.format(Locale.getDefault(), "%.2f €", shipping));
        discountView.setText(String.format(Locale.getDefault(), "-%.2f €", discount));
        totalView.setText(String.format(Locale.getDefault(), "%.2f €", subtotal + shipping - discount));
    }

    private double calculateShipping(double subtotal) {
        return subtotal > 50 ? 0 : 5.99;
    }

    private String getDiscountValidationError(DiscountCode discount) {
        if ("used".equals(discount.getStatus())) {
            return "Este código ya fue utilizado";
        } else if (discount.getExpirationDate() != null &&
                discount.getExpirationDate().before(new Date())) {
            return "Código expirado";
        }
        return "Código no válido";
    }

    private void applyValidDiscount(DiscountCode discount, String discountId,
                                    TextView discountView, TextView totalView,
                                    double subtotal, double shipping) {
        currentDiscount = subtotal * (discount.getDiscountPercentage() / 100);
        appliedDiscountCode = discountId;

        // Actualizar solo las vistas que cambian
        discountView.setText(String.format(Locale.getDefault(), "-%.2f €", currentDiscount));
        totalView.setText(String.format(Locale.getDefault(), "%.2f €", subtotal + shipping - currentDiscount));

        String message = String.format(Locale.getDefault(),
                "Descuento del %.0f%% aplicado!",
                discount.getDiscountPercentage());
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void markDiscountAsUsed(String discountId, Runnable onComplete) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference discountRef = FirebaseDatabase.getInstance()
                .getReference("discountCodes")
                .child(discountId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "used");
        updates.put("usedBy", userId);
        updates.put("usedDate", ServerValue.TIMESTAMP);

        discountRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> onComplete.run())
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error al registrar descuento",
                            Toast.LENGTH_SHORT).show();
                    onComplete.run();
                });
    }
}