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
import com.example.pcarstore.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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

                        orderMap.put("orderId", orderSnapshot.getKey());
                        orderMap.put("status", orderSnapshot.child("status").getValue(String.class));
                        orderMap.put("total", orderSnapshot.child("total").getValue(Double.class));

                        ordersList.add(orderMap);

                        Log.d(TAG, "Orden agregada: ID=" + orderSnapshot.getKey() +
                                ", Estado=" + orderMap.get("status") +
                                ", Total=" + orderMap.get("total"));

                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando orden: " + orderSnapshot.getKey(), e);
                    }
                }

                adapter.updateOrders(ordersList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error en listener: " + error.getMessage(), error.toException());
                Toast.makeText(requireContext(), "Error en conexión: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        userOrdersQuery.addValueEventListener(ordersListener);
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