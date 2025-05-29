package com.example.pcarstore.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pcarstore.Adapters.GiftCardAdapter;
import com.example.pcarstore.Dialogs.CreateGiftCardDialog;
import com.example.pcarstore.Dialogs.EditGiftCardDialog;
import com.example.pcarstore.Dialogs.ExpireGiftCardDialog;
import com.example.pcarstore.ModelsDB.GiftCard;
import com.example.pcarstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Gift_CardsFragment extends Fragment implements
        GiftCardAdapter.OnGiftCardActionsListener,
        CreateGiftCardDialog.OnGiftCardCreatedListener,
        EditGiftCardDialog.OnGiftCardUpdatedListener,
        ExpireGiftCardDialog.OnGiftCardExpiredListener {
    /*************************************************************VARIABLES******************************************************************************************/
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private GiftCardAdapter adapter;
    private final List<GiftCard> giftCards = new ArrayList<>();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gift_cards, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_gift_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GiftCardAdapter(giftCards, getContext(), this);
        recyclerView.setAdapter(adapter);

        loadUserGiftCards();

        Button btn_new_gift = view.findViewById(R.id.add_new_gift_card);
        btn_new_gift.setOnClickListener(v -> showCreateGiftCardDialog());

        return view;
    }

    private void showCreateGiftCardDialog() {
        CreateGiftCardDialog.show(requireContext(),
                mDatabase.child("giftCards"),
                this);
    }

    private void loadUserGiftCards() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        mDatabase.child("giftCards")
                .orderByChild("createdBy")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("GiftCardInfo", "Gift cards encontradas: " + snapshot.getChildrenCount());

                        List<GiftCard> loadedGiftCards = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            GiftCard giftCard = GiftCard.fromSnapshot(dataSnapshot);
                            if (giftCard != null) {
                                loadedGiftCards.add(giftCard);
                            }
                        }

                        giftCards.clear();
                        giftCards.addAll(loadedGiftCards);
                        adapter.notifyDataSetChanged();

                        Log.d("GiftCardInfo", "Mostrando " + giftCards.size() + " gift cards");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("GiftCardError", "Error: " + error.getMessage());
                        Toast.makeText(getContext(), "Error al cargar gift cards", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEditGiftCard(GiftCard giftCard) {
        EditGiftCardDialog.show(requireContext(),
                giftCard,
                mDatabase.child("giftCards"),
                dateFormatter,
                this);
    }

    @Override
    public void onDeleteGiftCard(GiftCard giftCard) {
        ExpireGiftCardDialog.show(requireContext(),
                giftCard,
                mDatabase.child("giftCards"),
                this);
    }

    @Override
    public void onGiftCardCreated(GiftCard giftCard) {
    }

    @Override
    public void onCreationError(String error) {
    }

    @Override
    public void onGiftCardUpdated() {
    }

    @Override
    public void onUpdateError(String error) {
    }

    @Override
    public void onGiftCardExpired() {
    }

    @Override
    public void onExpireError(String error) {
    }

}