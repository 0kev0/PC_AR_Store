package com.example.pcarstore.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;
import com.example.pcarstore.ModelsDB.GiftCard;
import com.google.firebase.database.DatabaseReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExpireGiftCardDialog {

    public interface OnGiftCardExpiredListener {
        void onGiftCardExpired();
        void onExpireError(String error);
    }

    public static void show(Context context, GiftCard giftCard,
                            DatabaseReference databaseRef,
                            OnGiftCardExpiredListener listener) {

        new AlertDialog.Builder(context)
                .setTitle("Marcar como vencida")
                .setMessage("¿Estás seguro de marcar esta Gift Card como vencida?")
                .setPositiveButton("Marcar", (dialog, which) ->
                        expireGiftCard(context, giftCard, databaseRef, listener))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private static void expireGiftCard(Context context, GiftCard giftCard,
                                       DatabaseReference databaseRef,
                                       OnGiftCardExpiredListener listener) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "VENCIDA");
        updates.put("expirationDate", new Date().getTime());

        DatabaseReference targetRef;
        String refKey = databaseRef.getKey();

        if (refKey == null) {
            targetRef = databaseRef.child("giftCards").child(giftCard.getCardId());
        } else if (refKey.equals("giftCards")) {
            targetRef = databaseRef.child(giftCard.getCardId());
        } else {
            targetRef = databaseRef.child("giftCards").child(giftCard.getCardId());
        }

        targetRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Gift Card marcada como vencida", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onGiftCardExpired();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onExpireError(e.getMessage());
                    }
                });
    }
}