package com.example.pcarstore.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pcarstore.Adapters.GiftCardAdapter;
import com.example.pcarstore.R;
import com.example.pcarstore.ModelsDB.GiftCard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Gift_CardsFragment extends Fragment implements GiftCardAdapter.OnGiftCardActionsListener {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private GiftCardAdapter adapter;
    private List<GiftCard> giftCards = new ArrayList<>();
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public Gift_CardsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializar Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gift__cards, container, false);

        recyclerView = view.findViewById(R.id.recycler_gift_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GiftCardAdapter(giftCards, getContext(), this);
        recyclerView.setAdapter(adapter);

        loadUserGiftCards();

        Button btn_new_gift = view.findViewById(R.id.add_new_gift_card);
        btn_new_gift.setOnClickListener(v -> showCreateGiftCardDialog());

        return view;
    }

    private void showCreateGiftCardDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_giftcard, null);

        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etCurrency = dialogView.findViewById(R.id.et_currency);
        EditText etExpiryDays = dialogView.findViewById(R.id.et_expiry_days);
        EditText etRecipientEmail = dialogView.findViewById(R.id.et_recipient_email);

        new AlertDialog.Builder(getContext())
                .setTitle("Crear nueva Gift Card")
                .setView(dialogView)
                .setPositiveButton("Crear", (dialog, which) -> {
                    try {
                        double amount = Double.parseDouble(etAmount.getText().toString());
                        String currency = etCurrency.getText().toString();
                        int expiryDays = Integer.parseInt(etExpiryDays.getText().toString());
                        String recipientEmail = etRecipientEmail.getText().toString();

                        if (amount <= 0) {
                            Toast.makeText(getContext(), "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (expiryDays <= 0) {
                            Toast.makeText(getContext(), "Los días de validez deben ser mayores a 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        createNewGiftCard(amount, currency, expiryDays, recipientEmail);

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Ingrese valores válidos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private void createNewGiftCard(double amount, String currency, int expiryDays, String recipientEmail) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Debes iniciar sesión para crear gift cards", Toast.LENGTH_SHORT).show();
            return;
        }

        String cardId = generateUniqueId();
        String code = generateRandomCode();
        Date creationDate = new Date();
        Date expirationDate = calculateExpirationDate(expiryDays);

        GiftCard giftCard = new GiftCard();
        giftCard.setCardId(cardId);
        giftCard.setCode(code);
        giftCard.setAmount(amount);
        giftCard.setCreationDate(creationDate);
        giftCard.setExpirationDate(expirationDate);
        giftCard.setStatus("ACTIVE");
        giftCard.setCreatedBy(currentUser.getUid());
        giftCard.setRecipientEmail(recipientEmail.isEmpty() ? null : recipientEmail);

        mDatabase.child("giftCards").child(cardId)
                .setValue(giftCard.toMap())
                .addOnSuccessListener(aVoid -> {
                    String message = String.format("Gift Card creada!\nCódigo: %s", code);
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    Log.d("GiftCards", "Nueva gift card creada: " + code);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al crear gift card: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("GiftCards", "Error al crear gift card", e);
                });
    }

    private void loadUserGiftCards() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        mDatabase.child("giftCards")
                .orderByChild("createdBy")
                .equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<GiftCard> loadedGiftCards = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            GiftCard giftCard = GiftCard.fromSnapshot(dataSnapshot);
                            loadedGiftCards.add(giftCard);
                        }
                        giftCards.clear();
                        giftCards.addAll(loadedGiftCards);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error al cargar gift cards: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("GiftCards", "Error al cargar datos", error.toException());
                    }
                });
    }


    private String generateUniqueId() {
        return "GC-" + System.currentTimeMillis();
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            code.append(chars.charAt((int)(Math.random() * chars.length())));
        }
        return code.toString();
    }

    private Date calculateExpirationDate(int days) {
        long daysInMillis = days * 24L * 60 * 60 * 1000;
        return new Date(System.currentTimeMillis() + daysInMillis);
    }

    @Override
    public void onEditGiftCard(GiftCard giftCard) {
        showEditGiftCardDialog(giftCard);
    }

    @Override
    public void onDeleteGiftCard(GiftCard giftCard) {
        showExpireConfirmationDialog(giftCard);
    }

    private void showEditGiftCardDialog(GiftCard giftCard) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_giftcard, null);

        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etExpiryDate = dialogView.findViewById(R.id.et_expiry_date);
        EditText etStatus = dialogView.findViewById(R.id.et_status);

        // Configurar valores iniciales
        etAmount.setText(String.valueOf(giftCard.getAmount()));
        etExpiryDate.setText(dateFormatter.format(giftCard.getExpirationDate()));
        etStatus.setText(giftCard.getStatus());

        // DatePicker para la fecha de expiración
        etExpiryDate.setOnClickListener(v -> showDatePicker(etExpiryDate));

        new AlertDialog.Builder(getContext())
                .setTitle("Editar Gift Card")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    try {
                        double newAmount = Double.parseDouble(etAmount.getText().toString());
                        String newStatus = etStatus.getText().toString();
                        Date newExpiryDate = dateFormatter.parse(etExpiryDate.getText().toString());

                        if (newAmount <= 0) {
                            Toast.makeText(getContext(), "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        updateGiftCard(giftCard.getCardId(), newAmount, newExpiryDate, newStatus);

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error en los datos ingresados", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showDatePicker(EditText etExpiryDate) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etExpiryDate.setText(dateFormatter.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateGiftCard(String cardId, double newAmount, Date newExpiryDate, String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("amount", newAmount);
        updates.put("expirationDate", newExpiryDate.getTime());
        updates.put("status", newStatus);

        mDatabase.child("giftCards").child(cardId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Gift Card actualizada", Toast.LENGTH_SHORT).show();
                    Log.d("GiftCards", "Gift Card actualizada: " + cardId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("GiftCards", "Error al actualizar", e);
                });
    }

    private void showExpireConfirmationDialog(GiftCard giftCard) {
        new AlertDialog.Builder(getContext())
                .setTitle("Marcar como vencida")
                .setMessage("¿Estás seguro de marcar esta Gift Card como vencida?")
                .setPositiveButton("Marcar", (dialog, which) -> expireGiftCard(giftCard.getCardId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void expireGiftCard(String cardId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "VENCIDA");
        updates.put("expirationDate", new Date().getTime()); // Opcional: actualizar fecha a hoy

        mDatabase.child("giftCards").child(cardId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Gift Card marcada como vencida", Toast.LENGTH_SHORT).show();
                    Log.d("GiftCards", "Gift Card vencida: " + cardId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("GiftCards", "Error al marcar como vencida", e);
                });
    }
}
