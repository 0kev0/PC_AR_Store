package com.example.pcarstore.Dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pcarstore.ModelsDB.Order;
import com.example.pcarstore.R;

import java.util.Date;

public class OrderDetailsDialog extends DialogFragment {
    /*************************************************************VARIABLES******************************************************************************************/
    private final Order order;
    private ProgressBar progressBar;
    private TextView orderStatusTextView;
    private final Handler handler = new Handler();
    private Runnable updateProgressRunnable;

    public OrderDetailsDialog(Order order) {
        this.order = order;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_order_details);

        // Inicializar vistas
        TextView orderIdTextView = dialog.findViewById(R.id.tvOrderId);
        orderStatusTextView = dialog.findViewById(R.id.tvStatus);
        TextView orderTotalTextView = dialog.findViewById(R.id.tvTotalAmount);
        TextView orderDateTextView = dialog.findViewById(R.id.tvOrderDate);
        TextView deliveryDateTextView = dialog.findViewById(R.id.tvDeliveryDate);
        TextView itemsCountTextView = dialog.findViewById(R.id.tvItemsCount);
        progressBar = dialog.findViewById(R.id.orderProgress);
        Button closeButton = dialog.findViewById(R.id.btnClose);

        // Mostrar detalles de la orden
        orderIdTextView.setText(order.getOrderId());
        orderStatusTextView.setText(order.getStatus());
        orderTotalTextView.setText("$" + order.getTotal());
        orderDateTextView.setText(order.getDate().toString());
        deliveryDateTextView.setText(order.getDeliveryDate().toString());
        itemsCountTextView.setText(order.getItems().size() + " artículos");

        // Iniciar actualización periódica del progreso
        startProgressUpdate();

        // Cerrar el diálogo
        closeButton.setOnClickListener(v -> {
            handler.removeCallbacks(updateProgressRunnable); // Detener actualización
            dismiss();
        });

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_order_details, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return view;
    }

    private void startProgressUpdate() {
        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                int progress = calculateProgress(order.getDate(), order.getDeliveryDate());
                progressBar.setProgress(progress);
                orderStatusTextView.setText(getOrderStatus(progress));
                handler.postDelayed(this, 3000); // Actualizar cada 3 segundos
            }
        };
        handler.post(updateProgressRunnable);
    }

    private int calculateProgress(Date orderDate, Date deliveryDate) {
        if (orderDate == null || deliveryDate == null) return 0;

        long currentTime = new Date().getTime();
        long startTime = orderDate.getTime();
        long endTime = deliveryDate.getTime();

        if (currentTime < startTime) return 0;
        if (currentTime >= endTime) return 100;

        long totalTime = endTime - startTime;
        long elapsedTime = currentTime - startTime;

        return (int) ((elapsedTime * 100) / totalTime); // Calcular porcentaje
    }

    private String getOrderStatus(int progress) {
        if (progress < 15) {
            return "Procesando";
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
            return "Completado";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateProgressRunnable); // Detener actualización
    }

}
