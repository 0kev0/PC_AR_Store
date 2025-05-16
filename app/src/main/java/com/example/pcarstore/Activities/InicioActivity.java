package com.example.pcarstore.Activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.pcarstore.Fragments.CarritoFragment;
import com.example.pcarstore.Fragments.CatalogoFragment;
import com.example.pcarstore.Fragments.PerfilFragment;
import com.example.pcarstore.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.badge.BadgeDrawable;

public class InicioActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private BadgeDrawable cartBadge;
    private int cartItemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Configurar el badge
        cartBadge = bottomNavigationView.getOrCreateBadge(R.id.Carrito);

        updateCartBadge(0);

        if (savedInstanceState == null) {
            loadFragment(new CatalogoFragment());
        }
    }

    public void updateCartBadge(int itemCount) {
        if (itemCount > 0) {
            cartBadge.setNumber(itemCount);
            cartBadge.setVisible(true);
        } else {
            cartBadge.setVisible(false);
        }
    }
    public void incrementCartCount() {
        cartItemCount++;
        updateCartBadge(cartItemCount);

    }


    private int getCartItemCount() {
        return cartItemCount;
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        Fragment selectedFragment = null;

        if (itemId == R.id.Inicio) {
            selectedFragment = new CatalogoFragment();
        } else if (itemId == R.id.Carrito) {
            selectedFragment = new CarritoFragment();
        } else if (itemId == R.id.Perfil) {
            selectedFragment = new PerfilFragment();
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
            return true;
        }
        return false;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainerView2, fragment);
        transaction.commit();
    }
}