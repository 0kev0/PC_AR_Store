package com.example.pcarstore.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.pcarstore.ModelsDB.User;
import com.example.pcarstore.R;

public class PrimeReminder {
    private static final String PREFS_NAME = "PrimePrefs";
    private static final String LAST_SHOWN_KEY = "lastShownTime";
    private static final long REMINDER_INTERVAL = 5 * 60 * 1000; // 5 minutos

    public static void showIfNeeded(Activity activity, User user) {
        Log.d("PrimeDialog", "showIfNeeded called");
        Log.d("PrimeDialog", "Activity: " + (activity != null ? "OK" : "NULL"));
        Log.d("PrimeDialog", "User: " + (user != null ? "OK" : "NULL"));

        if (user != null) {
            Log.d("PrimeDialog", "User Prime Status: " + user.isMembresiaPrime());
        }

        boolean shouldShow = !shouldNotShow(activity, user);
        Log.d("PrimeDialog", "Should show dialog: " + shouldShow);

        if (!shouldShow) {
            Log.d("PrimeDialog", "Dialog not shown due to conditions");
            return;
        }

        activity.runOnUiThread(() -> {
            try {
                Log.d("PrimeDialog", "Creating and showing dialog");
                AlertDialog dialog = createPrimeDialog(activity, user);
                dialog.show();
                updateLastShownTime(activity);
                Log.d("PrimeDialog", "Dialog shown successfully");
            } catch (Exception e) {
                Log.e("PrimeDialog", "Error al mostrar diálogo: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static boolean shouldNotShow(Activity activity, User user) {
        if (activity == null) {
            Log.d("PrimeDialog", "Activity is null");
            return true;
        }
        if (activity.isFinishing()) {
            Log.d("PrimeDialog", "Activity is finishing");
            return true;
        }
        if (activity.isDestroyed()) {
            Log.d("PrimeDialog", "Activity is destroyed");
            return true;
        }
        if (user == null) {
            Log.d("PrimeDialog", "User is null");
            return true;
        }
        if (user.isMembresiaPrime()) {
            Log.d("PrimeDialog", "User already has Prime");
            return true;
        }
        if (isDialogDisabled(activity)) {
            Log.d("PrimeDialog", "Dialog is disabled");
            return true;
        }
        if (!isTimeToShow(activity)) {
            Log.d("PrimeDialog", "Not time to show yet");
            return true;
        }

        Log.d("PrimeDialog", "All conditions passed - should show dialog");
        return false;
    }

    private static AlertDialog createPrimeDialog(Activity activity, User user) {
        try {
            Log.d("PrimeDialog", "Creating dialog with XML layout");

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_prime_reminder, null);

            Log.d("PrimeDialog", "Layout inflated successfully");

            // Referencias a los botones únicamente
            Button subscribeButton = dialogView.findViewById(R.id.btn_start_prime_trial);
            Button MoreInfoButton = dialogView.findViewById(R.id.btn_learn_more);
            TextView dontShowAgain = dialogView.findViewById(R.id.btn_maybe_later);


            // Crear el diálogo
            AlertDialog dialog = builder.setView(dialogView).create();
            Log.d("PrimeDialog", "Dialog created successfully");

            // Configurar listeners de los botones
            if (subscribeButton != null) {
                subscribeButton.setOnClickListener(v -> {
                    Log.d("PrimeDialog", "Subscribe button clicked");
                    openSubscriptionActivity(activity);
                    dialog.dismiss();
                });
            }

            if (MoreInfoButton != null) {
                MoreInfoButton.setOnClickListener(v -> {
                    Log.d("PrimeDialog", "More info button clicked");
                    Toast.makeText(activity, "Próximamente", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }

            if (dontShowAgain != null) {
                dontShowAgain.setOnClickListener(v -> {
                    Log.d("PrimeDialog", "Don't show again clicked");
                    disableDialog(activity);
                    dialog.dismiss();
                });
            }

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            return dialog;

        } catch (Exception e) {
            Log.e("PrimeDialog", "Error creating dialog with XML: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void openSubscriptionActivity(Activity activity) {
        try {

            Log.d("PrimeDialog", "Abriendo actividad de suscripción...");

        } catch (Exception e) {
            Log.e("PrimeDialog", "Error al abrir suscripción: " + e.getMessage());
        }
    }

    private static boolean isTimeToShow(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastShown = prefs.getLong(LAST_SHOWN_KEY, 0);
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastShown;

        Log.d("PrimeDialog", "Last shown: " + lastShown);
        Log.d("PrimeDialog", "Current time: " + currentTime);
        Log.d("PrimeDialog", "Time difference: " + timeDiff + " ms");
        Log.d("PrimeDialog", "Required interval: " + REMINDER_INTERVAL + " ms");

        boolean shouldShow = timeDiff >= REMINDER_INTERVAL;
        Log.d("PrimeDialog", "Time check result: " + shouldShow);

        return shouldShow;
    }

    private static void updateLastShownTime(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(LAST_SHOWN_KEY, System.currentTimeMillis())
                .apply();
    }

    private static boolean isDialogDisabled(Context context) {
        boolean disabled = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean("dialog_disabled", false);
        Log.d("PrimeDialog", "Dialog disabled status: " + disabled);
        return disabled;
    }

    private static void disableDialog(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("dialog_disabled", true)
                .apply();
    }

}