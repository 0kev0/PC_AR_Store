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

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.pcarstore.Fragments.UsuariosFragment;
import com.example.pcarstore.R;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Asegúrate que sea el layout correcto

    }
}