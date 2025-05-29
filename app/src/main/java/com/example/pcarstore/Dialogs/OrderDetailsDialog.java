package com.example.pcarstore.Dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pcarstore.ModelsDB.Order;
import com.example.pcarstore.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderDetailsDialog extends DialogFragment {
    private static final String TAG = "OrderDetailsDialog";
    private final Order order;
    private ProgressBar progressBar;
    private TextView orderStatusTextView;
    private final Handler handler = new Handler();
    private Runnable updateProgressRunnable;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public OrderDetailsDialog(Order order) {
        this.order = order;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        try {
            dialog.setContentView(R.layout.dialog_order_details);

            // Configurar fondo transparente
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            // Inicializar vistas
            initializeViews(dialog);

        } catch (Exception e) {
            Log.e(TAG, "Error al crear diálogo", e);
            Toast.makeText(getContext(), "Error al mostrar detalles del pedido", Toast.LENGTH_SHORT).show();
        }
        return dialog;
    }

    private void initializeViews(Dialog dialog) {
        TextView orderIdTextView = dialog.findViewById(R.id.tvOrderId);
        orderStatusTextView = dialog.findViewById(R.id.tvStatus);
        TextView orderTotalTextView = dialog.findViewById(R.id.tvTotalAmount);
        TextView orderDateTextView = dialog.findViewById(R.id.tvOrderDate);
        TextView deliveryDateTextView = dialog.findViewById(R.id.tvDeliveryDate);
        TextView itemsCountTextView = dialog.findViewById(R.id.tvItemsCount);
        progressBar = dialog.findViewById(R.id.orderProgress);
        Button closeButton = dialog.findViewById(R.id.btnClose);

        // Mostrar detalles de la orden
        if (order != null) {
            // ID de orden (mostrar solo primeros 8 caracteres)
            String shortOrderId = order.getOrderId() != null && order.getOrderId().length() > 8
                    ? order.getOrderId().substring(0, 8).toUpperCase()
                    : order.getOrderId();
            orderIdTextView.setText(getString(R.string.order_id_format, shortOrderId));

            // Estado
            orderStatusTextView.setText(getOrderStatusText(order.getStatus()));

            // Total
            orderTotalTextView.setText(getString(R.string.price_format, order.getTotal()));

            // Fechas
            orderDateTextView.setText(order.getDate() != null
                    ? dateFormat.format(order.getDate())
                    : getString(R.string.not_available));

            deliveryDateTextView.setText(order.getDeliveryDate() != null
                    ? dateFormat.format(order.getDeliveryDate())
                    : getString(R.string.not_available));

            // Conteo de artículos
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            itemsCountTextView.setText(getResources().getQuantityString(
                    R.plurals.items_count, itemCount, itemCount));

            // Configurar progreso solo si el pedido está en proceso
            Log.d(TAG, "Order status: " + order.getStatus());
            if ("processing".equalsIgnoreCase(order.getStatus())) {
                Log.d(TAG, "Order dates - Start: " + order.getDate() + ", End: " + order.getDeliveryDate());
                if (order.getDate() != null && order.getDeliveryDate() != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    startProgressUpdate();
                } else {
                    Log.d(TAG, "Hiding progress bar - missing dates");
                    progressBar.setVisibility(View.GONE);
                }
            } else {
                Log.d(TAG, "Hiding progress bar - status is not processing");
                progressBar.setVisibility(View.GONE);
            }
        } else {
            progressBar.setVisibility(View.GONE);
        }

        // Cerrar el diálogo
        closeButton.setOnClickListener(v -> dismiss());
    }

    private String getOrderStatusText(String status) {
        if (status == null) return getString(R.string.status_pending);

        switch (status.toLowerCase()) {
            case "completed":
                return getString(R.string.status_completed);
            case "cancelled":
                return getString(R.string.status_cancelled);
            case "processing":
                return getString(R.string.status_processing);
            default:
                return getString(R.string.status_pending);
        }
    }

    private void startProgressUpdate() {
        if (order.getDate() == null || order.getDeliveryDate() == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Initial update
        updateProgress();

        // Schedule periodic updates
        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                updateProgress();
                handler.postDelayed(this, 5000); // Actualizar cada 5 segundos
            }
        };
        handler.postDelayed(updateProgressRunnable, 5000);
    }

    private void updateProgress() {
        try {
            int progress = calculateProgress(order.getDate(), order.getDeliveryDate());
            Log.d(TAG, "Updating progress: " + progress);
            progressBar.setProgress(progress);
            orderStatusTextView.setText(getOrderStatus(progress));

            // If progress is complete, stop updates
            if (progress >= 100) {
                handler.removeCallbacks(updateProgressRunnable);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en actualización de progreso", e);
        }
    }

    private int calculateProgress(Date orderDate, Date deliveryDate) {
        long currentTime = System.currentTimeMillis();
        long startTime = orderDate.getTime();
        long endTime = deliveryDate.getTime();

        if (currentTime <= startTime) return 0;
        if (currentTime >= endTime) return 100;

        long totalDuration = endTime - startTime;
        long elapsedTime = currentTime - startTime;

        return (int) ((elapsedTime * 100) / totalDuration);
    }

    private String getOrderStatus(int progress) {
        if (progress < 15) {
            return getString(R.string.status_processing);
        } else if (progress < 30) {
            return "Empacando";
        } else if (progress < 45) {
            return "En ruta";
        } else if (progress < 75) {
            return "En camino";
        } else if (progress < 90) {
            return "Por llegar";
        } else if (progress < 100) {
            return "Entrega inminente";
        } else {
            return getString(R.string.status_completed);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar handler para evitar memory leaks
        if (handler != null && updateProgressRunnable != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }
    }
}