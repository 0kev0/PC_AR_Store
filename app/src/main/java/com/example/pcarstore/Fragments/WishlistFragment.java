package com.example.pcarstore.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.Adapters.WishlistAdapter;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {
    private RecyclerView rvWishlistItems;
    private WishlistAdapter wishlistAdapter;
    private List<Product> wishlistProducts;
    private TextView tvWishlistItemCount;
    private Button btnClearWishlist;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference wishlistRef;
    private DatabaseReference productsRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Inicializar vistas
        rvWishlistItems = view.findViewById(R.id.rvWishlistItems);
        tvWishlistItemCount = view.findViewById(R.id.tvWishlistItemCount);
        btnClearWishlist = view.findViewById(R.id.btnClearWishlist);

        // Configurar RecyclerView
        wishlistProducts = new ArrayList<>();
        wishlistAdapter = new WishlistAdapter(getContext(), wishlistProducts, this::removeItemFromWishlist);
        rvWishlistItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWishlistItems.setAdapter(wishlistAdapter);

        // Configurar botón de limpiar
        btnClearWishlist.setOnClickListener(v -> clearWishlist());

        // Cargar datos si el usuario está autenticado
        if (currentUser != null) {
            setupFirebaseReferences();
            loadWishlistData();
        } else {
            showLoginMessage();
        }

        return view;
    }

    private void setupFirebaseReferences() {
        wishlistRef = FirebaseDatabase.getInstance().getReference()
                .child("wishlist")
                .child(currentUser.getUid());

        productsRef = FirebaseDatabase.getInstance().getReference("products");
    }

    private void loadWishlistData() {
        wishlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistProducts.clear();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    String productId = productSnapshot.getKey();
                    fetchProductDetails(productId);
                }

                updateItemCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("WishlistFragment", "Error loading wishlist", error.toException());
                Toast.makeText(getContext(), "Error al cargar la lista de deseos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProductDetails(String productId) {
        productsRef.child(productId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            product.setProductId(productId);
                            product.setInWishlist(true); // Marcar como en wishlist

                            // Evitar duplicados
                            if (!wishlistProducts.contains(product)) {
                                wishlistProducts.add(product);
                                wishlistAdapter.notifyDataSetChanged();
                                updateItemCount();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("WishlistFragment", "Error loading product", error.toException());
                    }
                }
        );
    }

    private void removeItemFromWishlist(Product product) {
        if (currentUser != null && wishlistRef != null) {
            wishlistRef.child(product.getProductId())
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        wishlistProducts.remove(product);
                        wishlistAdapter.notifyDataSetChanged();
                        updateItemCount();
                        Toast.makeText(getContext(), "Producto eliminado de favoritos", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al eliminar de favoritos", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void clearWishlist() {
        if (currentUser != null && wishlistRef != null) {
            wishlistRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        wishlistProducts.clear();
                        wishlistAdapter.notifyDataSetChanged();
                        updateItemCount();
                        Toast.makeText(getContext(), "Lista de deseos vaciada", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al vaciar la lista", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateItemCount() {
        if (tvWishlistItemCount != null) {
            tvWishlistItemCount.setText(wishlistProducts.size() + " artículos");
        }
    }

    private void showLoginMessage() {
        Toast.makeText(getContext(), "Inicia sesión para ver tu lista de deseos", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar listeners si es necesario
    }
}