package com.cesba.pokeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.SharedPreferences;
import android.net.Uri;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainMenuActivity extends AppCompatActivity {

    private ImageView btnSettingsMenu;
    private MaterialCardView btnAvatarPerfil;
    private ImageView imgAvatar;
    private TextView txtWelcome;
    private MaterialCardView cardPokedex, cardBattle, cardVersus, cardTorre;
    private MaterialCardView cardWhoPokemon, cardSafari, cardTipos, cardMemory;
    private MaterialCardView cardFavoritos, cardPerfil;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("PokeAppPrefs", MODE_PRIVATE);

        initViews();
        setupUser();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUser();
    }

    private void initViews() {
        btnSettingsMenu = findViewById(R.id.btnSettingsMenu);
        btnAvatarPerfil = findViewById(R.id.btnAvatarPerfil);
        imgAvatar = findViewById(R.id.imgAvatar);
        txtWelcome = findViewById(R.id.txtWelcome);

        cardPokedex = findViewById(R.id.cardPokedex);
        cardBattle = findViewById(R.id.cardBattle);
        cardVersus = findViewById(R.id.cardVersus);
        cardTorre = findViewById(R.id.cardTorre);
        cardWhoPokemon = findViewById(R.id.cardWhoPokemon);
        cardSafari = findViewById(R.id.cardSafari);
        cardTipos = findViewById(R.id.cardTipos);
        cardMemory = findViewById(R.id.cardMemory);
        cardFavoritos = findViewById(R.id.cardFavoritos);
        cardPerfil = findViewById(R.id.cardPerfil);
    }

    private void setupUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombre");
                            txtWelcome.setText(nombre != null && !nombre.isEmpty() ? nombre : "Entrenador");
                        } else {
                            txtWelcome.setText(user.getEmail() != null ? user.getEmail().split("@")[0] : "Entrenador");
                        }
                    })
                    .addOnFailureListener(e -> {
                        txtWelcome.setText(user.getEmail() != null ? user.getEmail().split("@")[0] : "Entrenador");
                    });

            String savedUri = prefs.getString("profile_image_" + user.getUid(), null);
            if (savedUri != null) {
                Glide.with(this).load(Uri.parse(savedUri)).circleCrop().into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_user);
            }
        }
    }

    private void setupListeners() {
        btnSettingsMenu.setOnClickListener(this::showSettingsMenu);
        btnAvatarPerfil.setOnClickListener(v -> showSettingsMenu(v));

        // Modulo A: Pokédex (Abre la Pokédex actual)
        cardPokedex.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Modulo B: Battle
        cardBattle.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, BattleSetupActivity.class);
            startActivity(intent);
        });

        // Modulo C: Battle Versus
        cardVersus.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, VersusSetupActivity.class);
            startActivity(intent);
        });

        // Modulo D: Torre Pokémon
        cardTorre.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, TowerSetupActivity.class);
            startActivity(intent);
        });

        // Modulo E: ¿Quién es ese Pokémon?
        cardWhoPokemon.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, WhoPokemonActivity.class);
            startActivity(intent);
        });

        // Modulo F: Safari Pokémon
        cardSafari.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, SafariActivity.class);
            startActivity(intent);
        });

        // Placeholders para futuros módulos
        cardTipos.setOnClickListener(v -> showToast("Modulo G. Maestro Tipos próximamente"));
        cardMemory.setOnClickListener(v -> showToast("Modulo H. PokéMemory próximamente"));
        
        cardFavoritos.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        cardPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void showSettingsMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add(Menu.NONE, 1, 1, "Mi Perfil");
        popup.getMenu().add(Menu.NONE, 2, 2, "Historial de Batallas");
        popup.getMenu().add(Menu.NONE, 3, 3, "Colección de Medallas");
        popup.getMenu().add(Menu.NONE, 4, 4, "Cerrar Sesión");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    startActivity(new Intent(MainMenuActivity.this, ProfileActivity.class));
                    return true;
                case 2:
                    startActivity(new Intent(MainMenuActivity.this, HistoryActivity.class));
                    return true;
                case 3:
                    startActivity(new Intent(MainMenuActivity.this, MedalsActivity.class));
                    return true;
                case 4:
                    logout();
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(MainMenuActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}