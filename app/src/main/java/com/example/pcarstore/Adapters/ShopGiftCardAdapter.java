package com.example.pcarstore.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pcarstore.ModelsDB.GiftCard;
import com.example.pcarstore.R;
import java.util.List;

public class ShopGiftCardAdapter extends RecyclerView.Adapter<ShopGiftCardAdapter.GiftCardViewHolder> {

    private final List<GiftCard> giftCards;
    private final OnBuyClickListener listener;

    public interface OnBuyClickListener {
        void onBuyClick(GiftCard giftCard);
    }

    public ShopGiftCardAdapter(List<GiftCard> giftCards, OnBuyClickListener listener) {
        this.giftCards = giftCards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GiftCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_gift_card_store, parent, false);
        return new GiftCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftCardViewHolder holder, int position) {
        GiftCard giftCard = giftCards.get(position);
        holder.tvPrice.setText(String.format("$%.2f", giftCard.getAmount()));

        holder.btnBuy.setOnClickListener(v -> listener.onBuyClick(giftCard));
    }

    @Override
    public int getItemCount() {
        return giftCards.size();
    }

    public void updateData(List<GiftCard> newGiftCards) {
        giftCards.clear();
        giftCards.addAll(newGiftCards);
        notifyDataSetChanged();
    }

    static class GiftCardViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPrice;
        final Button btnBuy;

        public GiftCardViewHolder(View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.tvGiftCardPrice);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }
}