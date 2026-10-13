package com.cesba.pokeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500; // 2.5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Usuario ya inició sesión -> Ir al Menú Principal
                Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);
                startActivity(intent);
            } else {
                // No hay sesión activa -> Ir al Login / Registro
                Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
                startActivity(intent);
            }
            finish();
        }, SPLASH_DELAY);
    }
}