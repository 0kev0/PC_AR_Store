package com.example.pcarstore.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.ModelsDB.Order;
import com.example.pcarstore.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrdersAdapter(OnOrderClickListener listener) {
        this.orders = new ArrayList<>();
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView orderIdText, dateText, totalText, statusText;
        private ImageView statusIcon;


        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            dateText = itemView.findViewById(R.id.dateText);
            totalText = itemView.findViewById(R.id.totalText);
            statusText = itemView.findViewById(R.id.statusText);
            statusIcon = itemView.findViewById(R.id.statusIcon);
        }

        public void bind(Order order) {
            orderIdText.setText("Pedido #" + order.getOrderId().substring(0, 8).toUpperCase());

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            dateText.setText(sdf.format(order.getDate()));

            totalText.setText(String.format(Locale.getDefault(), "Total: $%.2f", order.getTotal()));

            // Configurar estado con colores diferentes
            switch (order.getStatus().toLowerCase()) {
                case "completed":
                    statusText.setText("Completado");
                    statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.green));
                    statusIcon.setImageResource(R.drawable.ic_check_circle);
                    break;
                case "cancelled":
                    statusText.setText("Cancelado");
                    statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.red));
                    statusIcon.setImageResource(R.drawable.ic_cancel);
                    break;
                case "processing":
                    statusText.setText("En proceso");
                    statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.orange));
                    statusIcon.setImageResource(R.drawable.ic_pending);
                    break;
                default:
                    statusText.setText("Pendiente");
                    statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.blue));
                    statusIcon.setImageResource(R.drawable.ic_processing);
            }
        }
    }
}