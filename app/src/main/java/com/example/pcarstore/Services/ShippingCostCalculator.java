package com.example.pcarstore.Services;

import androidx.annotation.NonNull;

import com.example.pcarstore.ModelsDB.Departamento;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ShippingCostCalculator {
    public static void calculateShippingCost(String departamento, String ciudad, boolean isPrimeMember, double cartTotal, ShippingCostCallback callback) {

        // Prime members get free shipping
        if (isPrimeMember) {
            callback.onCostCalculated(0.0);
            return;
        }

        // Default shipping costs based on cart total
        if (departamento == null || departamento.isEmpty() ||
                ciudad == null || ciudad.isEmpty()) {
            double cost = cartTotal > 50 ? 2.99 : 5.99;
            callback.onCostCalculated(cost);
            return;
        }

        // Get shipping costs from database
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
                            // Calculate cost based on department model
                            double cost = dept.calcularCostoEnvio(ciudad);

                            // Apply discount for purchases over 50€
                            if (cartTotal > 50) {
                                cost *= 0.5; // 50% discount
                            }

                            callback.onCostCalculated(cost);
                            return;
                        }
                    }
                }

                // Department not found - use default
                double cost = cartTotal > 50 ? 2.99 : 5.99;
                callback.onCostCalculated(cost);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Use default on error
                double cost = cartTotal > 50 ? 2.99 : 5.99;
                callback.onCostCalculated(cost);
            }
        });
    }
    public interface ShippingCostCallback {
        void onCostCalculated(double cost);
    }
}