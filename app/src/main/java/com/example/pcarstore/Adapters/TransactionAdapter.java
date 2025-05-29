package com.example.pcarstore.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.ModelsDB.Transaction;
import com.example.pcarstore.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private final Context context;
    private final List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.tvTransactionType.setText(transaction.getReadableType());
        if (transaction.getType().contains("purchase")) {
            holder.tvTransactionAmount.setText(String.format(Locale.getDefault(), "-$%.2f", transaction.getAmount()));
            holder.tvTransactionAmount.setTextColor(context.getResources().getColor(R.color.red_700));
        } else {
            holder.tvTransactionAmount.setText(String.format(Locale.getDefault(), "+$%.2f", transaction.getAmount()));
            holder.tvTransactionAmount.setTextColor(context.getResources().getColor(R.color.green_700));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault());
        holder.tvTransactionDate.setText(sdf.format(new Date(transaction.getDate())));

        holder.tvTransactionDescription.setText(transaction.getDescription());
        holder.tvTransactionUser.setText("Usuario: " + transaction.getUserId());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionType, tvTransactionAmount, tvTransactionDate,
                tvTransactionDescription, tvTransactionUser;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionType = itemView.findViewById(R.id.tvTransactionType);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionDescription = itemView.findViewById(R.id.tvTransactionDescription);
            tvTransactionUser = itemView.findViewById(R.id.tvTransactionUser);
        }
    }

}