package com.example.pcarstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.checkerframework.common.subtyping.qual.Bottom;

public class MainActivity extends AppCompatActivity {
        private Button catalogo, loginGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        catalogo = findViewById(R.id.ContinueWithoutAcount);
        loginGoogle = findViewById(R.id.LoginGoogle);
    }

    public void VerCatologo(View view) {
        Intent intent = new Intent(this, CatalogoActivity.class);
        startActivity(intent);
    }
}