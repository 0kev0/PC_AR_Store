package com.example.pcarstore.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.pcarstore.Fragments.AdminFragment;
import com.example.pcarstore.Fragments.SalesRecordsFragment;
import com.example.pcarstore.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        BottomNavigationView bottomNavigationView = findViewById(R.id.admin_bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        setupToolbar();
        loadInitialFragment();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Administración");
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        Fragment selectedFragment = null;

        if (itemId == R.id.nav_dashboard) {
            selectedFragment = new AdminFragment();
        } else if (itemId == R.id.nav_ventas) {
            selectedFragment = new SalesRecordsFragment();
            Toast.makeText(this, "Registro de ventas", Toast.LENGTH_SHORT).show(); // Corregido typo
        } else if (itemId == R.id.nav_products) {
            // Implementa tu fragmento de inventario aquí
            Toast.makeText(this, "Inventario (no implementado)", Toast.LENGTH_SHORT).show();
            return false; // No carga fragmento
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
            return true;
        }
        return false;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
        transaction.replace(R.id.fragmentContainerView, fragment);
        transaction.commit();
    }

    private void loadInitialFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContaineradmin, new AdminFragment())
                .commit();
    }
}