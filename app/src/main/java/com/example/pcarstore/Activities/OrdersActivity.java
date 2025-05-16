package com.example.pcarstore.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pcarstore.Adapters.OrdersAdapter;
import com.example.pcarstore.ModelsDB.Order;
import com.example.pcarstore.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
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

        // Inicializar vistas
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        // Configurar RecyclerView
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(this::onOrderClicked);
        ordersRecyclerView.setAdapter(adapter);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        // Cargar órdenes
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
                            Order order = orderSnapshot.getValue(Order.class);
                            if (order != null) {
                                order.setOrderId(orderSnapshot.getKey());

                                // Convertir timestamp de Firebase a Date si es necesario
                                if (order.getDate() == null && orderSnapshot.child("date").getValue() != null) {
                                    long timestamp = (long) orderSnapshot.child("date").getValue();
                                    order.setDate(new Date(timestamp));
                                }

                                orders.add(order);
                            }
                        }

                        // Ordenar por fecha (más reciente primero)
                        Collections.sort(orders, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));

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
                        Toast.makeText(OrdersActivity.this,
                                "Error al cargar pedidos: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onOrderClicked(Order order) {
        Toast.makeText(this, "Pedido seleccionado: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}