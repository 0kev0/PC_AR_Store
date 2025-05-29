package com.example.pcarstore.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pcarstore.R;
import com.example.pcarstore.ModelsDB.GiftCard;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class GiftCardAdapter extends RecyclerView.Adapter<GiftCardAdapter.GiftCardViewHolder> {

    private final List<GiftCard> giftCards;
    private final Context context;
    private final SimpleDateFormat dateFormat;
    private final OnGiftCardActionsListener listener;

    public interface OnGiftCardActionsListener {
        void onEditGiftCard(GiftCard giftCard);
        void onDeleteGiftCard(GiftCard giftCard);
    }

    public GiftCardAdapter(List<GiftCard> giftCards, Context context, OnGiftCardActionsListener listener) {
        this.giftCards = giftCards;
        this.context = context;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public GiftCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gift_card, parent, false);
        return new GiftCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftCardViewHolder holder, int position) {
        GiftCard giftCard = giftCards.get(position);

        holder.tvCode.setText(giftCard.getCode());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", giftCard.getAmount()));
        holder.tvStatus.setText(giftCard.getStatus());

        // Manejo seguro de la fecha de expiración
        String expirationStr = "Vence: ";
        if (giftCard.getExpirationDate() != null) {
            expirationStr += dateFormat.format(giftCard.getExpirationDate());
        } else {
            expirationStr += "No definida"; // O cualquier texto por defecto
        }
        holder.tvExpiration.setText(expirationStr);

        if (giftCard.getRecipientEmail() != null && !giftCard.getRecipientEmail().isEmpty()) {
            holder.tvRecipient.setText("Para: " + giftCard.getRecipientEmail());
            holder.tvRecipient.setVisibility(View.VISIBLE);
        } else {
            holder.tvRecipient.setVisibility(View.GONE);
        }

        int statusColor;
        switch (giftCard.getStatus()) {
            case "REDEEMED":
                statusColor = R.color.green;
                break;
            case "VENCIDA":
                statusColor = R.color.red;
                break;
            default:
                statusColor = R.color.blue;
                break;
        }
        holder.tvStatus.setBackgroundColor(context.getResources().getColor(statusColor));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditGiftCard(giftCard);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteGiftCard(giftCard);
            }
        });

    }

    @Override
    public int getItemCount() {
        return giftCards != null ? giftCards.size() : 0;
    }

    public static class GiftCardViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvAmount, tvStatus, tvExpiration, tvRecipient;
        Button btnEdit, btnDelete;

        public GiftCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_card_number);
            tvAmount = itemView.findViewById(R.id.tv_balance);
            tvStatus = itemView.findViewById(R.id.chip_status);
            tvExpiration = itemView.findViewById(R.id.tv_expiry);
            tvRecipient = itemView.findViewById(R.id.tv_recipient);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_deactivate);
        }
    }

}