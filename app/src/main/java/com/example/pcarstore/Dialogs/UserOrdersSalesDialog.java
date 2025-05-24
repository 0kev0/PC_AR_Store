package com.example.pcarstore.Dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.OrderSalesAdapter;
import com.example.pcarstore.ModelsDB.Order;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserOrdersSalesDialog extends DialogFragment {
    private static final String TAG = "UserOrdersDialog";
    private static final String ARG_USER_ID = "user_id";

    private DatabaseReference ordersRef;
    private ValueEventListener ordersListener;
    private OrderSalesAdapter adapter;

    public static UserOrdersSalesDialog newInstance(String userId) {
        UserOrdersSalesDialog fragment = new UserOrdersSalesDialog();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_user_orders_sales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userId = getArguments().getString(ARG_USER_ID);
        RecyclerView recyclerView = view.findViewById(R.id.rvOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new OrderSalesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        setupRealtimeListener(userId);
    }

    private void setupRealtimeListener(String userId) {
        Log.d(TAG, "Configurando listener en tiempo real para user: " + userId);

        Query userOrdersQuery = ordersRef.orderByChild("userId").equalTo(userId);

        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Map<String, Object>> ordersList = new ArrayList<>();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    try {
                        Map<String, Object> orderMap = new HashMap<>();

                        // Agregar solo los datos necesarios para mostrar
                        orderMap.put("orderId", orderSnapshot.getKey());
                        orderMap.put("status", orderSnapshot.child("status").getValue(String.class));
                        orderMap.put("total", orderSnapshot.child("total").getValue(Double.class));

                        ordersList.add(orderMap);

                        // Log para depuración
                        Log.d(TAG, "Orden agregada: ID=" + orderSnapshot.getKey() +
                                ", Estado=" + orderMap.get("status") +
                                ", Total=" + orderMap.get("total"));

                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando orden: " + orderSnapshot.getKey(), e);
                    }
                }

                // Actualizar el adaptador con la lista simplificada
                adapter.updateOrders(ordersList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error en listener: " + error.getMessage(), error.toException());
                Toast.makeText(requireContext(), "Error en conexión: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        userOrdersQuery.addValueEventListener(ordersListener);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ordersRef != null && ordersListener != null) {
            Log.d(TAG, "Removiendo listener de Firebase");
            ordersRef.removeEventListener(ordersListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}