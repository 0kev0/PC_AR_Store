package com.example.pcarstore.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.DiscountCodesAdapter;
import com.example.pcarstore.ModelsDB.DiscountCode;
import com.example.pcarstore.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DiscountCodesFragment extends Fragment implements DiscountCodesAdapter.OnDiscountCodeActionsListener {

    private RecyclerView recyclerView;
    private DiscountCodesAdapter adapter;
    private List<DiscountCode> discountCodes = new ArrayList<>();
    private DatabaseReference databaseReference;
    private Button fabAddCode;
    private FirebaseAuth mAuth;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discount_codes, container, false);

        mAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.rv_gift_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DiscountCodesAdapter(discountCodes, getContext(), this);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("discountCodes");
        fabAddCode = view.findViewById(R.id.button_add_code);
        fabAddCode.setOnClickListener(v -> showAddCodeDialog());

        loadDiscountCodes();

        return view;
    }

    @Override
    public void onEditCode(DiscountCode code) {
        showEditCodeDialog(code);
    }

    @Override
    public void onDeactivateCode(DiscountCode code) {
        new AlertDialog.Builder(getContext())
                .setTitle("Desactivar código")
                .setMessage("¿Estás seguro de marcar este código como inactivo?")
                .setPositiveButton("Desactivar", (dialog, which) -> deactivateCode(code))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showEditCodeDialog(DiscountCode code) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_discount_code, null);

        EditText etCode = dialogView.findViewById(R.id.et_code);
        EditText etDiscount = dialogView.findViewById(R.id.et_discount);
        EditText etExpiryDate = dialogView.findViewById(R.id.et_expiry_date);

        // Configurar valores actuales
        etCode.setText(code.getCode());
        etDiscount.setText(String.valueOf(code.getDiscountPercentage()));
        if (code.getExpirationDate() != null) {
            etExpiryDate.setText(dateFormat.format(code.getExpirationDate()));
        }

        // Date picker para fecha de expiración
        Calendar calendar = Calendar.getInstance();
        if (code.getExpirationDate() != null) {
            calendar.setTime(code.getExpirationDate());
        }

        etExpiryDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        etExpiryDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Editar código de descuento")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    try {
                        String newCode = etCode.getText().toString();
                        double discountPercentage = Double.parseDouble(etDiscount.getText().toString());
                        String expiryDate = etExpiryDate.getText().toString();

                        if (newCode.isEmpty() || expiryDate.isEmpty()) {
                            Toast.makeText(getContext(), "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (discountPercentage <= 0 || discountPercentage > 100) {
                            Toast.makeText(getContext(), "El descuento debe ser entre 0.1% y 100%", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        updateDiscountCode(code, newCode, discountPercentage, expiryDate);

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Ingrese valores válidos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateDiscountCode(DiscountCode originalCode, String newCode,
                                    double discountPercentage, String expiryDate) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("code", newCode);
        updates.put("discountPercentage", discountPercentage);

        try {
            Date expirationDate = dateFormat.parse(expiryDate);
            updates.put("expirationDate", expirationDate);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(originalCode.getCodeId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                        "Código actualizado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showAddCodeDialog() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Debes iniciar sesión para crear códigos", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_discount_code, null);

        EditText etCode = dialogView.findViewById(R.id.et_code);
        EditText etDiscount = dialogView.findViewById(R.id.et_discount);
        EditText etExpiryDate = dialogView.findViewById(R.id.et_expiry_date);

        etCode.setText("DISC" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());

        Calendar calendar = Calendar.getInstance();
        etExpiryDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        etExpiryDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Crear nuevo código de descuento")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    try {
                        String code = etCode.getText().toString();
                        double discountPercentage = Double.parseDouble(etDiscount.getText().toString());
                        String expiryDate = etExpiryDate.getText().toString();

                        if (code.isEmpty() || expiryDate.isEmpty()) {
                            Toast.makeText(getContext(), "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (discountPercentage <= 0 || discountPercentage > 100) {
                            Toast.makeText(getContext(), "El descuento debe ser entre 0.1% y 100%", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        createDiscountCode(code, discountPercentage, expiryDate, currentUser.getUid());

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Ingrese valores válidos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createDiscountCode(String code, double discountPercentage,
                                    String expiryDate, String createdBy) {
        String codeId = databaseReference.push().getKey();
        Date creationDate = new Date();

        Date expirationDate;
        try {
            expirationDate = dateFormat.parse(expiryDate);
        } catch (Exception e) {
            expirationDate = new Date(creationDate.getTime() + (30L * 24 * 60 * 60 * 1000));
        }

        DiscountCode discountCode = new DiscountCode();
        discountCode.setCodeId(codeId);
        discountCode.setCode(code);
        discountCode.setDiscountPercentage(discountPercentage);
        discountCode.setCreationDate(creationDate);
        discountCode.setExpirationDate(expirationDate);
        discountCode.setStatus("active");
        discountCode.setCreatedBy(createdBy);

        databaseReference.child(codeId).setValue(discountCode)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                        "Código creado: " + code, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Error al crear código: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deactivateCode(DiscountCode code) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "inactive");
        updates.put("usedDate", new Date().getTime());

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updates.put("usedBy", currentUser.getUid());
        }

        databaseReference.child(code.getCodeId()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                        "Código desactivado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Error al desactivar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadDiscountCodes() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                discountCodes.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DiscountCode code = dataSnapshot.getValue(DiscountCode.class);
                    if (code != null) {
                        if ("active".equals(code.getStatus()) &&
                                code.getExpirationDate() != null &&
                                code.getExpirationDate().before(new Date())) {
                            code.setStatus("expired");
                            databaseReference.child(code.getCodeId()).child("status").setValue("expired");
                        }
                        discountCodes.add(code);
                    }
                }
                adapter.updateData(discountCodes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar códigos: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}