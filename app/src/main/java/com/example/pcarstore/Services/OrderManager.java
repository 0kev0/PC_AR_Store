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

    private OrderManager() {
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
    }

    public static synchronized OrderManager getInstance() {
        if (instance == null) {
            instance = new OrderManager();
        }
        return instance;
    }

    public void placeOrder(Order order) {
        String orderId = ordersRef.push().getKey();
        order.setOrderId(orderId);

        // Convertir a mapa para Firebase
        Map<String, Object> orderValues = new HashMap<>();
        orderValues.put("orderId", orderId);
        orderValues.put("userId", order.getUserId());
        orderValues.put("date", order.getDate().getTime());
        orderValues.put("status", order.getStatus());
        orderValues.put("total", order.getTotal());

        // Convertir items
        Map<String, Object> itemsMap = new HashMap<>();
        for (Map.Entry<String, OrderItem> entry : order.getItems().entrySet()) {
            OrderItem item = entry.getValue();
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProductId());
            itemMap.put("productName", item.getProductName());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemsMap.put(entry.getKey(), itemMap);
        }
        orderValues.put("items", itemsMap);

        ordersRef.child(orderId).setValue(orderValues);
    }
}
