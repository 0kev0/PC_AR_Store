package com.example.pcarstore.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.R;

import java.util.List;
import java.util.Map;

public class OrderSalesAdapter extends RecyclerView.Adapter<OrderSalesAdapter.OrderViewHolder> {
    /*************************************************************VARIABLES******************************************************************************************/
    private List<Map<String, Object>> orders;
    public OrderSalesAdapter(List<Map<String, Object>> orders) {
        this.orders = orders;
    }
    public void updateOrders(List<Map<String, Object>> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_sales, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Map<String, Object> order = orders.get(position);

        holder.tvOrderId.setText((String) order.get("orderId"));
        holder.tvStatus.setText((String) order.get("status"));

        Double total = (Double) order.get("total");
        holder.tvTotal.setText(String.format("$%.2f", total != null ? total : 0.0));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotal = itemView.findViewById(R.id.tvTotalH);
        }
    }

}