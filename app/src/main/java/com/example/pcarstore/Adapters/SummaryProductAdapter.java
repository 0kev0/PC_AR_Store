package com.example.pcarstore.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pcarstore.ModelsDB.OrderItem;
import com.example.pcarstore.R;

import java.util.List;
import java.util.Locale;

public class SummaryProductAdapter extends RecyclerView.Adapter<SummaryProductAdapter.SummaryProductViewHolder> {

    private List<OrderItem> orderItems;

    public SummaryProductAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public SummaryProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_summary_product, parent, false);
        return new SummaryProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryProductViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);

        holder.tvProductName.setText(item.getProductName());
        holder.tvProductPrice.setText(String.format(Locale.getDefault(), "Precio: %.2f €", item.getPrice()));
        holder.tvProductQuantity.setText(String.format(Locale.getDefault(), "Cantidad: %d", item.getQuantity()));
        holder.tvProductSubtotal.setText(String.format(Locale.getDefault(), "%.2f €", item.getTotalPrice()));

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .into(holder.ivProductImage);
        }
    }

    @Override
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    public static class SummaryProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductQuantity, tvProductSubtotal;

        public SummaryProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvProductSubtotal = itemView.findViewById(R.id.tv_product_subtotal);
        }
    }
}
