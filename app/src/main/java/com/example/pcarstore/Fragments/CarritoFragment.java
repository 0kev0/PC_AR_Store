package com.example.pcarstore.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.CartAdapter;
import com.example.pcarstore.Dialogs.PaymentConfirmationDialog;
import com.example.pcarstore.ModelsDB.Cart;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CarritoFragment extends Fragment implements CartAdapter.OnCartItemListener, CartAdapter.OnCartUpdatedListener {

    private RecyclerView rvCartItems;
    private TextView tvItemCount, tvSubtotal, tvShipping, tvTotal;
    private Button btnProceedToCheckout;

    private CartAdapter cartAdapter;
    private List<OrderItem> cartItems;

    private FirebaseAuth mAuth;
    private DatabaseReference cartRef;
    private Cart currentCart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carrito, container, false);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId);

        // Inicializar vistas
        rvCartItems = view.findViewById(R.id.rvCartItems);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvShipping = view.findViewById(R.id.tvShipping);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnProceedToCheckout = view.findViewById(R.id.btnProceedToCheckout);

        // Configurar RecyclerView
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(getContext(), this, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(cartAdapter);

        // Cargar carrito
        loadCart();

        // Configurar botón de checkout
        btnProceedToCheckout.setOnClickListener(v -> proceedToCheckout());

        return view;
    }

    private void loadCart() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentCart = snapshot.getValue(Cart.class);
                if (currentCart == null) {
                    currentCart = new Cart(mAuth.getCurrentUser().getUid());
                }

                // Convertir Map a List para el adapter
                cartItems.clear();
                if (currentCart.getItems() != null) {
                    cartItems.addAll(currentCart.getItems().values());
                }

                cartAdapter.updateCartItems(cartItems);
                updateCartSummary(currentCart.getTotal());

                // Actualizar UI
                int itemCount = currentCart.getItems() != null ? currentCart.getItems().size() : 0;
                tvItemCount.setText(itemCount + (itemCount == 1 ? " artículo" : " artículos"));
                btnProceedToCheckout.setText("Proceder al pago (" + itemCount + " artículos)");
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

        // Convertir el Map de items a List<OrderItem>
        List<OrderItem> orderItems = new ArrayList<>(currentCart.getItems().values());
        showPaymentDialog(orderItems); // Ahora pasas la lista correctamente
    }

    private void showPaymentDialog(List<OrderItem> orderItems) {
        // Calcular el total del carrito
        double cartTotal = 0;
        for (OrderItem item : orderItems) {
            cartTotal += item.getTotalPrice();
        }

        PaymentConfirmationDialog dialog = new PaymentConfirmationDialog(
                orderItems,
                cartTotal,
                new PaymentConfirmationDialog.PaymentConfirmationListener() {

                    @Override
                    public void onPaymentConfirmed(double discountApplied) {
                        // Limpiar el carrito después de pago exitoso
                        clearCart();
                        Toast.makeText(getContext(),
                                String.format(Locale.getDefault(),
                                        "Pago exitoso! Descuento: %.2f €", discountApplied),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPaymentCancelled() {
                        // Lógica cuando se cancela
                        Toast.makeText(getContext(), "Pago cancelado", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onInsufficientBalance(double missingAmount) {
                        // Mostrar diálogo para agregar saldo
                        showAddBalanceDialog(missingAmount);
                    }
                });

        dialog.show(getChildFragmentManager(), "PaymentConfirmationDialog");
    }

    private void clearCart() {
        if (mAuth.getCurrentUser() != null && currentCart != null) {
            // Crear un nuevo carrito vacío
            Cart emptyCart = new Cart(mAuth.getCurrentUser().getUid());

            // Actualizar en Firebase
            cartRef.setValue(emptyCart)
                    .addOnSuccessListener(aVoid -> {
                        // Actualizar UI
                        cartItems.clear();
                        cartAdapter.updateCartItems(cartItems);
                        updateCartSummary(0.0);
                        tvItemCount.setText("0 artículos");
                        btnProceedToCheckout.setText("Proceder al pago (0 artículos)");

                        Toast.makeText(getContext(), "Carrito vaciado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(),
                                "Error al vaciar carrito: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showAddBalanceDialog(double missingAmount) {
        new AlertDialog.Builder(getContext())
                .setTitle("Saldo insuficiente")
                .setMessage(String.format(Locale.getDefault(),
                        "Necesitas %.2f € más para completar esta compra. ¿Deseas agregar saldo ahora?",
                        missingAmount))
                .setPositiveButton("Agregar Saldo", (dialog, which) -> {
                    // Navegar a la pantalla de agregar saldo
                   // navigateToAddBalanceScreen(missingAmount);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void updateCartSummary(double subtotal) {
        double shipping = subtotal > 50 ? 0.0 : 0.00;
        double total = subtotal + shipping;

        tvSubtotal.setText(String.format("$%.2f", subtotal));
        tvShipping.setText(String.format("$%.2f", shipping));
        tvTotal.setText(String.format("$%.2f", total));

        // Actualizar total en el carrito
        if (currentCart != null) {
            currentCart.setTotal(subtotal);
        }
    }

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
        if (currentCart != null && currentCart.getItems() != null) {
            OrderItem item = currentCart.getItems().get(productId);
            if (item != null) {
                if (newQuantity <= 0) {
                    removeItemFromCart(productId);
                } else {
                    item.setQuantity(newQuantity);
                    currentCart.calculateTotal();
                    cartRef.setValue(currentCart);
                }
            }
        }
    }

    private void removeItemFromCart(String productId) {
        if (currentCart != null && currentCart.getItems() != null) {
            currentCart.removeItem(productId);
            cartRef.setValue(currentCart);
        }
    }
}