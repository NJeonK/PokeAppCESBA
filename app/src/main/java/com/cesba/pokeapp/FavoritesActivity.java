package com.cesba.pokeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private ImageView btnBackFavorites;
    private RecyclerView rvFavorites;
    private TextView txtEmptyFavorites;

    private FavoritesAdapter adapter;
    private List<FavoritePokemon> favoriteList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBackFavorites = findViewById(R.id.btnBackFavorites);
        rvFavorites = findViewById(R.id.rvFavorites);
        txtEmptyFavorites = findViewById(R.id.txtEmptyFavorites);

        btnBackFavorites.setOnClickListener(v -> finish());

        rvFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        favoriteList = new ArrayList<>();
        adapter = new FavoritesAdapter(favoriteList, pokemon -> {
            Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
            intent.putExtra("POKEMON_ID", String.valueOf(pokemon.getId()));
            startActivity(intent);
        });
        rvFavorites.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users").document(user.getUid())
                .collection("favoritos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        FavoritePokemon fav = doc.toObject(FavoritePokemon.class);
                        if (fav != null) {
                            favoriteList.add(fav);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (favoriteList.isEmpty()) {
                        txtEmptyFavorites.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    } else {
                        txtEmptyFavorites.setVisibility(View.GONE);
                        rvFavorites.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar favoritos", Toast.LENGTH_SHORT).show();
                });
    }
}