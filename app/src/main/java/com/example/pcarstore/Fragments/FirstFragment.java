package com.example.pcarstore.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.pcarstore.R;
import com.google.android.material.card.MaterialCardView;

public class FirstFragment extends Fragment {

    private TextView tvUsersCount;
    private TextView tvProductsCount;
    private TextView tvOrdersCount;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);
        initializeViews(view);
        setupClickListeners(view);
        updateMetrics();
        return view;
    }

    private void initializeViews(View rootView) {
        tvUsersCount = rootView.findViewById(R.id.tvUsersCount);
        tvProductsCount = rootView.findViewById(R.id.tvProductsCount);
        tvOrdersCount = rootView.findViewById(R.id.tvOrdersCount);

        TextView tvWelcome = rootView.findViewById(R.id.tvWelcome);
        tvWelcome.setText("Bienvenido, Admin");
    }

    private void setupClickListeners(View rootView) {
        // Métricas
        rootView.findViewById(R.id.metric_users).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ver detalles de usuarios", Toast.LENGTH_SHORT).show();
        });

        rootView.findViewById(R.id.metric_products).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ver detalles de productos", Toast.LENGTH_SHORT).show();
        });

        rootView.findViewById(R.id.metric_orders).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ver detalles de pedidos", Toast.LENGTH_SHORT).show();
        });

        // Acciones rápidas
        rootView.findViewById(R.id.card_manage_users).setOnClickListener(v -> {
            replaceFragment(new UsuariosFragment());
        });

        rootView.findViewById(R.id.card_inventory).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Control de Inventario seleccionado", Toast.LENGTH_SHORT).show();
        });

        rootView.findViewById(R.id.card_reports).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Generar Reportes seleccionado", Toast.LENGTH_SHORT).show();
        });

        rootView.findViewById(R.id.card_settings).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Configuración seleccionada", Toast.LENGTH_SHORT).show();
        });
    }

    private void replaceFragment(Fragment fragment2) {
        try {
            // Obtener el FragmentManager de la actividad
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();

            // Reemplazar el fragmento actual
            transaction.replace(R.id.fragmentContaineradmin, fragment2);

            // Opcional: Agregar a la pila de retroceso
            transaction.addToBackStack(null);

            // Confirmar la transacción
            transaction.commit();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al cargar el fragmento", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void updateMetrics() {
        tvUsersCount.setText("125");
        tvProductsCount.setText("89");
        tvOrdersCount.setText("42");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tvUsersCount = null;
        tvProductsCount = null;
        tvOrdersCount = null;
    }
}