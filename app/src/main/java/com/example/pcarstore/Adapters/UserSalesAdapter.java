package com.example.pcarstore.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.ModelsDB.UserSales;
import com.example.pcarstore.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UserSalesAdapter extends RecyclerView.Adapter<UserSalesAdapter.UserViewHolder> {

    private List<UserSales> userSalesList;
    private Context context;

    public interface OnUserClickListener {
        void onUserClick(String userId);
    }

    private OnUserClickListener listener;

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    public UserSalesAdapter(List<UserSales> userSalesList, Context context) {
        this.userSalesList = userSalesList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_sales, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserSales userSales = userSalesList.get(position);

        // Mostrar ID de usuario temporalmente
        holder.tvUserId.setText(userSales.getUserId());
        holder.tvOrderCount.setText(String.format("Pedidos: %d", userSales.getOrderCount()));
        holder.tvTotalAmount.setText(String.format(Locale.getDefault(), "Total: $%.2f", userSales.getTotalAmount()));

        // Opcional: Obtener nombre real del usuario desde Firebase
        loadUserName(userSales.getUserId(), holder.tvUserId);

        // Configurar el click listener para el elemento
        holder.itemView.setOnClickListener(v -> {
            showUserDetailsDialog(userSales);
        });
    }

    private void showUserDetailsDialog(UserSales userSales) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Detalles del Usuario");

        // Inflar un layout personalizado si lo deseas
        View dialogView = LayoutInflater.from(context).inflate(R.layout.item_user_sales, null);

        TextView tvDialogUserId = dialogView.findViewById(R.id.tvOrderId);
        TextView tvDialogOrderCount = dialogView.findViewById(R.id.tvStatus);
        TextView tvDialogTotalAmount = dialogView.findViewById(R.id.tvTotal);

        tvDialogUserId.setText("Usuario: " + userSales.getUserId());
        tvDialogOrderCount.setText("Total pedidos: " + userSales.getOrderCount());
        tvDialogTotalAmount.setText(String.format(Locale.getDefault(), "Monto total: $%.2f", userSales.getTotalAmount()));

        builder.setView(dialogView);
        builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loadUserName(String userId, TextView textView) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    if (userName != null && !userName.isEmpty()) {
                        textView.setText(userName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al cargar nombre de usuario: " + error.getMessage());
            }
        });
    }

    public void updateData(List<UserSales> newList, boolean sortDescending) {
        Collections.sort(newList, (o1, o2) -> sortDescending ?
                Double.compare(o2.getTotalAmount(), o1.getTotalAmount()) :
                Double.compare(o1.getTotalAmount(), o2.getTotalAmount()));

        this.userSalesList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userSalesList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserId, tvOrderCount, tvTotalAmount;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvOrderCount = itemView.findViewById(R.id.tvOrderCount);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
        }
    }
}