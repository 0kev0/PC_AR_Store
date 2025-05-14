package com.example.pcarstore.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
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

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productPrice;
        private final RatingBar productRating;
        private final Button viewDetailsButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productRating = itemView.findViewById(R.id.productRating);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }

        public void bind(Product product, OnProductClickListener listener) {
            // Configurar datos básicos
            productName.setText(product.getName());
            productPrice.setText(String.format("$%.2f", product.getPrice()));

            // Cargar imagen
            Glide.with(itemView.getContext())
                    .load(product.getMainImageUrl())
                    .placeholder(R.drawable.placer_holder)
                    .into(productImage);

            // Configurar clics
            viewDetailsButton.setOnClickListener(v -> listener.onProductClick(product));
            itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
    }

    public void updateProductList(List<Product> newProductList) {
        productList = newProductList;
        notifyDataSetChanged();
    }
}