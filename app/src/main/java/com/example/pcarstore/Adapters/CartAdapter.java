package com.example.pcarstore.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemListener {
        void onIncreaseQuantity(String productId, int newQuantity);
        void onDecreaseQuantity(String productId, int newQuantity);
        void onRemoveItem(String productId);
    }

    private Context context;
    private List<OrderItem> cartItems;
    private OnCartUpdatedListener listener;
    private OnCartItemListener cartItemListener;

    public interface OnCartUpdatedListener {
        void updateCartSummary(double subtotal);
    }

    public CartAdapter(Context context, OnCartUpdatedListener listener, OnCartItemListener cartItemListener) {
        this.context = context;
        this.cartItems = new ArrayList<>(); // Inicializar con lista vacía
        this.listener = listener;
        this.cartItemListener = cartItemListener;
    }

    public void updateCartItems(List<OrderItem> newItems) {
        if (newItems != null) {
            this.cartItems = new ArrayList<>(newItems); // Copia defensiva
        } else {
            this.cartItems = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        OrderItem item = cartItems.get(position);

        // Configurar vistas con el item
        holder.tvProductName.setText(item.getProductName());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.limo)
                    .into(holder.ivProductImage);
        }

        // Configurar listeners
        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            if (cartItemListener != null) {
                cartItemListener.onIncreaseQuantity(item.getProductId(), newQuantity);
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() - 1;
            if (newQuantity >= 1 && cartItemListener != null) {
                cartItemListener.onDecreaseQuantity(item.getProductId(), newQuantity);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (cartItemListener != null) {
                cartItemListener.onRemoveItem(item.getProductId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0; // Protección contra null
    }

    public double calculateSubtotal() {
        if (cartItems == null || cartItems.isEmpty()) {
            return 0.0;
        }

        double subtotal = 0.0;
        for (OrderItem item : cartItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        return subtotal;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvPrice, tvQuantity;
        ImageView btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}