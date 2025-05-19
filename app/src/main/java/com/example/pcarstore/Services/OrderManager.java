package com.example.pcarstore.Services;

import com.example.pcarstore.ModelsDB.Order;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class OrderManager {
    private static OrderManager instance;
    private DatabaseReference ordersRef;

    public interface OrderCallback {
        void onSuccess(String orderId);
        void onFailure(String error);
    }

    private OrderManager() {
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
    }

    public static synchronized OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }

    public void placeOrder(Order order, OrderCallback callback) {
        String orderId = ordersRef.push().getKey();
        if (orderId == null) {
            callback.onFailure("No se pudo generar ID de orden");
            return;
        }

        order.setOrderId(orderId);

        Map<String, Object> orderValues = orderToMap(order);

        ordersRef.child(orderId).setValue(orderValues)
                .addOnSuccessListener(aVoid -> callback.onSuccess(orderId))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private Map<String, Object> orderToMap(Order order) {
        Map<String, Object> orderValues = new HashMap<>();
        orderValues.put("orderId", order.getOrderId());
        orderValues.put("userId", order.getUserId());
        //variables de delivery
        orderValues.put("date", order.getDate().getTime());
        orderValues.put("deliveryDate", order.getDeliveryDate().getTime());
        orderValues.put("status", order.getStatus());
        orderValues.put("total", order.getTotal());

        // Convertir items
        Map<String, Object> itemsMap = new HashMap<>();
        if (order.getItems() != null) {
            for (Map.Entry<String, OrderItem> entry : order.getItems().entrySet()) {
                OrderItem item = entry.getValue();
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", item.getProductId());
                itemMap.put("productName", item.getProductName());
                itemMap.put("price", item.getPrice());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("totalPrice", item.getTotalPrice());
                itemsMap.put(entry.getKey(), itemMap);
            }
        }
        orderValues.put("items", itemsMap);

        return orderValues;
    }

    public void updateOrderStatus(String orderId, String newStatus, OrderCallback callback) {
        ordersRef.child(orderId).child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> callback.onSuccess(orderId))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateDeliveryDate(String orderId, long deliveryTimestamp, OrderCallback callback) {
        ordersRef.child(orderId).child("deliveryDate").setValue(deliveryTimestamp)
                .addOnSuccessListener(aVoid -> callback.onSuccess(orderId))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}