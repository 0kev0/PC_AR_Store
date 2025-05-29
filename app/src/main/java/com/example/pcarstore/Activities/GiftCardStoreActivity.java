package com.example.pcarstore.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pcarstore.Adapters.ShopGiftCardAdapter;
import com.example.pcarstore.Dialogs.CreditCardPaymentDialog;
import com.example.pcarstore.ModelsDB.GiftCard;
import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GiftCardStoreActivity extends AppCompatActivity implements CreditCardPaymentDialog.CreditCardPaymentListener{

    private RecyclerView rvGiftCards;
    private ShopGiftCardAdapter adapter;
    private List<GiftCard> giftCardList = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_card_store);

        rvGiftCards = findViewById(R.id.rvGiftCards);
        setupRecyclerView();
        loadGiftCardsFromFirebase();
    }
    private void setupRecyclerView() {
        adapter = new ShopGiftCardAdapter(giftCardList, giftCard -> {
            showRecipientSelectionDialog(giftCard);
        });
        rvGiftCards.setLayoutManager(new LinearLayoutManager(this));
        rvGiftCards.setAdapter(adapter);
        if(giftCardList.isEmpty()) {
            Toast.makeText(this, "No hay gift cards disponibles", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadGiftCardsFromFirebase() {
        showProgressDialog();
        Query query = FirebaseDatabase.getInstance()
                .getReference("giftCards")
                .orderByChild("amount"); // Ordenar por amount directamente en Firebase

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hideProgressDialog();
                giftCardList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        GiftCard giftCard = dataSnapshot.getValue(GiftCard.class);
                        if (giftCard != null) {
                            giftCard.setCardId(dataSnapshot.getKey());
                            giftCardList.add(giftCard);
                        }
                    } catch (Exception e) {
                        Log.e("Firebase", "Error parsing gift card", e);
                    }
                }

                adapter.notifyDataSetChanged();

                if (giftCardList.isEmpty()) {
                    Toast.makeText(GiftCardStoreActivity.this,
                            "No hay giftcards disponibles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
                Toast.makeText(GiftCardStoreActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }    private void showRecipientSelectionDialog(GiftCard giftCard) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Para quién es la Gift Card?");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user != null ? user.getEmail() : "tu correo";

        builder.setMessage(String.format(Locale.getDefault(),
                "Estás comprando una gift card de $%.2f. ¿Para quién será?",
                giftCard.getAmount()));

        builder.setPositiveButton("Para mí", (dialog, which) -> {
            processPaymentAndAssignGiftCard(giftCard, null);
        });

        builder.setNegativeButton("Para otra persona", (dialog, which) -> {
            showEmailInputDialog(giftCard);
        });

        builder.setNeutralButton("Cancelar", null);

        builder.show();
    }
    private void showEmailInputDialog(GiftCard giftCard) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enviar a otra persona");

        final EditText emailInput = new EditText(this);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setHint("Ingrese el email del destinatario");

        builder.setView(emailInput);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (isValidEmail(email)) {
                processPaymentAndAssignGiftCard(giftCard, email);
            } else {
                Toast.makeText(this, "Por favor ingrese un email válido", Toast.LENGTH_SHORT).show();
                showEmailInputDialog(giftCard);
            }
        });

        builder.setNegativeButton("Cancelar", null);

        builder.show();
    }
    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) &&
                Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    private void processPaymentAndAssignGiftCard(GiftCard giftCard, String recipientEmail) {
        showProgressDialog();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            hideProgressDialog();
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null) {
                    hideProgressDialog();
                    return;
                }

                if (user.getSaldo() < giftCard.getAmount()) {
                    hideProgressDialog();
                    showInsufficientBalanceDialog(giftCard, recipientEmail, user.getSaldo());
                    return;
                }

                completeGiftCardPurchase(userRef, user, giftCard, recipientEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressDialog();
                Log.e("Payment", "Error reading user data", error.toException());
            }
        });
    }
    private void showInsufficientBalanceDialog(GiftCard giftCard, String recipientEmail, double currentBalance) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Saldo insuficiente");
        builder.setMessage(String.format(Locale.getDefault(),
                "Necesitas $%.2f más para comprar esta gift card. ¿Deseas recargar saldo ahora?",
                giftCard.getAmount() - currentBalance));

        builder.setPositiveButton("Recargar", (dialog, which) -> {
            showCreditCardPaymentDialog(giftCard, recipientEmail);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            Toast.makeText(GiftCardStoreActivity.this,
                    "Compra cancelada", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }
    private void showCreditCardPaymentDialog(GiftCard giftCard, String recipientEmail) {
        double currentBalance = getCurrentUserBalance();
        double amountNeeded = giftCard.getAmount() - currentBalance;

        if (amountNeeded <= 0) {
            processPaymentAndAssignGiftCard(giftCard, recipientEmail);
            return;
        }

        CreditCardPaymentDialog dialog = CreditCardPaymentDialog.newInstance(amountNeeded);
        dialog.setListener(this);
        showDialogSafely(dialog, "GiftCardPaymentDialog");
    }
    private void updateUserBalance(double amountAdded) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(user.getUid());

        userRef.child("balance").setValue(getCurrentUserBalance() + amountAdded)
                .addOnFailureListener(e -> Log.e("Balance", "Error updating balance", e));
    }
    private void showDialogSafely(DialogFragment dialog, String tag) {
        try {
            if (!isFinishing() && !isDestroyed()) {
                dialog.show(getSupportFragmentManager(), tag);
            }
        } catch (IllegalStateException e) {
            Log.e("Dialog", "Error showing dialog", e);
        }
    }
    private double getCurrentUserBalance() {
        // Implementa según tu estructura de datos
        return 0.0; // Valor temporal
    }
    private void completeGiftCardPurchase(DatabaseReference userRef, User user,
                                          GiftCard giftCard, String recipientEmail) {
        double newBalance = user.getSaldo() - giftCard.getAmount();
        userRef.child("saldo").setValue(newBalance)
                .addOnSuccessListener(aVoid -> {
                    registerTransaction(user.getUserId(), -giftCard.getAmount(),
                            "giftcard_purchase", "Compra de Gift Card");
                    createAndAssignGiftCard(giftCard, recipientEmail, user.getEmail());
                })
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    Toast.makeText(GiftCardStoreActivity.this,
                            "Error en el pago", Toast.LENGTH_SHORT).show();
                    Log.e("Payment", "Error updating balance", e);
                });
    }
    private void createAndAssignGiftCard(GiftCard giftCard, String recipientEmail, String creatorEmail) {
        String finalRecipient = (recipientEmail == null) ? creatorEmail : recipientEmail;
        giftCard.setRecipientEmail(finalRecipient);
        giftCard.setCreatedBy(creatorEmail);
        giftCard.setCreationDate(new Date());
        giftCard.setStatus("active");

        if (giftCard.getCode() == null) {
            giftCard.setCode(generateGiftCardCode());
        }

        DatabaseReference giftCardRef = FirebaseDatabase.getInstance()
                .getReference("giftCards")
                .push();

        giftCard.setCardId(giftCardRef.getKey());

        Map<String, Object> giftCardMap = giftCard.toMap();

        giftCardRef.setValue(giftCardMap)
                .addOnSuccessListener(aVoid -> {
                    hideProgressDialog();

                    Toast.makeText(GiftCardStoreActivity.this,
                            "Compra exitosa!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    Log.e("Payment", "Error saving gift card", e);
                    revertPayment(creatorEmail, giftCard.getAmount());
                });
        giftCardRef.setValue(giftCardMap)
                .addOnSuccessListener(aVoid -> {
                    if (recipientEmail != null) {
                        addBalanceToRecipient(recipientEmail, giftCard.getAmount());
                    }
                    hideProgressDialog();
                    Toast.makeText(this, "¡Gift Card enviada!", Toast.LENGTH_SHORT).show();
                });
    }
    private void addBalanceToRecipient(String recipientEmail, double amount) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = usersRef.orderByChild("email").equalTo(recipientEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User recipient = userSnapshot.getValue(User.class);
                        if (recipient != null) {
                            double newBalance = recipient.getSaldo() + amount;
                            userSnapshot.getRef().child("saldo").setValue(newBalance);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al buscar destinatario", error.toException());
            }
        });
    }
    private String generateGiftCardCode() {
        // Implementa tu lógica de generación de código
        return "PDM-" + System.currentTimeMillis();
    }
    private void registerTransaction(String userId, double amount,
                                     String type, String description) {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .push();

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("userId", userId);
        transaction.put("amount", amount);
        transaction.put("type", type);
        transaction.put("description", description);
        transaction.put("date", ServerValue.TIMESTAMP);
        transaction.put("status", "completed");

        transactionsRef.setValue(transaction)
                .addOnFailureListener(e -> Log.e("Payment", "Error saving transaction", e));
    }
    private void revertPayment(String userEmail, double amount) {}
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Procesando...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }
    @Override
    public void onPaymentConfirmed(String cardName, String cardNumber, String expiry, String cvv, double amount) {
        updateUserBalance(amount);
    }
    @Override
    public void onPaymentCancelled() {
        Toast.makeText(this, "Pago cancelado", Toast.LENGTH_SHORT).show();
    }
}