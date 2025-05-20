package com.example.pcarstore.Activities;

import android.os.Bundle;

import com.example.pcarstore.Fragments.CarritoFragment;
import com.example.pcarstore.Fragments.CatalogoFragment;
import com.example.pcarstore.Fragments.FirstFragment;
import com.example.pcarstore.Fragments.PerfilFragment;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.pcarstore.Fragments.UsuariosFragment;
import com.example.pcarstore.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        bottomNavigationView = findViewById(R.id.admin_bottom_nav);
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
            selectedFragment = new FirstFragment();
        } else if (itemId == R.id.nav_users) {
            Toast.makeText(this, "Usuarios", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_products) {
            Toast.makeText(this, "Inventario", Toast.LENGTH_SHORT).show();
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
            return true;
        }
        return false;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContaineradmin, fragment);
        transaction.commit();
    }

    private void loadInitialFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, new FirstFragment())
                .commit();
    }

}