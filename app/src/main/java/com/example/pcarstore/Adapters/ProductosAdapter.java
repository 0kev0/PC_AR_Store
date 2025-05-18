package com.example.pcarstore.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.common.subtyping.qual.Bottom;

import java.util.ArrayList;
import java.util.List;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ProductViewHolder> {

    private List<Product> productList = new ArrayList<>();
    private final OnProductActionsListener listener;
    private final DatabaseReference productsRef;

    public interface OnProductActionsListener {
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
    }

    public ProductosAdapter(OnProductActionsListener listener) {
        this.listener = listener;
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        loadProducts();
    }

    // Método para actualizar datos desde el Fragment
    public void setProducts(List<Product> products) {
        this.productList = new ArrayList<>(products);
        notifyDataSetChanged();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productStock;
        private final TextView productPrice;
        private final TextView productCategory;
        private final Button btnEdit;
        private final Button btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productStock = itemView.findViewById(R.id.productStock);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCategory = itemView.findViewById(R.id.productCategory);
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
            if (product.getDescription() != null) {
            }

            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageUrls())
                        .placeholder(R.drawable.ic_products)
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.ic_products);
            }

            btnEdit.setOnClickListener(v -> listener.onEditProduct(product));
            btnDelete.setOnClickListener(v -> listener.onDeleteProduct(product));
        }
    }
}
