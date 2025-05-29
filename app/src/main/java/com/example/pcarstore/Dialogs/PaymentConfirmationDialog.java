package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.pcarstore.ModelsDB.Departamento;
import com.example.pcarstore.ModelsDB.DiscountCode;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.R;
import com.example.pcarstore.Services.SoundService;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PaymentConfirmationDialog extends DialogFragment {
    /*************************************************************VARIABLES******************************************************************************************/
    private final List<OrderItem> orderItems;
    private final double cartTotal;
    private final PaymentConfirmationListener listener;
    private double currentDiscount = 0.0;
    private String appliedDiscountCode = null;
    private TextView tvAvailableBalance;
    private TextView tvShipping;
    private TextView tvSubtotal;
    private TextView tvDiscount;
    private TextView tvTotal;
    private double userBalance = 0.0;
    private double shippingCost = 0.0;
    private boolean isPrimeMember = false;

    public interface PaymentConfirmationListener {
        void onPaymentConfirmed(double discountApplied);
        void onPaymentCancelled();
        void onInsufficientBalance(double missingAmount);
    }

    public PaymentConfirmationDialog(List<OrderItem> orderItems, double cartTotal, PaymentConfirmationListener listener) {
        this.orderItems = orderItems;
        this.cartTotal = cartTotal;
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_payment_confirmation, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return view;
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
        tvSubtotal = dialogView.findViewById(R.id.tv_subtotal);
        tvShipping = dialogView.findViewById(R.id.tv_shipping);
        tvDiscount = dialogView.findViewById(R.id.tv_discount);
        tvTotal = dialogView.findViewById(R.id.tv_total);
        Button btnConfirmPayment = dialogView.findViewById(R.id.btn_confirm_payment);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Configurar RecyclerView
        rvSummaryProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        SummaryProductAdapter adapter = new SummaryProductAdapter(orderItems);
        rvSummaryProducts.setAdapter(adapter);

        // Mostrar costo de envío con valor por defecto mientras se calcula
        tvShipping.setText(String.format(Locale.getDefault(), "%.2f $", 0.0));

        // Cargar datos del usuario (saldo y membresía)
        loadUserData();

        // Configurar montos iniciales (envío se calculará después de cargar datos)
        updatePaymentSummary(cartTotal, 0.0, currentDiscount);

        // Configurar botones
        btnApplyDiscount.setOnClickListener(v -> {
            String discountCode = etDiscountCode.getText().toString().trim();
            if (!discountCode.isEmpty()) {
                validateAndApplyDiscount(discountCode);
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

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Obtener saldo
                Double balance = snapshot.child("saldo").getValue(Double.class);
                if (balance != null) {
                    userBalance = balance;
                    tvAvailableBalance.setText(String.format(Locale.getDefault(),
                            "Saldo disponible: %.2f $", userBalance));
                }

                // Obtener membresía Prime
                Boolean prime = snapshot.child("membresiaPrime").getValue(Boolean.class);
                isPrimeMember = prime != null && prime;

                // Obtener ubicación y calcular envío
                String departamento = snapshot.child("departamento").getValue(String.class);
                String ciudad = snapshot.child("ciudad").getValue(String.class);

                calculateShippingCost(departamento, ciudad, isPrimeMember);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Usar valores por defecto en caso de error
                calculateShippingCost("", "", false);
            }
        });
    }

    private void calculateShippingCost(String departamento, String ciudad, boolean isPrimeMember) {
        // 1. Verificar membresía Prime (envío gratis)
        if (isPrimeMember) {
            shippingCost = 0.0;
            // Actualizar UI directamente con mensaje de envío gratis
            tvShipping.setText(String.format(Locale.getDefault(), "GRATIS", shippingCost));
            Log.d("ShippingCost", "Free shipping for Prime member");
            updatePaymentSummary(cartTotal, shippingCost, currentDiscount);
            return;
        }

        // 2. Si no hay datos de ubicación, usar costo por defecto
        if (departamento == null || departamento.isEmpty() || ciudad == null || ciudad.isEmpty()) {
            shippingCost = cartTotal > 500 ? 2.99 : 5.99;
            updatePaymentSummary(cartTotal, shippingCost, currentDiscount);
            Log.d("ShippingCost", "Using default shipping cost");
            return;
        }

        // 3. Mostrar que se está calculando el costo
        tvShipping.setText("Calculando...");

        // 4. Obtener costos de envío desde el departamento correspondiente
        Query deptQuery = FirebaseDatabase.getInstance()
                .getReference("departamentos")
                .orderByChild("nombre")
                .equalTo(departamento);

        deptQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot depSnapshot : dataSnapshot.getChildren()) {
                        Departamento dept = depSnapshot.getValue(Departamento.class);
                        if (dept != null) {
                            // Calcular costo usando el nuevo modelo
                            shippingCost = dept.calcularCostoEnvio(ciudad);
                            Log.d("ShippingCost", "Calculated shipping cost: " + shippingCost);

                            // Aplicar descuento por compra mayor a $50
                            if (cartTotal > 500) {
                                Log.d("ShippingCost", "Applying discount > 500");
                                shippingCost *= 0.5; // 50% de descuento
                            }

                            updatePaymentSummary(cartTotal, shippingCost, currentDiscount);
                            return;
                        }
                    }
                }

                // Si no se encuentra el departamento
                shippingCost = cartTotal > 50 ? 2.99 : 5.99;
                Log.d("ShippingCost", "Department not found");
                updatePaymentSummary(cartTotal, shippingCost, currentDiscount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                shippingCost = cartTotal > 50 ? 2.99 : 5.99;
                updatePaymentSummary(cartTotal, shippingCost, currentDiscount);
            }
        });
    }

    private void verifyAndProcessPayment() {
        double totalToPay = cartTotal + shippingCost - currentDiscount;

        if (userBalance >= totalToPay) {
            Intent soundIntent = new Intent(getContext(), SoundService.class);
            if (getContext() != null) {
                getContext().startService(soundIntent);
            }
            processPayment(totalToPay);
        } else {
            double missingAmount = totalToPay - userBalance;
            if (listener != null) {
                dismiss();
                listener.onInsufficientBalance(missingAmount);
            }
            Toast.makeText(getContext(),
                    String.format(Locale.getDefault(),
                            "Saldo insuficiente. Faltan %.2f $", missingAmount),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void processPayment(double amount) {
        if (appliedDiscountCode != null) {
            markDiscountAsUsed(appliedDiscountCode, () -> {
                updateUserBalance(amount, () -> {
                    saveTransaction(amount, () -> {
                        if (listener != null) {
                            listener.onPaymentConfirmed(currentDiscount);
                        }
                        dismiss();
                    });
                });
            });
        } else {
            updateUserBalance(amount, () -> {
                saveTransaction(amount, () -> {
                    if (listener != null) {
                        listener.onPaymentConfirmed(0.0);
                    }
                    dismiss();
                });
            });
        }
    }

    private void saveTransaction(double amount, Runnable onComplete) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance()
                .getReference("transactions");

        String transactionId = transactionsRef.push().getKey();

        // Crear objeto de transacción
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", amount);
        transaction.put("date", ServerValue.TIMESTAMP);
        transaction.put("description", "Compra de productos");
        transaction.put("status", "completed");
        transaction.put("type", "sale");
        transaction.put("userId", userId);

        // Agregar detalles de los productos
        List<Map<String, Object>> products = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Map<String, Object> product = new HashMap<>();
            product.put("productId", item.getProductId());
            product.put("quantity", item.getQuantity());
            product.put("price", item.getPrice());
            products.add(product);
        }
        transaction.put("products", products);

        // Agregar información de envío y descuento si aplica
        transaction.put("shippingCost", shippingCost);
        if (appliedDiscountCode != null) {
            transaction.put("discountCode", appliedDiscountCode);
            transaction.put("discountAmount", currentDiscount);
        }

        transactionsRef.child(transactionId).setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Transaction", "Transacción guardada exitosamente");
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("Transaction", "Error al guardar transacción", e);
                    Toast.makeText(getContext(), "Error al registrar transacción", Toast.LENGTH_SHORT).show();
                    onComplete.run();
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
                    Toast.makeText(getContext(), "Error al actualizar saldo", Toast.LENGTH_SHORT).show();
                }
                onComplete.run();
            }
        });
    }

    private void validateAndApplyDiscount(String code) {
        DatabaseReference discountsRef = FirebaseDatabase.getInstance().getReference("discountCodes");
        Query discountQuery = discountsRef.orderByChild("code").equalTo(code);
        Log.d("DiscountCode", "Validating discount code: " + code);

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
                    if (cartTotal < discount.getMinPurchaseRequired()) {
                        String message = String.format(Locale.getDefault(),
                                "Requiere compra mínima de %.2f €",
                                discount.getMinPurchaseRequired());
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Aplicar descuento
                    applyValidDiscount(discount, discountSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al verificar descuento", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePaymentSummary(double subtotal, double shipping, double discount) {
        tvSubtotal.setText(String.format(Locale.getDefault(), "%.2f $", subtotal));

        // Actualizar con lógica específica para el envío
        if (isPrimeMember) {
            tvShipping.setText("GRATIS (Prime)");
            tvShipping.setTextColor(Color.GREEN);
            Log.d("ShippingCost", "Free shipping for Prime member");
        } else if (shipping == 0.0) {
            tvShipping.setText("GRATIS");
            Log.d("ShippingCost", "Free shipping");
        } else if (cartTotal > 500 && shipping < 5.99) {
            // Si tiene descuento por compra mayor a 500
            tvShipping.setText(String.format(Locale.getDefault(), "%.2f $ (50%% desc.)", shipping));
            Log.d("ShippingCost", "Shipping cost with discount: " + shipping);
        } else {
            tvShipping.setText(String.format(Locale.getDefault(), "%.2f $", shipping));
            Log.d("ShippingCost", "Shipping cost: " + shipping);
        }
    Log.d("ShippingCost", "Discount: " + discount);
        tvDiscount.setText(String.format(Locale.getDefault(), "-%.2f $", discount));
        tvTotal.setText(String.format(Locale.getDefault(), "%.2f $", subtotal + shipping - discount));
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

    private void applyValidDiscount(DiscountCode discount, String discountId) {
        currentDiscount = cartTotal * (discount.getDiscountPercentage() / 100);
        appliedDiscountCode = discountId;

        updatePaymentSummary(cartTotal, shippingCost, currentDiscount);

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
                    Toast.makeText(getContext(), "Error al registrar descuento", Toast.LENGTH_SHORT).show();
                    onComplete.run();
                });
    }

}