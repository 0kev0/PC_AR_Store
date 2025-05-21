package com.example.pcarstore.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.CartAdapter;
import com.example.pcarstore.Dialogs.CreditCardPaymentDialog;
import com.example.pcarstore.Dialogs.PaymentConfirmationDialog;
import com.example.pcarstore.ModelsDB.Cart;
import com.example.pcarstore.ModelsDB.Departamento;
import com.example.pcarstore.ModelsDB.Order;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.R;
import com.example.pcarstore.Services.CardValidatorService;
import com.example.pcarstore.Services.OrderManager;
import com.example.pcarstore.Services.PaymentService;
import com.example.pcarstore.Services.ShippingCostCalculator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CarritoFragment extends Fragment implements CartAdapter.OnCartItemListener, CartAdapter.OnCartUpdatedListener, CreditCardPaymentDialog.CreditCardPaymentListener {

    private RecyclerView rvCartItems;
    private TextView tvItemCount, tvTotal;
    private Button btnProceedToCheckout;

    private CartAdapter cartAdapter;
    private List<OrderItem> cartItems;
    private Cart currentCart;

    // Referencias a la base de datos
    private FirebaseAuth mAuth;
    private DatabaseReference cartRef;
    private DatabaseReference userBalanceRef;
    private DatabaseReference ordersRef;
    private double shippingCost = 0.0;
    private boolean isPrimeMember = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carrito, container, false);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // Inicializar referencias a la base de datos
        cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId);
        userBalanceRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("saldo");
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        // Inicializar vistas
        rvCartItems = view.findViewById(R.id.rvCartItems);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnProceedToCheckout = view.findViewById(R.id.btnProceedToCheckout);

        // Configurar RecyclerView
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(getContext(), this, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(cartAdapter);

        loadCart();

        btnProceedToCheckout.setOnClickListener(v -> proceedToCheckout());
        loadUserDataForShipping();


        return view;
    }
    private void loadUserDataForShipping() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        if (userId.isEmpty()) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Obtener membresía Prime
                Boolean prime = snapshot.child("membresiaPrime").getValue(Boolean.class);
                isPrimeMember = prime != null && prime;

                // Obtener ubicación y calcular envío
                String departamento = snapshot.child("departamento").getValue(String.class);
                String ciudad = snapshot.child("ciudad").getValue(String.class);

                calculateShippingCost(departamento, ciudad);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Usar valores por defecto en caso de error
                calculateShippingCost("", "");
            }
        });
    }
    private void calculateShippingCost(String departamento, String ciudad) {
        ShippingCostCalculator.calculateShippingCost(
                departamento,
                ciudad,
                isPrimeMember,
                currentCart != null ? currentCart.getTotal() : 0.0,
                cost -> {
                    shippingCost = cost;
                    updateCartSummary(currentCart != null ? currentCart.getTotal() : 0.0);
                }
        );
    }
    @Override
    public void updateCartSummary(double subtotal) {
        // Aplicar descuento por compra mayor a $50
        double finalShippingCost = subtotal > 500 ? shippingCost * 0.5 : shippingCost;
        double total = subtotal + finalShippingCost;

        tvTotal.setText(String.format("$%.2f", total));

        if (currentCart != null) {
            currentCart.setTotal(subtotal);
        }
    }
    private void loadCart() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentCart = snapshot.getValue(Cart.class);
                if (currentCart == null) {
                    currentCart = new Cart(mAuth.getCurrentUser().getUid());
                }

                cartItems.clear();
                if (currentCart.getItems() != null) {
                    cartItems.addAll(currentCart.getItems().values());
                }

                cartAdapter.updateCartItems(cartItems);
                updateCartSummary(currentCart.getTotal());

                int itemCount = currentCart.getItems() != null ? currentCart.getItems().size() : 0;
                tvItemCount.setText(itemCount + (itemCount == 1 ? " artículo" : " artículos"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar carrito: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void proceedToCheckout() {
        if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
            Toast.makeText(getContext(), "Tu carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Debes iniciar sesión para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderItem> orderItems = new ArrayList<>(currentCart.getItems().values());
        showPaymentDialog(orderItems);
    }
    private void showPaymentDialog(List<OrderItem> orderItems) {
        double cartTotal = currentCart.getTotal();

        PaymentConfirmationDialog dialog = new PaymentConfirmationDialog(
                orderItems,
                cartTotal,
                new PaymentConfirmationDialog.PaymentConfirmationListener() {
                    @Override
                    public void onPaymentConfirmed(double discountApplied) {
                        processOrderPayment(cartTotal - discountApplied);
                    }

                    @Override
                    public void onPaymentCancelled() {
                        Toast.makeText(getContext(), "Pago cancelado", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onInsufficientBalance(double missingAmount) {
                        showAddBalanceDialog(missingAmount);
                    }
                });

        dialog.show(getChildFragmentManager(), "PaymentConfirmationDialog");
    }
    private void processOrderPayment(double amountToPay) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Procesando pago...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Use runTransaction to ensure atomic operation
        userBalanceRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Double currentBalance = mutableData.getValue(Double.class);
                if (currentBalance == null) {
                    currentBalance = 0.0;
                }

                if (currentBalance < amountToPay) {
                    // Mark transaction as aborted but still valid
                    return Transaction.success(mutableData);
                }

                // Update balance
                mutableData.setValue(currentBalance - amountToPay);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed,
                                   @Nullable DataSnapshot currentData) {
                if (error != null) {
                    progressDialog.dismiss();
                    showError("Error en la transacción: " + error.getMessage());
                    return;
                }

                Double updatedBalance = currentData.getValue(Double.class);
                Double initialBalance = updatedBalance != null ? updatedBalance + amountToPay : amountToPay;

                if (initialBalance < amountToPay) {
                    // Insufficient balance
                    progressDialog.dismiss();
                    double missingAmount = amountToPay - initialBalance;
                    showAddBalanceDialog(missingAmount);
                } else if (committed) {
                    // Successfully updated balance, now create the order
                    createOrder(updatedBalance, amountToPay);
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    showError("Error al procesar pago");
                }
            }
        });
    }
    private void createOrder(double newBalance, double amountPaid) {
        Order order = new Order(
                mAuth.getCurrentUser().getUid(),
                "completada",
                amountPaid
        );

        if (currentCart.getItems() != null) {
            order.setItems(new HashMap<>(currentCart.getItems()));
        }

        OrderManager.getInstance().placeOrder(order, new OrderManager.OrderCallback() {
            @Override
            public void onSuccess(String orderId) {
                showSuccess(String.format(Locale.getDefault(),
                        "Orden #%s creada. Saldo restante: %.2f €",
                        orderId, newBalance));
                clearCart();
            }

            @Override
            public void onFailure(String error) {
                showError("Error al crear orden: " + error);
            }
        });
    }
    private void showAddBalanceDialog(double missingAmount) {
        if (getContext() == null || mAuth.getCurrentUser() == null) return;

        ProgressDialog loadingDialog = new ProgressDialog(getContext());
        loadingDialog.setMessage("Verificando saldo...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        userBalanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingDialog.dismiss();
                Double currentBalance = snapshot.getValue(Double.class);
                if (currentBalance == null) currentBalance = 0.0;

                if (currentBalance >= missingAmount) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Saldo suficiente")
                            .setMessage(String.format(Locale.getDefault(),
                                    "Tienes %.2f € disponibles. ¿Deseas continuar con la compra?",
                                    currentBalance))
                            .setPositiveButton("Continuar", (dialog, which) ->
                                    processOrderPayment(missingAmount))
                            .setNegativeButton("Cancelar", null)
                            .show();
                } else {
                    double remainingAmount = missingAmount - currentBalance;
                    new AlertDialog.Builder(getContext())
                            .setTitle("Saldo insuficiente")
                            .setMessage(String.format(Locale.getDefault(),
                                    "Necesitas %.2f € más (Tienes %.2f €). ¿Deseas agregar saldo ahora?",
                                    remainingAmount, currentBalance))
                            .setPositiveButton("Agregar Saldo", (dialog, which) ->
                                    showCreditCardPaymentDialog(remainingAmount))
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
                showError("Error al verificar saldo: " + error.getMessage());
            }
        });
    }
    private void showCreditCardPaymentDialog(double amountToAdd) {
        CreditCardPaymentDialog dialog = CreditCardPaymentDialog.newInstance(amountToAdd);
        dialog.show(getChildFragmentManager(), "CreditCardPaymentDialog");
    }
    private void processCardPayment(double amount) {
        // Primero validar datos de tarjeta (ejemplo)
        CardValidatorService.CardValidationResult validation =
                CardValidatorService.validateCard(
                        "4111111111111111", // Número de tarjeta de prueba
                        "12/25",            // Fecha expiración
                        "123",              // CVV
                        "Juan Perez"        // Nombre
                );

        if (!validation.isValid) {
            showError(validation.errorMessage);
            return;
        }

        PaymentService.processCardPayment(
                getContext(),
                userBalanceRef,
                amount,
                new PaymentService.PaymentCallback() {
                    @Override
                    public void onSuccess(double newBalance) {
                        showSuccess(String.format(Locale.getDefault(),
                                "Se agregaron %.2f $ a tu cuenta. Saldo total: %.2f $",
                                amount, newBalance));
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        showError(errorMessage);
                    }
                }
        );
    }
    private void clearCart() {
        Cart emptyCart = new Cart(mAuth.getCurrentUser().getUid());

        cartRef.setValue(emptyCart)
                .addOnSuccessListener(aVoid -> {
                    cartItems.clear();
                    cartAdapter.updateCartItems(cartItems);
                    updateCartSummary(0.0);
                    tvItemCount.setText("0 artículos");
                    btnProceedToCheckout.setText("Proceder al pago (0 artículos)");
                })
                .addOnFailureListener(e -> showError("Error al vaciar carrito: " + e.getMessage()));
    }
    // Métodos auxiliares
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void showSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
    // Implementación de interfaces del adaptador
    @Override
    public void onIncreaseQuantity(String productId, int newQuantity) {
        updateItemQuantity(productId, newQuantity);
    }
    @Override
    public void onDecreaseQuantity(String productId, int newQuantity) {
        updateItemQuantity(productId, newQuantity);
    }

    @Override
    public void onRemoveItem(String productId) {
        removeItemFromCart(productId);
    }
    private void updateItemQuantity(String productId, int newQuantity) {
        if (currentCart.getItems() != null && currentCart.getItems().containsKey(productId)) {
            if (newQuantity <= 0) {
                removeItemFromCart(productId);
            } else {
                currentCart.getItems().get(productId).setQuantity(newQuantity);
                currentCart.calculateTotal();
                cartRef.setValue(currentCart);
            }
        }
    }
    private void removeItemFromCart(String productId) {
        currentCart.removeItem(productId);
        cartRef.setValue(currentCart);
    }
    @Override
    public void onPaymentConfirmed(String cardName, String cardNumber, String expiry, String cvv, double amount) {
        processCardPayment(amount);
    }
    @Override
    public void onPaymentCancelled() {
        Toast.makeText(getContext(), "Pago cancelado", Toast.LENGTH_SHORT).show();
    }
}