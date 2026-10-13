package com.cesba.pokeapp;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SafariActivity extends AppCompatActivity {

    private ImageView btnBackSafari, imgSafariPokemon;
    private TextView txtSafariName, txtSafariRarity, txtSafariLog;
    private TextView txtSafariBalls, txtSafariScore;
    private MaterialButton btnWalk, btnThrowBall;

    private PokeApiService service;
    private Pokemon currentPokemon;
    private Random random = new Random();

    private int safariBalls = 10;
    private int safariScore = 0;
    private int currentPokemonPoints = 50;
    private double currentCatchRate = 50.0;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final List<Integer> LEGENDARY_IDS = Arrays.asList(
            150, 151, 243, 244, 245, 249, 250, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386,
            480, 481, 482, 483, 484, 485, 486, 487, 488, 490, 491, 492, 493
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safari);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        service = ApiClient.getClient().create(PokeApiService.class);

        btnBackSafari = findViewById(R.id.btnBackSafari);
        imgSafariPokemon = findViewById(R.id.imgSafariPokemon);

        txtSafariName = findViewById(R.id.txtSafariName);
        txtSafariRarity = findViewById(R.id.txtSafariRarity);
        txtSafariLog = findViewById(R.id.txtSafariLog);

        txtSafariBalls = findViewById(R.id.txtSafariBalls);
        txtSafariScore = findViewById(R.id.txtSafariScore);

        btnWalk = findViewById(R.id.btnWalk);
        btnThrowBall = findViewById(R.id.btnThrowBall);

        btnBackSafari.setOnClickListener(v -> finish());

        btnWalk.setOnClickListener(v -> walkInGrass());
        btnThrowBall.setOnClickListener(v -> throwSafariBall());

        resetSafariSession();
    }

    private void resetSafariSession() {
        safariBalls = 10;
        safariScore = 0;
        currentPokemon = null;

        updateSafariStatsUI();
        imgSafariPokemon.setImageDrawable(null);
        txtSafariName.setText("Camina por la hierba");
        txtSafariRarity.setText("Valor: -- pts");
        txtSafariLog.setText("Presiona 'CAMINAR POR LA HIERBA' para buscar Pokémon...");
        btnThrowBall.setEnabled(false);
    }

    private void updateSafariStatsUI() {
        txtSafariBalls.setText("Esferas Safari: " + safariBalls + " / 10");
        txtSafariScore.setText("Puntos: " + safariScore + " pts");
        btnThrowBall.setText("LANZAR SAFARI BALL (" + safariBalls + " RESTANTES)");
    }

    private void walkInGrass() {
        if (safariBalls <= 0) {
            endSafariSession();
            return;
        }

        btnWalk.setEnabled(false);
        btnThrowBall.setEnabled(false);
        txtSafariLog.setText("Caminando por la hierba alta...");

        int randomId = random.nextInt(800) + 1;
        service.getPokemon(String.valueOf(randomId)).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                btnWalk.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    currentPokemon = response.body();
                    displayWildPokemon();
                } else {
                    walkInGrass();
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                btnWalk.setEnabled(true);
                Toast.makeText(SafariActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayWildPokemon() {
        if (currentPokemon == null) return;

        btnThrowBall.setEnabled(true);
        String name = currentPokemon.getName().substring(0, 1).toUpperCase(Locale.ROOT) + currentPokemon.getName().substring(1);

        // Clasificación de Rareza y Puntos
        int id = currentPokemon.getId();
        if (LEGENDARY_IDS.contains(id)) {
            currentPokemonPoints = 150;
            currentCatchRate = 20.0;
            txtSafariRarity.setText("¡LEGENDARIO! • Valor: 150 pts");
        } else if (id % 3 == 0 || getBaseStatTotal(currentPokemon) > 420) {
            currentPokemonPoints = 100;
            currentCatchRate = 35.0;
            txtSafariRarity.setText("EVOLUCIONADO • Valor: 100 pts");
        } else {
            currentPokemonPoints = 50;
            currentCatchRate = 55.0;
            txtSafariRarity.setText("NORMAL • Valor: 50 pts");
        }

        txtSafariName.setText(name + " (#" + id + ")");
        txtSafariLog.setText("¡Un " + name.toUpperCase(Locale.ROOT) + " apareció!");

        if (currentPokemon.getSprites() != null) {
            Glide.with(this)
                    .load(currentPokemon.getSprites().getFrontDefault())
                    .into(imgSafariPokemon);
        }
    }

    private int getBaseStatTotal(Pokemon p) {
        int total = 0;
        if (p.getStats() != null) {
            for (PokemonStat s : p.getStats()) {
                total += s.getBaseStat();
            }
        }
        return total;
    }

    private void throwSafariBall() {
        if (currentPokemon == null || safariBalls <= 0) return;

        safariBalls--;
        updateSafariStatsUI();

        btnThrowBall.setEnabled(false);
        String name = currentPokemon.getName().toUpperCase(Locale.ROOT);

        txtSafariLog.setText("Lanzaste una Safari Ball...");

        ObjectAnimator anim = ObjectAnimator.ofFloat(imgSafariPokemon, "translationX", 0f, 15f, -15f, 15f, -15f, 0f);
        anim.setDuration(500);
        anim.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (random.nextInt(100) < currentCatchRate) {
                // CAPTURADO
                safariScore += currentPokemonPoints;
                updateSafariStatsUI();
                txtSafariLog.setText("¡GOTCHA! ¡" + name + " capturado! (+" + currentPokemonPoints + " pts)");
                saveToCollection();

                if (safariBalls == 0) {
                    new Handler(Looper.getMainLooper()).postDelayed(this::endSafariSession, 2000);
                }
            } else {
                // FALLÓ
                if (random.nextInt(100) < 35) {
                    // HUYÓ
                    txtSafariLog.setText("¡" + name + " se escapó y huyó de la zona!");
                    currentPokemon = null;
                    if (safariBalls == 0) {
                        new Handler(Looper.getMainLooper()).postDelayed(this::endSafariSession, 2000);
                    }
                } else {
                    // SE QUEDA
                    txtSafariLog.setText("¡El Pokémon se zafó! Puedes intentar otro tiro.");
                    if (safariBalls > 0) {
                        btnThrowBall.setEnabled(true);
                    } else {
                        new Handler(Looper.getMainLooper()).postDelayed(this::endSafariSession, 2000);
                    }
                }
            }
        }, 1000);
    }

    private void saveToCollection() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || currentPokemon == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("id", currentPokemon.getId());
        data.put("name", currentPokemon.getName());
        if (currentPokemon.getSprites() != null) {
            data.put("image", currentPokemon.getSprites().getFrontDefault());
        }

        db.collection("users").document(user.getUid())
                .collection("coleccion").document(String.valueOf(currentPokemon.getId()))
                .set(data);
    }

    private void endSafariSession() {
        saveSafariRecord();

        new AlertDialog.Builder(this)
                .setTitle("¡FIN DEL SAFARI!")
                .setMessage("Te has quedado sin Esferas Safari.\n\nPuntuación Total: " + safariScore + " pts")
                .setCancelable(false)
                .setPositiveButton("Jugar de nuevo", (dialog, which) -> resetSafariSession())
                .setNegativeButton("Salir al menú", (dialog, which) -> finish())
                .show();
    }

    private void saveSafariRecord() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("record", safariScore);

        db.collection("users").document(user.getUid())
                .collection("records").document("safari")
                .set(data);
    }
}