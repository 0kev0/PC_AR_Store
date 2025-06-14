package com.example.pcarstore.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pcarstore.ModelsDB.Product;
import com.example.pcarstore.R;
import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {
    private final Context context;
    private final List<Product> wishlistProducts;
    private final OnWishlistActionListener listener;

    public interface OnWishlistActionListener {
        void onRemoveItem(Product product);
    }

    public WishlistAdapter(Context context, List<Product> wishlistProducts, OnWishlistActionListener listener) {
        this.context = context;
        this.wishlistProducts = wishlistProducts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = wishlistProducts.get(position);

        holder.productName.setText(product.getName());
        holder.productPrice.setText("$" + product.getPrice());
        Glide.with(context).load(product.getMainImageUrl()).into(holder.productImage);

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItem(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlistProducts.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < wishlistProducts.size()) {
            wishlistProducts.remove(position);
            notifyItemRemoved(position);
            // No es necesario notifyItemRangeChanged si usas notifyDataSetChanged
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;
        ImageView productImage, btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImage = itemView.findViewById(R.id.productImage);
            btnRemove = itemView.findViewById(R.id.btnRemove);

        }
    }
}