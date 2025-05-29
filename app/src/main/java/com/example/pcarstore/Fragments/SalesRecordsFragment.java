package com.example.pcarstore.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.UserSalesAdapter;
import com.example.pcarstore.Dialogs.UserOrdersSalesDialog;
import com.example.pcarstore.ModelsDB.UserSales;
import com.example.pcarstore.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.TuplesKt;

public class SalesRecordsFragment extends Fragment {
   private RecyclerView recyclerView;
    private UserSalesAdapter adapter;
    private final List<UserSales> userSalesList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sales_records, container, false);

        initializeViews(view);
        setupAdapter();
        setupFilterSpinner(view);
        loadSalesData();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.rvUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupAdapter() {
        adapter = new UserSalesAdapter(userSalesList, requireContext());
        recyclerView.setAdapter(adapter);

        adapter.setOnUserClickListener(userId -> {
            Toast.makeText(requireContext(), "Mostrando órdenes de " + userId, Toast.LENGTH_SHORT).show();
           try {
                if (isAdded() && !isDetached()) {
                    UserOrdersSalesDialog dialog = UserOrdersSalesDialog.newInstance(userId);

                    FragmentManager fragmentManager = getParentFragmentManager();

                    dialog.show(fragmentManager, "UserOrdersSalesDialog");

                    Log.d("DialogDebug", "Mostrando diálogo para: " + userId);
                    Toast.makeText(requireContext(), "Mostrando órdenes de " + userId,
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("DialogError", "Error al mostrar diálogo", e);
                Toast.makeText(requireContext(), "Error al mostrar órdenes",
                        Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void setupFilterSpinner(View view) {
        Spinner filterSpinner = view.findViewById(R.id.spinnerFilter);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.filter_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean sortDescending = position == 0;
                adapter.updateData(new ArrayList<>(userSalesList), sortDescending);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSalesData() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, UserSales> userSalesMap = new HashMap<>();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    processOrder(orderSnapshot, userSalesMap);
                }

                updateAdapterWithNewData(userSalesMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al cargar órdenes: " + error.getMessage());
            }
        });
    }

    private void processOrder(DataSnapshot orderSnapshot, Map<String, UserSales> userSalesMap) {
        String userId = orderSnapshot.child("userId").getValue(String.class);
        Double total = orderSnapshot.child("total").getValue(Double.class);
        String status = orderSnapshot.child("status").getValue(String.class);

        if (userId != null && total != null && "completed".equalsIgnoreCase(status)) {
            userSalesMap.computeIfAbsent(userId, UserSales::new).addOrder(total);
        }
    }

    private void updateAdapterWithNewData(Map<String, UserSales> userSalesMap) {
        userSalesList.clear();
        userSalesList.addAll(userSalesMap.values());
        adapter.updateData(new ArrayList<>(userSalesList), true);
    }

}