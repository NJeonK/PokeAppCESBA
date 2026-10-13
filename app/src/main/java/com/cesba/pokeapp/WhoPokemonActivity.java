package com.cesba.pokeapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WhoPokemonActivity extends AppCompatActivity {

    private ImageView btnBackWho, imgWhoPokemon;
    private TextView txtWhoScore, txtWhoRecord, txtWhoHintResult, txtAttemptBadge;
    private MaterialButton btnHintType, btnHintStat, btnHintCry;
    private MaterialButton btnOpt1, btnOpt2, btnOpt3, btnOpt4;

    private PokeApiService service;
    private Pokemon currentPokemon;
    private MediaPlayer mediaPlayer;

    private int score = 0;
    private int record = 0;
    private int currentAttempt = 1; // 1, 2, 3

    private final List<String> allPokemonNames = new ArrayList<>();
    private final Random random = new Random();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_pokemon);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        service = ApiClient.getClient().create(PokeApiService.class);

        btnBackWho = findViewById(R.id.btnBackWho);
        imgWhoPokemon = findViewById(R.id.imgWhoPokemon);

        txtWhoScore = findViewById(R.id.txtWhoScore);
        txtWhoRecord = findViewById(R.id.txtWhoRecord);
        txtWhoHintResult = findViewById(R.id.txtWhoHintResult);
        txtAttemptBadge = findViewById(R.id.txtAttemptBadge);

        btnHintType = findViewById(R.id.btnHintType);
        btnHintStat = findViewById(R.id.btnHintStat);
        btnHintCry = findViewById(R.id.btnHintCry);

        btnOpt1 = findViewById(R.id.btnOpt1);
        btnOpt2 = findViewById(R.id.btnOpt2);
        btnOpt3 = findViewById(R.id.btnOpt3);
        btnOpt4 = findViewById(R.id.btnOpt4);

        btnBackWho.setOnClickListener(v -> finish());

        btnHintType.setOnClickListener(v -> giveTypeHint());
        btnHintStat.setOnClickListener(v -> giveStatHint());
        btnHintCry.setOnClickListener(v -> giveCryHint());

        loadUserRecord();
        cargarListaNombresYEmpezar();
    }

    private void loadUserRecord() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .collection("records").document("who_is_that")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("record")) {
                        Long rec = doc.getLong("record");
                        if (rec != null) {
                            record = rec.intValue();
                            txtWhoRecord.setText("Récord: " + record);
                        }
                    }
                });
    }

    private void cargarListaNombresYEmpezar() {
        service.getPokemonList(800, 0).enqueue(new Callback<PokemonListResponse>() {
            @Override
            public void onResponse(Call<PokemonListResponse> call, Response<PokemonListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    allPokemonNames.clear();
                    for (PokemonListResponse.PokemonItem item : response.body().getResults()) {
                        if (item.getName() != null) {
                            allPokemonNames.add(item.getName());
                        }
                    }
                }
                loadNextPokemon();
            }

            @Override
            public void onFailure(Call<PokemonListResponse> call, Throwable t) {
                loadNextPokemon();
            }
        });
    }

    private void loadNextPokemon() {
        currentAttempt = 1;
        actualizarInsigniaIntento();

        txtWhoHintResult.setText("");
        btnHintType.setEnabled(true);
        btnHintStat.setEnabled(true);
        btnHintCry.setEnabled(true);

        resetearBoton(btnOpt1);
        resetearBoton(btnOpt2);
        resetearBoton(btnOpt3);
        resetearBoton(btnOpt4);

        int pokemonId = random.nextInt(750) + 1;

        service.getPokemon(String.valueOf(pokemonId)).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentPokemon = response.body();
                    displaySilhouette();
                    prepararOpciones(currentPokemon.getName());
                } else {
                    loadNextPokemon();
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(WhoPokemonActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetearBoton(MaterialButton btn) {
        btn.setEnabled(true);
        btn.setBackgroundResource(R.drawable.btn_pokemon_console_light);
        btn.setBackgroundTintList(null);
        btn.setTextColor(Color.parseColor("#111111"));
    }

    private void actualizarInsigniaIntento() {
        if (currentAttempt == 1) {
            txtAttemptBadge.setText("Intento 1 de 3 (100 Pts)");
        } else if (currentAttempt == 2) {
            txtAttemptBadge.setText("Intento 2 de 3 (50 Pts)");
        } else {
            txtAttemptBadge.setText("Intento 3 de 3 (25 Pts)");
        }
    }

    private void prepararOpciones(String correctName) {
        List<String> options = new ArrayList<>();
        options.add(correctName);

        if (!allPokemonNames.isEmpty()) {
            while (options.size() < 4) {
                String randomName = allPokemonNames.get(random.nextInt(allPokemonNames.size()));
                if (!options.contains(randomName)) {
                    options.add(randomName);
                }
            }
        } else {
            // Nombres de respaldo si la API no cargo la lista aún
            options.add("pikachu");
            options.add("charizard");
            options.add("mewtwo");
            Collections.shuffle(options);
        }

        Collections.shuffle(options);

        configurarOpcionBoton(btnOpt1, options.get(0), correctName);
        configurarOpcionBoton(btnOpt2, options.get(1), correctName);
        configurarOpcionBoton(btnOpt3, options.get(2), correctName);
        configurarOpcionBoton(btnOpt4, options.get(3), correctName);
    }

    private void configurarOpcionBoton(MaterialButton btn, String optionName, String correctName) {
        String formatted = optionName.substring(0, 1).toUpperCase(Locale.ROOT) + optionName.substring(1);
        btn.setText(formatted);
        btn.setOnClickListener(v -> procesarSeleccion(btn, optionName, correctName));
    }

    private void procesarSeleccion(MaterialButton btnSelected, String selectedName, String correctName) {
        boolean isCorrect = selectedName.equalsIgnoreCase(correctName);

        if (isCorrect) {
            // CORRECTO!
            int pointsGained = (currentAttempt == 1) ? 100 : (currentAttempt == 2) ? 50 : 25;
            score += pointsGained;
            txtWhoScore.setText("Puntos: " + score);

            imgWhoPokemon.clearColorFilter(); // Revelar silueta
            playAudio(currentPokemon != null && currentPokemon.getCries() != null ? currentPokemon.getCries().getLatest() : null);

            btnSelected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btnSelected.setTextColor(Color.WHITE);

            deshabilitarBotonesOpcion();

            if (score > record) {
                record = score;
                txtWhoRecord.setText("Récord: " + record);
                saveUserRecord();
            }

            txtWhoHintResult.setText("¡CORRECTO! Es " + formattedName(correctName) + " (+" + pointsGained + " Pts)");
            new Handler(Looper.getMainLooper()).postDelayed(this::loadNextPokemon, 2200);

        } else {
            // INCORRECTO!
            btnSelected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
            btnSelected.setTextColor(Color.WHITE);
            btnSelected.setEnabled(false);

            if (currentAttempt < 3) {
                currentAttempt++;
                actualizarInsigniaIntento();
                txtWhoHintResult.setText("¡Incorrecto! Puntos disponibles: " + (currentAttempt == 2 ? "50 Pts" : "25 Pts"));
            } else {
                // Se agotaron los 3 intentos
                imgWhoPokemon.clearColorFilter(); // Revelar silueta
                deshabilitarBotonesOpcion();
                resaltarBotonCorrecto(correctName);

                txtWhoHintResult.setText("¡Agotaste los 3 intentos! Era " + formattedName(correctName) + " (0 Pts)");
                new Handler(Looper.getMainLooper()).postDelayed(this::loadNextPokemon, 2500);
            }
        }
    }

    private void resaltarBotonCorrecto(String correctName) {
        MaterialButton[] buttons = new MaterialButton[]{btnOpt1, btnOpt2, btnOpt3, btnOpt4};
        for (MaterialButton btn : buttons) {
            if (btn.getText().toString().equalsIgnoreCase(correctName)) {
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                btn.setTextColor(Color.WHITE);
            }
        }
    }

    private void deshabilitarBotonesOpcion() {
        btnOpt1.setEnabled(false);
        btnOpt2.setEnabled(false);
        btnOpt3.setEnabled(false);
        btnOpt4.setEnabled(false);
    }

    private String formattedName(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        return raw.substring(0, 1).toUpperCase(Locale.ROOT) + raw.substring(1);
    }

    private void displaySilhouette() {
        if (currentPokemon == null || currentPokemon.getSprites() == null) return;

        imgWhoPokemon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        Glide.with(this)
                .load(currentPokemon.getSprites().getFrontDefault())
                .into(imgWhoPokemon);
    }

    private void giveTypeHint() {
        if (currentPokemon == null || currentPokemon.getTypes() == null) return;
        btnHintType.setEnabled(false);

        StringBuilder sb = new StringBuilder("Tipo(s): ");
        for (Pokemon.PokemonType pt : currentPokemon.getTypes()) {
            if (pt.getType() != null) {
                sb.append(pt.getType().getName().toUpperCase(Locale.ROOT)).append(" ");
            }
        }
        txtWhoHintResult.setText(sb.toString().trim());
    }

    private void giveStatHint() {
        if (currentPokemon == null || currentPokemon.getStats() == null) return;
        btnHintStat.setEnabled(false);

        int hp = 0;
        for (PokemonStat s : currentPokemon.getStats()) {
            if (s.getStat() != null && "hp".equals(s.getStat().getName())) {
                hp = s.getBaseStat();
                break;
            }
        }
        txtWhoHintResult.setText("Puntos de Salud (HP Base): " + hp);
    }

    private void giveCryHint() {
        if (currentPokemon == null || currentPokemon.getCries() == null) return;
        btnHintCry.setEnabled(false);

        playAudio(currentPokemon.getCries().getLatest());
        txtWhoHintResult.setText("Escuchando sonido...");
    }

    private void saveUserRecord() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("record", record);

        db.collection("users").document(user.getUid())
                .collection("records").document("who_is_that")
                .set(data);
    }

    private void playAudio(String url) {
        if (url == null) return;
        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}