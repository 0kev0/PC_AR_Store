package com.example.pcarstore.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.pcarstore.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AdminFragment extends Fragment {

    private TextView tvUsersCount;
    private TextView tvProductsCount;
    private TextView tvOrdersCount;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);
        initializeViews(view);
        setupRecentActivities(view);
        setupClickListeners(view);
        updateMetrics();
        return view;
    }
    private void setupRecentActivities(View rootView) {
        setupRecentUsers(rootView);
        setupRecentOrders(rootView);
    }

    private void setupRecentUsers(View rootView) {
        LinearLayout userActivity = rootView.findViewById(R.id.activity_user);
        TextView tvUserTitle = rootView.findViewById(R.id.tvActivityTitle1);
        TextView tvUserSubtitle = rootView.findViewById(R.id.tvActivitySubtitle1);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query recentUserQuery = usersRef.orderByChild("timestamp")
                .limitToLast(1);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String name = userSnapshot.child("name").getValue(String.class);
                        Long timestamp = userSnapshot.child("timestamp").getValue(Long.class);
                        String email = userSnapshot.child("email").getValue(String.class);

                        //String timeAgo = getTimeAgo(timestamp);

                        tvUserTitle.setText("Nuevo usuario registrado");
                        tvUserSubtitle.setText((name != null ? name : email) + " - "+ email +  "\n - " + timestamp);

                    }
                } else {
                    tvUserTitle.setText("No hay usuarios recientes");
                    tvUserSubtitle.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading users", databaseError.toException());
                tvUserTitle.setText("Error cargando usuarios");
                tvUserSubtitle.setText("");
            }
        });
    }

    private void setupRecentOrders(View rootView) {
        LinearLayout orderActivity = rootView.findViewById(R.id.activity_order);
        TextView tvOrderTitle = rootView.findViewById(R.id.tvActivityTitle2);
        TextView tvOrderSubtitle = rootView.findViewById(R.id.tvActivitySubtitle2);

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        Query recentOrderQuery = ordersRef.orderByChild("status")
                .equalTo("completado")
                .limitToLast(1);

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        String orderId = orderSnapshot.getKey();
                        Long timestamp = orderSnapshot.child("timestamp").getValue(Long.class);
                        String customerName = orderSnapshot.child("customerName").getValue(String.class);
                        Double total = orderSnapshot.child("total").getValue(Double.class);

                        //String timeAgo = getTimeAgo(timestamp);

                        tvOrderTitle.setText("Pedido completado");
                        tvOrderSubtitle.setText("Orden #" + orderId + " - " +
                                (customerName != null ? customerName : "") +
                                (total != null ? " - $" + total : "") +
                                " - " + timestamp);


                    }
                } else {
                    tvOrderTitle.setText("No hay pedidos recientes");
                    tvOrderSubtitle.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading orders", databaseError.toException());
                tvOrderTitle.setText("Error cargando pedidos");
                tvOrderSubtitle.setText("");
            }
        });
    }

    private void initializeViews(View rootView) {
        try {
            // Inicializar TextViews con verificación
            tvUsersCount = rootView.findViewById(R.id.tvUsersCount);
            tvProductsCount = rootView.findViewById(R.id.tvProductsCount);
            tvOrdersCount = rootView.findViewById(R.id.tvOrdersCount);

            // Verificar que todas las vistas fueron encontradas
            if (tvUsersCount == null) {
                throw new RuntimeException("tvUsersCount no encontrado en el layout");
            }
            if (tvProductsCount == null) {
                throw new RuntimeException("tvProductsCount no encontrado en el layout");
            }
            if (tvOrdersCount == null) {
                throw new RuntimeException("tvOrdersCount no encontrado en el layout");
            }

            // Inicializar consultas
            countUsers();
            countProducts();
            countOrders();

            // TextView de bienvenida
            TextView tvWelcome = rootView.findViewById(R.id.tvWelcome);
            if (tvWelcome != null) {
                tvWelcome.setText("Bienvenido, Admin");
            } else {
                Log.e("InitializeViews", "tvWelcome no encontrado en el layout");
            }

        } catch (Exception e) {
            Log.e("InitializeViews", "Error inicializando vistas: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error inicializando vista: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void countUsers() {
        try {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (tvUsersCount != null) {
                        long userCount = dataSnapshot.getChildrenCount();
                        tvUsersCount.setText(String.valueOf(userCount));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (tvUsersCount != null) {
                        tvUsersCount.setText("Error");
                    }
                    Log.e("Firebase", "Error contando usuarios", databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.e("CountUsers", "Error en countUsers", e);
        }
    }

    private void countProducts() {
        try {
            DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("products");
            productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (tvProductsCount != null) {
                        long productCount = dataSnapshot.getChildrenCount();
                        tvProductsCount.setText(String.valueOf(productCount));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (tvProductsCount != null) {
                        tvProductsCount.setText("Error");
                    }
                    Log.e("Firebase", "Error contando productos", databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.e("CountProducts", "Error en countProducts", e);
        }
    }

    private void countOrders() {
        try {
            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
            ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (tvOrdersCount != null) {
                        long orderCount = dataSnapshot.getChildrenCount();
                        tvOrdersCount.setText(String.valueOf(orderCount));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (tvOrdersCount != null) {
                        tvOrdersCount.setText("Error");
                    }
                    Log.e("Firebase", "Error contando pedidos", databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.e("CountOrders", "Error en countOrders", e);
        }
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
            replaceFragment(new InventarioFragment());
        });

        rootView.findViewById(R.id.card_gift_cards).setOnClickListener(v -> {
            replaceFragment(new CodesFragment());
        });

        rootView.findViewById(R.id.card_settings).setOnClickListener(v -> {
            replaceFragment(new ConfiguracionAdminFragment());
        });
    }

    private void replaceFragment(Fragment fragment2) {
        try {
            // Obtener el FragmentManager de la actividad
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
            transaction.replace(R.id.fragmentContaineradmin, fragment2);
            transaction.addToBackStack(null);
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