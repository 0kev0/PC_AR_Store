package com.example.pcarstore.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;

public class PrimeReminder {
    private static final String PREFS_NAME = "PrimePrefs";
    private static final String LAST_SHOWN_KEY = "lastShownTime";
    private static final long REMINDER_INTERVAL = 5 * 60 * 1000; // 5 minutos

    public static void showIfNeeded(Activity activity, User user) {
        if (shouldNotShow(activity, user)) return;

        activity.runOnUiThread(() -> {
            try {
                AlertDialog dialog = createPrimeDialog(activity);
                dialog.show();
                updateLastShownTime(activity);
            } catch (Exception e) {
                Log.e("PrimeDialog", "Error al mostrar diálogo: " + e.getMessage());
            }
        });
    }

    private static boolean shouldNotShow(Activity activity, User user) {
        return activity == null ||
                activity.isFinishing() ||
                activity.isDestroyed() ||
                user == null ||
                user.isMembresiaPrime() ||
                isDialogDisabled(activity) ||
                !isTimeToShow(activity);
    }

    private static AlertDialog createPrimeDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_prime_reminder, null);

        // Configuración de la imagen
        ImageView primeBanner = dialogView.findViewById(R.id.iv_prime_banner);
        Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.azamoz_logo2);
        primeBanner.setImageDrawable(drawable);

        // Configuración de botones
        /*dialogView.findViewById(R.id.btn_see_details).setOnClickListener(v -> {
            // Intent intent = new Intent(activity, PrimeActivity.class);
            // activity.startActivity(intent);
        });*/

        /*dialogView.findViewById(R.id.btn_close).setOnClickListener(v -> {
            // Cierra el diálogo
        });*/

        /*dialogView.findViewById(R.id.btn_dont_show).setOnClickListener(v -> {
            disableDialog(activity);
        });*/

        return builder.setView(dialogView).create();
    }

    private static boolean isTimeToShow(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastShown = prefs.getLong(LAST_SHOWN_KEY, 0);
        return System.currentTimeMillis() - lastShown >= REMINDER_INTERVAL;
    }

    private static void updateLastShownTime(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(LAST_SHOWN_KEY, System.currentTimeMillis())
                .apply();
    }

    private static boolean isDialogDisabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean("dialog_disabled", false);
    }

    private static void disableDialog(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("dialog_disabled", true)
                .apply();
    }
}