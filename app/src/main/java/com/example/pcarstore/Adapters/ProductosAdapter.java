package com.example.pcarstore.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ProductViewHolder> {
    private List<Product> productList = new ArrayList<>();
    private final OnProductActionsListener listener;
    private final DatabaseReference productsRef;
    private DatabaseReference wishlistRef;
    private final FirebaseAuth mAuth;

    public interface OnProductActionsListener {
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
        void onWishlistUpdated(Product product, boolean isInWishlist);
    }

    public ProductosAdapter(OnProductActionsListener listener) {
        this.listener = listener;
        this.mAuth = FirebaseAuth.getInstance();
        this.productsRef = FirebaseDatabase.getInstance().getReference("products");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            this.wishlistRef = FirebaseDatabase.getInstance().getReference("wishlist").child(userId);
        }

        loadProducts();
    }

    public void setProducts(List<Product> products) {
        this.productList = new ArrayList<>(products);
        notifyDataSetChanged();
        checkWishlistStatus();
    }

    private void loadProducts() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.setProductId(productSnapshot.getKey());
                        productList.add(product);
                    }
                }
                notifyDataSetChanged();
                checkWishlistStatus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void checkWishlistStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || wishlistRef == null) {
            for (Product product : productList) {
                product.setInWishlist(false);
            }
            notifyDataSetChanged();
            return;
        }

        wishlistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (Product product : productList) {
                    boolean isInWishlist = snapshot.hasChild(product.getProductId());
                    product.setInWishlist(isInWishlist);
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, listener);
    }

    private void addToWishlist(Product product) {
        if (wishlistRef == null) return;

        wishlistRef.child(product.getProductId()).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    product.setInWishlist(true);
                    notifyItemChanged(productList.indexOf(product));
                    if (listener != null) {
                        listener.onWishlistUpdated(product, true);
                    }
                });
    }

    private void removeFromWishlist(Product product) {
        if (wishlistRef == null) return;

        wishlistRef.child(product.getProductId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    product.setInWishlist(false);
                    notifyItemChanged(productList.indexOf(product));
                    if (listener != null) {
                        listener.onWishlistUpdated(product, false);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productStock;
        private final TextView productPrice;
        private final TextView productCategory;
        private final ImageView icHeart;
        private final Button btnEdit;
        private final Button btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productStock = itemView.findViewById(R.id.productStock);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCategory = itemView.findViewById(R.id.productCategory);
            icHeart = itemView.findViewById(R.id.ic_heart);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Product product, OnProductActionsListener listener) {
            productName.setText(product.getName());
            productPrice.setText(String.format("$%.2f", product.getPrice()));
            productStock.setText(product.getStock() + " unidades");

            if (product.getCategory() != null) {
                productCategory.setText(product.getCategory());
            }

            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageUrls().get(0))
                        .placeholder(R.drawable.ic_products)
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.ic_products);
            }

            updateWishlistIcon(product.isInWishlist());

            btnEdit.setOnClickListener(v -> listener.onEditProduct(product));
            btnDelete.setOnClickListener(v -> listener.onDeleteProduct(product));

            icHeart.setOnClickListener(v -> {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    if (listener != null) {
                        listener.onWishlistUpdated(product, false);
                    }
                    return;
                }

                if (product.isInWishlist()) {
                    removeFromWishlist(product);
                } else {
                    addToWishlist(product);
                }
            });
        }

        public void updateWishlistIcon(boolean isInWishlist) {
            if (isInWishlist) {
                icHeart.setImageResource(R.drawable.ic_heart);
                icHeart.setColorFilter(Color.RED);
            } else {
                icHeart.setImageResource(R.drawable.ic_unfill_heart);
                icHeart.setColorFilter(Color.GRAY);
            }
        }
    }

}