package com.example.pcarstore.Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.ModelsDB.DiscountCode;
import com.example.pcarstore.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DiscountCodesAdapter extends RecyclerView.Adapter<DiscountCodesAdapter.DiscountCodeViewHolder> {

    public interface OnDiscountCodeActionsListener {
        void onDeactivateCode(DiscountCode code);
        void onEditCode(DiscountCode code); // Nuevo método para editar
    }

    private List<DiscountCode> discountCodes;
    private final Context context;
    private final OnDiscountCodeActionsListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public DiscountCodesAdapter(List<DiscountCode> discountCodes, Context context,
                                OnDiscountCodeActionsListener listener) {
        this.discountCodes = discountCodes;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiscountCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gift_discount, parent, false);
        return new DiscountCodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscountCodeViewHolder holder, int position) {
        DiscountCode code = discountCodes.get(position);

        holder.tvCode.setText(code.getCode());
        holder.tvDiscount.setText(String.format(Locale.getDefault(),
                "%.1f%% de descuento", code.getDiscountPercentage()));
        holder.tvMinPurchase.setText(String.format(Locale.getDefault(),
                "Mín. compra: $%.2f", code.getMinPurchaseRequired()));

        if (code.getExpirationDate() != null) {
            holder.tvExpiry.setText("Válido hasta: " + dateFormat.format(code.getExpirationDate()));
        }

        setupStatusChip(holder, code);
        setupButtons(holder, code);
    }

    private void setupStatusChip(DiscountCodeViewHolder holder, DiscountCode code) {
        switch (code.getStatus()) {
            case "active":
                holder.chipStatus.setText("ACTIVO");
                holder.chipStatus.setChipBackgroundColorResource(R.color.green);
                holder.chipStatus.setTextColor(context.getColor(R.color.green_dark));
                holder.btnDeactivate.setVisibility(View.VISIBLE);
                break;
            case "used":
                holder.chipStatus.setText("USADO");
                holder.chipStatus.setChipBackgroundColorResource(R.color.amazon_blue);
                holder.chipStatus.setTextColor(context.getColor(R.color.blue));
                holder.btnDeactivate.setVisibility(View.GONE);
                break;
            case "expired":
                holder.chipStatus.setText("EXPIRADO");
                holder.chipStatus.setChipBackgroundColorResource(R.color.gray_light);
                holder.chipStatus.setTextColor(context.getColor(R.color.gray_dark));
                holder.btnDeactivate.setVisibility(View.GONE);
                break;
            case "inactive":
                holder.chipStatus.setText("INACTIVO");
                holder.chipStatus.setChipBackgroundColorResource(R.color.red);
                holder.chipStatus.setTextColor(context.getColor(R.color.red_error));
                holder.btnDeactivate.setVisibility(View.GONE);
                break;
        }
    }

    private void setupButtons(DiscountCodeViewHolder holder, DiscountCode code) {
        // Botón Editar
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditCode(code);
            }
        });

        // Botón Desactivar (solo para códigos activos)
        if ("active".equals(code.getStatus())) {
            holder.btnDeactivate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeactivateCode(code);
                }
            });
        } else {
            holder.btnDeactivate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return discountCodes.size();
    }

    public void updateData(List<DiscountCode> newDiscountCodes) {
        this.discountCodes = newDiscountCodes;
        notifyDataSetChanged();
    }

    static class DiscountCodeViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView tvCode, tvDiscount, tvMinPurchase, tvExpiry;
        Chip chipStatus;
        MaterialButton btnEdit, btnDeactivate;

        public DiscountCodeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_discount_code);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvMinPurchase = itemView.findViewById(R.id.tv_min_purchase);
            tvExpiry = itemView.findViewById(R.id.tv_expiry);
            chipStatus = itemView.findViewById(R.id.chip_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDeactivate = itemView.findViewById(R.id.btn_deactivate);
        }
    }
}