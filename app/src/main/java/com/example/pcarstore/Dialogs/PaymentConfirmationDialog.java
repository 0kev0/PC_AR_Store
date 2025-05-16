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
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.SummaryProductAdapter;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class PaymentConfirmationDialog extends DialogFragment {

    private List<OrderItem> orderItems;
    private double cartTotal;
    private PaymentConfirmationListener listener;

    public interface PaymentConfirmationListener {
        void onPaymentConfirmed();
        void onPaymentCancelled();
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
        TextView tvAvailableBalance = dialogView.findViewById(R.id.tv_available_balance);
        EditText etDiscountCode = dialogView.findViewById(R.id.et_discount_code);
        Button btnApplyDiscount = dialogView.findViewById(R.id.btn_apply_discount);
        RecyclerView rvSummaryProducts = dialogView.findViewById(R.id.rv_summary_products);
        TextView tvSubtotal = dialogView.findViewById(R.id.tv_subtotal);
        TextView tvShipping = dialogView.findViewById(R.id.tv_shipping);
        TextView tvDiscount = dialogView.findViewById(R.id.tv_discount);
        TextView tvTotal = dialogView.findViewById(R.id.tv_total);
        Button btnConfirmPayment = dialogView.findViewById(R.id.btn_confirm_payment);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Configurar RecyclerView
        rvSummaryProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        SummaryProductAdapter adapter = new SummaryProductAdapter(orderItems);
        rvSummaryProducts.setAdapter(adapter);

        // Cargar saldo del usuario
        loadUserBalance(tvAvailableBalance);

        // Configurar montos iniciales
        double shipping = calculateShipping(cartTotal);
        double discount = 0.0;

        updatePaymentSummary(tvSubtotal, tvShipping, tvDiscount, tvTotal, cartTotal, shipping, discount);

        // Configurar botones
        btnApplyDiscount.setOnClickListener(v -> {
            String discountCode = etDiscountCode.getText().toString().trim();
            if (!discountCode.isEmpty()) {
                applyDiscountCode(discountCode, tvDiscount, tvTotal, cartTotal, shipping);
            } else {
                Toast.makeText(getContext(), "Ingrese un código de descuento", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirmPayment.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentConfirmed();
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentCancelled();
            }
            dismiss();
        });

        return builder.create();
    }

    private void updatePaymentSummary(TextView subtotalView, TextView shippingView,
                                      TextView discountView, TextView totalView,
                                      double subtotal, double shipping, double discount) {
        subtotalView.setText(String.format(Locale.getDefault(), "%.2f €", subtotal));
        shippingView.setText(String.format(Locale.getDefault(), "%.2f €", shipping));
        discountView.setText(String.format(Locale.getDefault(), "-%.2f €", discount));
        totalView.setText(String.format(Locale.getDefault(), "%.2f €", subtotal + shipping - discount));
    }

    private void loadUserBalance(TextView balanceView) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double balance = snapshot.child("balance").getValue(Double.class);
                    if (balance != null) {
                        balanceView.setText(String.format(Locale.getDefault(), "Saldo disponible: %.2f €", balance));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                balanceView.setText("Error al cargar saldo");
            }
        });
    }

    private double calculateShipping(double subtotal) {
        return subtotal > 50 ? 0 : 5.99;
    }

    private void applyDiscountCode(String code, TextView discountView, TextView totalView,
                                   double subtotal, double shipping) {
        DatabaseReference discountsRef = FirebaseDatabase.getInstance().getReference("discountCodes").child(code);

        discountsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double discountPercent = snapshot.child("discountPercent").getValue(Double.class);
                    if (discountPercent != null) {
                        double discountAmount = subtotal * (discountPercent / 100);
                        discountView.setText(String.format(Locale.getDefault(), "-%.2f €", discountAmount));
                        totalView.setText(String.format(Locale.getDefault(), "%.2f €", subtotal + shipping - discountAmount));
                        Toast.makeText(getContext(), "Descuento aplicado!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Código inválido o expirado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al verificar descuento", Toast.LENGTH_SHORT).show();
            }
        });
    }
}