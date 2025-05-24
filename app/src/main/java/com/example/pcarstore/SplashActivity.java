package com.example.pcarstore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.pcarstore.Activities.LoginActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 3000;// 3 segundos
    private LottieAnimationView loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Lottie
        loadingAnimation = findViewById(R.id.loadingAnimation);
        loadingAnimation.setAnimation(R.raw.loading_animation2);
        loadingAnimation.loop(true);

        // Mostrar animación
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadingAnimation.setVisibility(View.VISIBLE);
            loadingAnimation.playAnimation();
        }, 500);

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToLogin, SPLASH_DELAY);
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (loadingAnimation != null) {
            loadingAnimation.cancelAnimation();
        }
        super.onDestroy();
    }
}