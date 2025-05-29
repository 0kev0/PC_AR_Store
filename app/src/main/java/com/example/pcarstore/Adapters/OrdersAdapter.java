package com.example.pcarstore.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.ModelsDB.Order;
import com.example.pcarstore.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    /*************************************************************VARIABLES******************************************************************************************/
    private List<Order> orders;
    private final OnOrderClickListener listener;
    private final SimpleDateFormat dateFormat;
    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrdersAdapter(OnOrderClickListener listener) {
        this.orders = new ArrayList<>();
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId;
        private final TextView tvStatus;
        private final TextView tvOrderDate;
        private final TextView tvDeliveryDate;
        private final TextView tvItemsCount;
        private final TextView tvTotalAmount;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvDeliveryDate = itemView.findViewById(R.id.tvDeliveryDate);
            tvItemsCount = itemView.findViewById(R.id.tvItemsCount);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            Button btnViewDetails = itemView.findViewById(R.id.btnViewDetails);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOrderClick(orders.get(position));
                }
            });

            btnViewDetails.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOrderClick(orders.get(position));
                }
            });
        }

        public void bind(Order order) {
            Context context = itemView.getContext();

            // Configurar ID de orden
            tvOrderId.setText(context.getString(R.string.order_id_format,
                    order.getOrderId().substring(0, 8).toUpperCase()));

            // Formatear fecha de orden
            if (order.getDate() != null) {
                tvOrderDate.setText(dateFormat.format(order.getDate()));
            } else {
                tvOrderDate.setText(context.getString(R.string.not_available));
            }

            // Formatear fecha de entrega
            if (order.getDeliveryDate() != null) {
                tvDeliveryDate.setText(dateFormat.format(order.getDeliveryDate()));
            } else {
                tvDeliveryDate.setText(context.getString(R.string.not_available));
            }

            // Configurar total
            tvTotalAmount.setText(context.getString(R.string.price_format, order.getTotal()));

            // Configurar conteo de artículos
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            tvItemsCount.setText(context.getResources().getQuantityString(
                    R.plurals.items_count, itemCount, itemCount));


        }
    }

}