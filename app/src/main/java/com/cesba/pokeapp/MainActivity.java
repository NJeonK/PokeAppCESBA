package com.cesba.pokeapp;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView etPokemon;
    private MaterialButton btnConsultar;
    private ImageView imgPokemon;

    private TextView txtNombrePokemon, txtIdPokemon;
    private TextView txtAltura, txtPeso, txtDescripcion;
    private LinearLayout layoutTipos;

    private View statHp, statAtk, statDef, statSpAtk, statSpDef, statSpd;

    private ImageView btnBack, btnFavorite, btnPlayCry;
    private View layoutImageContainer;
    private TextView txtShinyHint;
    private GestureDetector gestureDetector;
    private boolean isShinyMode = false;

    private LinearLayout contentHabilidades, contentMovimientos;
    private View headerHabilidades, headerMovimientos;
    private View headerEvoluciones, contentEvoluciones;
    private ImageView imgToggleEvoluciones;
    private LinearLayout layoutEvoluciones;

    private View cardFormasEspeciales;
    private View headerFormas, contentFormas;
    private ImageView imgToggleFormas;
    private LinearLayout layoutFormas;

    private View headerStats, contentStats;
    private ImageView imgToggleHabilidades, imgToggleMovimientos, imgToggleStats;

    private PokeApiService service;
    private MediaPlayer mediaPlayer;
    private boolean isFavorite = false;
    private Pokemon currentPokemon = null;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        enlazarVistas();

        service = ApiClient.getClient()
                .create(PokeApiService.class);

        btnBack.setOnClickListener(v -> finish()); // Cierra esta actividad y regresa a la anterior

        String incomingId = getIntent().getStringExtra("POKEMON_ID");
        if (incomingId != null && !incomingId.isEmpty()) {
            etPokemon.setText(incomingId);
            consultarPokemon(incomingId);
        } else {
            cargarPokemonInicial();
        }

        btnFavorite.setOnClickListener(v -> {
            if (currentPokemon == null) return;
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Inicia sesión para guardar favoritos", Toast.LENGTH_SHORT).show();
                return;
            }

            isFavorite = !isFavorite;
            actualizarIconoFavorito();

            String docId = String.valueOf(currentPokemon.getId());
            if (isFavorite) {
                Map<String, Object> favData = new HashMap<>();
                favData.put("id", currentPokemon.getId());
                favData.put("name", currentPokemon.getName());
                if (currentPokemon.getSprites() != null) {
                    favData.put("image", currentPokemon.getSprites().getFrontDefault());
                }

                db.collection("users").document(user.getUid())
                        .collection("favoritos").document(docId)
                        .set(favData)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Guardado en favoritos", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
            } else {
                db.collection("users").document(user.getUid())
                        .collection("favoritos").document(docId)
                        .delete()
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
            }
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 60;
            private static final int SWIPE_VELOCITY_THRESHOLD = 60;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    toggleShinyMode();
                    return true;
                }
                return false;
            }
        });

        View.OnTouchListener swipeTouchListener = (v, event) -> {
            gestureDetector.onTouchEvent(event);
            v.performClick();
            return true;
        };

        layoutImageContainer.setOnTouchListener(swipeTouchListener);
        imgPokemon.setOnTouchListener(swipeTouchListener);

        btnPlayCry.setOnClickListener(v -> {
            if (currentPokemon != null && currentPokemon.getCries() != null && currentPokemon.getCries().getLatest() != null) {
                playAudio(currentPokemon.getCries().getLatest());
            } else {
                Toast.makeText(this, "Cry no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        headerEvoluciones.setOnClickListener(v -> toggleSection(contentEvoluciones, imgToggleEvoluciones));
        headerFormas.setOnClickListener(v -> toggleSection(contentFormas, imgToggleFormas));
        headerHabilidades.setOnClickListener(v -> toggleSection(contentHabilidades, imgToggleHabilidades));
        headerMovimientos.setOnClickListener(v -> toggleSection(contentMovimientos, imgToggleMovimientos));
        headerStats.setOnClickListener(v -> toggleSection(contentStats, imgToggleStats));

        etPokemon.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            etPokemon.setText(selected);
            consultarPokemon(selected);
        });

        cargarSugerenciasBusqueda();

        btnConsultar.setOnClickListener(v -> {
            String busqueda = etPokemon
                    .getText()
                    .toString()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (busqueda.isEmpty()) {
                Toast.makeText(this, "Escribe un nombre o ID", Toast.LENGTH_SHORT).show();
                return;
            }
            limpiarResultado(); // Reset visual state before new search
            consultarPokemon(busqueda);
        });
    }

    private void cargarSugerenciasBusqueda() {
        service.getPokemonList(1025, 0).enqueue(new Callback<PokemonListResponse>() {
            @Override
            public void onResponse(Call<PokemonListResponse> call, Response<PokemonListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    List<String> names = new ArrayList<>();
                    for (PokemonListResponse.PokemonItem item : response.body().getResults()) {
                        if (item.getName() != null) {
                            names.add(item.getName());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_dropdown_item_1line, names);
                    etPokemon.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<PokemonListResponse> call, Throwable t) { }
        });
    }

    private void actualizarIconoFavorito() {
        btnFavorite.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    private void cargarPokemonInicial() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .collection("favoritos")
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            String favId = doc.getId();
                            etPokemon.setText(favId);
                            consultarPokemon(favId);
                        } else {
                            etPokemon.setText("pikachu");
                            consultarPokemon("pikachu");
                        }
                    })
                    .addOnFailureListener(e -> {
                        etPokemon.setText("pikachu");
                        consultarPokemon("pikachu");
                    });
        } else {
            etPokemon.setText("pikachu");
            consultarPokemon("pikachu");
        }
    }

    private void toggleSection(View content, ImageView arrow) {
        boolean isVisible = content.getVisibility() == View.VISIBLE;
        content.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        arrow.setImageResource(isVisible ? android.R.drawable.arrow_down_float : android.R.drawable.arrow_up_float);
    }

    private void playAudio(String url) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
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

    private void toggleShinyMode() {
        isShinyMode = !isShinyMode;
        if (isShinyMode) {
            txtShinyHint.setText("✨ ¡Versión Shiny Activada! (Desliza para Normal) ✨");
            txtShinyHint.setTextColor(0xFFD81B60);
            Toast.makeText(this, "Versión Shiny ✨", Toast.LENGTH_SHORT).show();
        } else {
            txtShinyHint.setText("👈 Desliza para versión Shiny ✨ 👉");
            txtShinyHint.setTextColor(0xFFE65100);
            Toast.makeText(this, "Versión Normal", Toast.LENGTH_SHORT).show();
        }

        if (currentPokemon != null && currentPokemon.getSprites() != null) {
            String spriteUrl = isShinyMode ? currentPokemon.getSprites().getFrontShiny() : currentPokemon.getSprites().getFrontDefault();
            if (spriteUrl != null) {
                Glide.with(this).load(spriteUrl).into(imgPokemon);
            }
        }
    }

    private void enlazarVistas() {
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnPlayCry = findViewById(R.id.btnPlayCry);

        layoutImageContainer = findViewById(R.id.layoutImageContainer);
        txtShinyHint = findViewById(R.id.txtShinyHint);
        
        etPokemon = findViewById(R.id.etPokemon);
        btnConsultar = findViewById(R.id.btnConsultar);
        imgPokemon = findViewById(R.id.imgPokemon);

        txtNombrePokemon = findViewById(R.id.txtNombrePokemon);
        txtIdPokemon = findViewById(R.id.txtIdPokemon);
        layoutTipos = findViewById(R.id.layoutTipos);
        txtAltura = findViewById(R.id.txtAltura);
        txtPeso = findViewById(R.id.txtPeso);
        txtDescripcion = findViewById(R.id.txtDescripcion);

        statHp = findViewById(R.id.statHp);
        statAtk = findViewById(R.id.statAtk);
        statDef = findViewById(R.id.statDef);
        statSpAtk = findViewById(R.id.statSpAtk);
        statSpDef = findViewById(R.id.statSpDef);
        statSpd = findViewById(R.id.statSpd);

        headerEvoluciones = findViewById(R.id.headerEvoluciones);
        contentEvoluciones = findViewById(R.id.contentEvoluciones);
        imgToggleEvoluciones = findViewById(R.id.imgToggleEvoluciones);
        layoutEvoluciones = findViewById(R.id.layoutEvoluciones);

        cardFormasEspeciales = findViewById(R.id.cardFormasEspeciales);
        headerFormas = findViewById(R.id.headerFormas);
        contentFormas = findViewById(R.id.contentFormas);
        imgToggleFormas = findViewById(R.id.imgToggleFormas);
        layoutFormas = findViewById(R.id.layoutFormas);

        headerHabilidades = findViewById(R.id.headerHabilidades);
        contentHabilidades = findViewById(R.id.contentHabilidades);
        imgToggleHabilidades = findViewById(R.id.imgToggleHabilidades);

        headerMovimientos = findViewById(R.id.headerMovimientos);
        contentMovimientos = findViewById(R.id.contentMovimientos);
        imgToggleMovimientos = findViewById(R.id.imgToggleMovimientos);

        headerStats = findViewById(R.id.headerStats);
        contentStats = findViewById(R.id.contentStats);
        imgToggleStats = findViewById(R.id.imgToggleStats);
    }

    private void consultarPokemon(String busqueda) {

        Call<Pokemon> call = service.getPokemon(busqueda);

        call.enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentPokemon = response.body();
                    mostrarPokemon(currentPokemon);
                    cargarEspecie(currentPokemon.getId());
                    comprobarFavorito(currentPokemon.getId());
                } else {
                    limpiarResultado();
                    Toast.makeText(MainActivity.this, "Pokémon no encontrado", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                limpiarResultado();
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void comprobarFavorito(int id) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            isFavorite = false;
            actualizarIconoFavorito();
            return;
        }

        db.collection("users").document(user.getUid())
                .collection("favoritos").document(String.valueOf(id))
                .get()
                .addOnSuccessListener(doc -> {
                    isFavorite = doc.exists();
                    actualizarIconoFavorito();
                })
                .addOnFailureListener(e -> {
                    isFavorite = false;
                    actualizarIconoFavorito();
                });
    }

    private void cargarEspecie(int id) {
        service.getPokemonSpecies(id).enqueue(new Callback<PokemonSpecies>() {
            @Override
            public void onResponse(Call<PokemonSpecies> call, Response<PokemonSpecies> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonSpecies species = response.body();
                    if (species.getFlavorTextEntries() != null) {
                        for (PokemonSpecies.FlavorTextEntry entry : species.getFlavorTextEntries()) {
                            if (entry.getLanguage() != null && "es".equals(entry.getLanguage().getName())) {
                                txtDescripcion.setText(entry.getFlavorText().replace("\n", " ").replace("\f", " "));
                                break;
                            }
                        }
                    }

                    if (species.getEvolutionChain() != null && species.getEvolutionChain().getUrl() != null) {
                        obtenerCadenaEvolutiva(species.getEvolutionChain().getUrl());
                    }

                    if (species.getVarieties() != null && species.getVarieties().size() > 1) {
                        mostrarVariedades(species.getVarieties());
                    } else {
                        cardFormasEspeciales.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onFailure(Call<PokemonSpecies> call, Throwable t) {
                cardFormasEspeciales.setVisibility(View.GONE);
            }
        });
    }

    private void mostrarVariedades(List<PokemonSpecies.Variety> varieties) {
        layoutFormas.removeAllViews();
        if (varieties == null || varieties.size() <= 1) {
            cardFormasEspeciales.setVisibility(View.GONE);
            return;
        }

        cardFormasEspeciales.setVisibility(View.VISIBLE);

        for (PokemonSpecies.Variety variety : varieties) {
            if (variety.getPokemon() == null || variety.getPokemon().getName() == null) continue;

            String formName = variety.getPokemon().getName();
            View view = getLayoutInflater().inflate(R.layout.item_evolution, layoutFormas, false);
            TextView txtEvoName = view.findViewById(R.id.txtEvoName);
            ImageView imgEvoSprite = view.findViewById(R.id.imgEvoSprite);

            String displayName = formatearNombreForma(formName);
            txtEvoName.setText(displayName);

            service.getPokemon(formName).enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getSprites() != null) {
                        String spriteUrl = isShinyMode ? response.body().getSprites().getFrontShiny() : response.body().getSprites().getFrontDefault();
                        Glide.with(MainActivity.this)
                                .load(spriteUrl)
                                .into(imgEvoSprite);
                    }
                }

                @Override
                public void onFailure(Call<Pokemon> call, Throwable t) { }
            });

            view.setOnClickListener(v -> {
                etPokemon.setText(formName);
                consultarPokemon(formName);
            });

            layoutFormas.addView(view);
        }
    }

    private String formatearNombreForma(String rawName) {
        if (rawName == null) return "";
        if (rawName.contains("-mega-x")) return "Mega X";
        if (rawName.contains("-mega-y")) return "Mega Y";
        if (rawName.contains("-mega")) return "Mega";
        if (rawName.contains("-gmax")) return "Gigantamax";
        if (rawName.contains("-alola")) return "Alola";
        if (rawName.contains("-galar")) return "Galar";
        if (rawName.contains("-hisui")) return "Hisui";
        if (rawName.contains("-primal")) return "Primigenio";

        String[] parts = rawName.split("-");
        if (parts.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                sb.append(parts[i].substring(0, 1).toUpperCase(Locale.ROOT)).append(parts[i].substring(1)).append(" ");
            }
            return sb.toString().trim();
        }
        return rawName.substring(0, 1).toUpperCase(Locale.ROOT) + rawName.substring(1);
    }

    private void obtenerCadenaEvolutiva(String url) {
        service.getEvolutionChain(url).enqueue(new Callback<EvolutionChainResponse>() {
            @Override
            public void onResponse(Call<EvolutionChainResponse> call, Response<EvolutionChainResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getChain() != null) {
                    List<String> names = new ArrayList<>();
                    extraerNombresCadena(response.body().getChain(), names);
                    mostrarCadenaEvolutiva(names);
                }
            }

            @Override
            public void onFailure(Call<EvolutionChainResponse> call, Throwable t) { }
        });
    }

    private void extraerNombresCadena(EvolutionChainResponse.Chain chain, List<String> names) {
        if (chain == null) return;
        if (chain.getSpecies() != null && chain.getSpecies().getName() != null) {
            names.add(chain.getSpecies().getName());
        }
        if (chain.getEvolvesTo() != null) {
            for (EvolutionChainResponse.Chain next : chain.getEvolvesTo()) {
                extraerNombresCadena(next, names);
            }
        }
    }

    private void mostrarCadenaEvolutiva(List<String> names) {
        layoutEvoluciones.removeAllViews();
        if (names.isEmpty()) return;

        for (String evoName : names) {
            View view = getLayoutInflater().inflate(R.layout.item_evolution, layoutEvoluciones, false);
            TextView txtEvoName = view.findViewById(R.id.txtEvoName);
            ImageView imgEvoSprite = view.findViewById(R.id.imgEvoSprite);

            txtEvoName.setText(evoName.substring(0, 1).toUpperCase(Locale.ROOT) + evoName.substring(1));

            service.getPokemon(evoName).enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getSprites() != null) {
                        String spriteUrl = isShinyMode ? response.body().getSprites().getFrontShiny() : response.body().getSprites().getFrontDefault();
                        Glide.with(MainActivity.this)
                                .load(spriteUrl)
                                .into(imgEvoSprite);
                    }
                }

                @Override
                public void onFailure(Call<Pokemon> call, Throwable t) { }
            });

            view.setOnClickListener(v -> {
                etPokemon.setText(evoName);
                consultarPokemon(evoName);
            });

            layoutEvoluciones.addView(view);
        }
    }

    private void mostrarPokemon(Pokemon pokemon) {

        txtNombrePokemon.setText(pokemon.getName().substring(0, 1).toUpperCase(Locale.ROOT) + pokemon.getName().substring(1));
        txtIdPokemon.setText(String.format(Locale.ROOT, "#%03d", pokemon.getId()));
        txtAltura.setText("Altura: " + (pokemon.getHeight() / 10.0) + " m");
        txtPeso.setText("Peso: " + (pokemon.getWeight() / 10.0) + " kg");

        if (pokemon.getSprites() != null) {
            String spriteUrl = isShinyMode ? pokemon.getSprites().getFrontShiny() : pokemon.getSprites().getFrontDefault();
            Glide.with(this)
                    .load(spriteUrl)
                    .into(imgPokemon);
        }

        // Tipos
        layoutTipos.removeAllViews();
        if (pokemon.getTypes() != null && !pokemon.getTypes().isEmpty()) {
            for (Pokemon.PokemonType pt : pokemon.getTypes()) {
                if (pt.getType() != null && pt.getType().getName() != null) {
                    View view = getLayoutInflater().inflate(R.layout.item_type, layoutTipos, false);
                    TextView txtTypeName = view.findViewById(R.id.txtTypeName);
                    txtTypeName.setText(pt.getType().getName().toUpperCase(Locale.ROOT));
                    layoutTipos.addView(view);
                }
            }
        }

        // Habilidades
        contentHabilidades.removeAllViews();
        if (pokemon.getAbilities() != null && !pokemon.getAbilities().isEmpty()) {
            for (Pokemon.PokemonAbility pa : pokemon.getAbilities()) {
                if (pa.getAbility() != null && pa.getAbility().getName() != null) {
                    View view = getLayoutInflater().inflate(R.layout.item_ability, contentHabilidades, false);
                    TextView txtAbilityName = view.findViewById(R.id.txtAbilityName);
                    View cardHidden = view.findViewById(R.id.cardHidden);
                    
                    txtAbilityName.setText(pa.getAbility().getName().substring(0, 1).toUpperCase(Locale.ROOT) + pa.getAbility().getName().substring(1));
                    if (pa.isHidden()) {
                        cardHidden.setVisibility(View.VISIBLE);
                    }
                    contentHabilidades.addView(view);
                }
            }
        }

        // Movimientos
        contentMovimientos.removeAllViews();
        if (pokemon.getMoves() != null && !pokemon.getMoves().isEmpty()) {
            List<MoveInfo> levelMoves = new ArrayList<>();
            List<MoveInfo> otherMoves = new ArrayList<>();

            for (Pokemon.PokemonMove pm : pokemon.getMoves()) {
                if (pm.getMove() != null && pm.getMove().getName() != null) {
                    String moveName = pm.getMove().getName().substring(0, 1).toUpperCase(Locale.ROOT) + pm.getMove().getName().substring(1);
                    String method = "N/A";
                    int level = 0;
                    boolean isLevelUp = false;

                    if (pm.getVersionGroupDetails() != null && !pm.getVersionGroupDetails().isEmpty()) {
                        Pokemon.VersionGroupDetail detail = pm.getVersionGroupDetails().get(pm.getVersionGroupDetails().size() - 1);
                        level = detail.getLevelLearnedAt();
                        if (detail.getMoveLearnMethod() != null) {
                            String methodName = detail.getMoveLearnMethod().getName();
                            if ("level-up".equals(methodName)) {
                                method = "Nvl " + level;
                                isLevelUp = true;
                            } else if ("machine".equals(methodName)) {
                                method = "MT/MO";
                            } else if ("egg".equals(methodName)) {
                                method = "Huevo";
                            } else if ("tutor".equals(methodName)) {
                                method = "Tutor";
                            } else {
                                method = methodName.substring(0, 1).toUpperCase(Locale.ROOT) + methodName.substring(1);
                            }
                        }
                    }
                    
                    if (isLevelUp) {
                        levelMoves.add(new MoveInfo(moveName, method, level));
                    } else {
                        otherMoves.add(new MoveInfo(moveName, method, level));
                    }
                }
            }

            Collections.sort(levelMoves, (a, b) -> Integer.compare(a.level, b.level));
            Collections.sort(otherMoves, (a, b) -> a.name.compareTo(b.name));

            for (MoveInfo m : levelMoves) addMoveView(m);
            for (MoveInfo m : otherMoves) addMoveView(m);
        }

        reiniciarStats();

        if (pokemon.getStats() == null) return;

        for (PokemonStat item : pokemon.getStats()) {

            if (item.getStat() == null) continue;

            String nombre = item.getStat().getName();
            int valor = item.getBaseStat();

            switch (nombre) {
                case "hp":
                    actualizarStatView(statHp, "HP", valor);
                    break;
                case "attack":
                    actualizarStatView(statAtk, "Ataque", valor);
                    break;
                case "defense":
                    actualizarStatView(statDef, "Defensa", valor);
                    break;
                case "special-attack":
                    actualizarStatView(statSpAtk, "At. Esp.", valor);
                    break;
                case "special-defense":
                    actualizarStatView(statSpDef, "Def. Esp.", valor);
                    break;
                case "speed":
                    actualizarStatView(statSpd, "Velocidad", valor);
                    break;
            }
        }
    }

    private void addMoveView(MoveInfo m) {
        View view = getLayoutInflater().inflate(R.layout.item_move, contentMovimientos, false);
        TextView txtName = view.findViewById(R.id.txtMoveName);
        TextView txtMethod = view.findViewById(R.id.txtMoveMethod);
        
        txtName.setText(m.name);
        txtMethod.setText(m.method);
        contentMovimientos.addView(view);
    }

    private void actualizarStatView(View statView, String nombre, int valor) {
        TextView txtStatName = statView.findViewById(R.id.txtStatName);
        ProgressBar pbStat = statView.findViewById(R.id.pbStat);
        TextView txtStatValue = statView.findViewById(R.id.txtStatValue);

        txtStatName.setText(nombre);
        pbStat.setProgress(valor);
        txtStatValue.setText(String.valueOf(valor));
    }

    private void reiniciarStats() {
        actualizarStatView(statHp, "HP", 0);
        actualizarStatView(statAtk, "Ataque", 0);
        actualizarStatView(statDef, "Defensa", 0);
        actualizarStatView(statSpAtk, "At. Esp.", 0);
        actualizarStatView(statSpDef, "Def. Esp.", 0);
        actualizarStatView(statSpd, "Velocidad", 0);
    }

    private void limpiarResultado() {
        currentPokemon = null;
        txtNombrePokemon.setText("POKÉMON");
        txtIdPokemon.setText("#---");
        imgPokemon.setImageDrawable(null);
        layoutTipos.removeAllViews();
        txtAltura.setText("Altura: -- m");
        txtPeso.setText("Peso: -- kg");
        txtDescripcion.setText("Descripción no disponible.");
        contentHabilidades.removeAllViews();
        contentMovimientos.removeAllViews();
        isFavorite = false;
        actualizarIconoFavorito();
        reiniciarStats();
    }

    private static class MoveInfo {
        String name;
        String method;
        int level;
        MoveInfo(String name, String method, int level) {
            this.name = name; this.method = method; this.level = level;
        }
    }
}