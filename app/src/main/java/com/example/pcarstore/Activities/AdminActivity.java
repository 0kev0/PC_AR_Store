package com.example.pcarstore.Activities;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pcarstore.databinding.ActivityAdminBinding;

import com.example.pcarstore.R;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Asegúrate que sea el layout correcto

        // Configuración básica
        setupToolbar();
        loadInitialFragment();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar); // ID debe coincidir con tu XML
        setSupportActionBar(toolbar);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Administración");
        }
    }

    private void loadInitialFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, new FirstFragment())
                .commit();
    }

    // Método seguro para encontrar vistas
    private <T extends View> T findView(@IdRes int id) {
        T view = findViewById(id);
        if (view == null) {
            throw new IllegalArgumentException("ID "+getResources().getResourceName(id)+" no encontrado");
        }
        return view;
    }
}