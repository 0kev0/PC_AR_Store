package com.example.pcarstore.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pcarstore.Adapters.OrdersAdapter;
import com.example.pcarstore.Dialogs.OrderDetailsDialog;
import com.example.pcarstore.ModelsDB.Order;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView emptyView;
    private OrdersAdapter adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_usuarios);

        // Configurar toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        // Inicializar vistas
        RecyclerView ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(this::onOrderClicked);
        ordersRecyclerView.setAdapter(adapter);

        loadUserOrders();
    }

    private void loadUserOrders() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        String currentUserId = mAuth.getCurrentUser().getUid();

        ordersRef.orderByChild("userId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Order> orders = new ArrayList<>();

                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            Order order = parseOrderSnapshot(orderSnapshot);
                            if (order != null) {
                                orders.add(order);
                            }
                        }

                        // Ordenar por fecha (más reciente primero)
                        Collections.sort(orders, (o1, o2) -> Long.compare(o2.getDate().getTime(), o1.getDate().getTime()));

                        adapter.setOrders(orders);
                        progressBar.setVisibility(View.GONE);

                        if (orders.isEmpty()) {
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(OrdersActivity.this, "Error al cargar pedidos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        emptyView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private Order parseOrderSnapshot(DataSnapshot orderSnapshot) {
        try {
            Order order = new Order();
            order.setOrderId(orderSnapshot.getKey());
            order.setUserId(orderSnapshot.child("userId").getValue(String.class));
            order.setStatus(orderSnapshot.child("status").getValue(String.class));
            order.setTotal(orderSnapshot.child("total").getValue(Double.class));

            // Parsear fecha de orden
            Long timestamp = orderSnapshot.child("date").getValue(Long.class);
            if (timestamp != null) {
                order.setDate(new Date(timestamp));
            }

            // Parsear fecha de entrega
            Long deliveryTimestamp = orderSnapshot.child("deliveryDate").getValue(Long.class);
            if (deliveryTimestamp != null) {
                order.setDeliveryDate(new Date(deliveryTimestamp));
            } else if (timestamp != null) {
                // Si no hay fecha de entrega pero sí hay fecha de orden, calcular automáticamente
                order.setDate(new Date(timestamp)); // Esto activará el cálculo automático de la fecha de entrega
            }

            // Parsear items
            Map<String, OrderItem> items = new HashMap<>();
            DataSnapshot itemsSnapshot = orderSnapshot.child("items");
            for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                OrderItem item = new OrderItem();
                item.setProductId(itemSnapshot.child("productId").getValue(String.class));
                item.setProductName(itemSnapshot.child("productName").getValue(String.class));
                item.setPrice(itemSnapshot.child("price").getValue(Double.class));
                item.setQuantity(itemSnapshot.child("quantity").getValue(Integer.class));
                items.put(itemSnapshot.getKey(), item);
            }
            order.setItems(items);

            return order;
        } catch (Exception e) {
            Log.e("OrdersActivity", "Error parsing order", e);
            return null;
        }
    }

    private void onOrderClicked(Order order) {
        // Aquí puedes implementar la navegación al detalle de la orden
        Toast.makeText(this, "Pedido seleccionado: " + order.getOrderId(), Toast.LENGTH_SHORT).show();

        OrderDetailsDialog dialog = new OrderDetailsDialog(order);
        dialog.show(getSupportFragmentManager(), "OrderDetailsDialog");

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}