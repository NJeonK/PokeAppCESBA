package com.cesba.pokeapp;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TowerActivity extends AppCompatActivity {

    private ImageView imgTeacherPhoto, imgPlayerTower, imgEnemyTower;
    private TextView txtTowerLevel, txtTeacherName;
    private TextView txtPlayerNameTower, txtEnemyNameTower;
    private TextView txtPlayerStatusTower, txtEnemyStatusTower;
    private ProgressBar pbPlayerHpTower, pbEnemyHpTower;
    private TextView txtPlayerHpNumbersTower, txtEnemyHpNumbersTower, txtBattleLogTower;
    private TextView txtPlayerStatModsTower, txtEnemyStatModsTower;
    private Button btnAtk1, btnAtk2, btnAtk3, btnAtk4;
    private RelativeLayout layoutTowerLoading;

    private int pAtkMod = 0, pDefMod = 0, pSpAtkMod = 0, pSpDefMod = 0, pSpdMod = 0;
    private int eAtkMod = 0, eDefMod = 0, eSpAtkMod = 0, eSpDefMod = 0, eSpdMod = 0;

    private String pStatus = "none";
    private String eStatus = "none";

    private PokeApiService service;
    private List<Pokemon> playerTeam = new ArrayList<>();
    private Pokemon activePlayerPokemon;
    private Pokemon activeEnemyPokemon;

    private int activePlayerIndex = 0;
    private boolean[] playerFainted = new boolean[]{false, false, false};

    private int currentLevel = 1;
    private int playerMaxHp, playerCurrentHp;
    private int enemyMaxHp, enemyCurrentHp;

    private List<Pokemon.PokemonMove> playerMoves = new ArrayList<>();
    private Random random = new Random();
    private boolean isBattleEnded = false;

    private static final int[] TEACHER_DRAWABLES = new int[]{
            R.drawable.ic_teacher_1,
            R.drawable.ic_teacher_2,
            R.drawable.ic_teacher_3,
            R.drawable.ic_teacher_4,
            R.drawable.ic_teacher_5,
            R.drawable.ic_teacher_6
    };

    private static final int[] LEGENDARY_IDS = new int[]{150, 249, 250, 382, 383, 384, 483, 484, 487, 493};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tower);

        service = ApiClient.getClient().create(PokeApiService.class);

        imgTeacherPhoto = findViewById(R.id.imgTeacherPhoto);
        txtTowerLevel = findViewById(R.id.txtTowerLevel);
        txtTeacherName = findViewById(R.id.txtTeacherName);

        imgPlayerTower = findViewById(R.id.imgPlayerTower);
        imgEnemyTower = findViewById(R.id.imgEnemyTower);
        txtPlayerNameTower = findViewById(R.id.txtPlayerNameTower);
        txtEnemyNameTower = findViewById(R.id.txtEnemyNameTower);

        pbPlayerHpTower = findViewById(R.id.pbPlayerHpTower);
        pbEnemyHpTower = findViewById(R.id.pbEnemyHpTower);
        txtPlayerHpNumbersTower = findViewById(R.id.txtPlayerHpNumbersTower);
        txtEnemyHpNumbersTower = findViewById(R.id.txtEnemyHpNumbersTower);
        txtPlayerStatusTower = findViewById(R.id.txtPlayerStatusTower);
        txtEnemyStatusTower = findViewById(R.id.txtEnemyStatusTower);
        txtPlayerStatModsTower = findViewById(R.id.txtPlayerStatModsTower);
        txtEnemyStatModsTower = findViewById(R.id.txtEnemyStatModsTower);
        txtBattleLogTower = findViewById(R.id.txtBattleLogTower);

        btnAtk1 = findViewById(R.id.btnAtkTower1);
        btnAtk2 = findViewById(R.id.btnAtkTower2);
        btnAtk3 = findViewById(R.id.btnAtkTower3);
        btnAtk4 = findViewById(R.id.btnAtkTower4);

        layoutTowerLoading = findViewById(R.id.layoutTowerLoading);

        ArrayList<String> teamNames = getIntent().getStringArrayListExtra("PLAYER_TEAM");
        if (teamNames == null || teamNames.size() < 3) {
            Toast.makeText(this, "Equipo incompleto", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        disableButtons();
        fetchPlayerTeam(teamNames);
    }

    private void fetchPlayerTeam(List<String> names) {
        for (int i = 0; i < 3; i++) playerTeam.add(null);

        for (int i = 0; i < 3; i++) {
            final int index = i;
            service.getPokemon(names.get(i)).enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        playerTeam.set(index, response.body());
                        checkTeamLoaded();
                    } else handleLoadError();
                }
                @Override public void onFailure(Call<Pokemon> call, Throwable t) { handleLoadError(); }
            });
        }
    }

    private void handleLoadError() {
        Toast.makeText(this, "Error de red al cargar tu equipo", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void checkTeamLoaded() {
        for (Pokemon p : playerTeam) if (p == null) return;
        startLevel(1);
    }

    private void startLevel(int level) {
        currentLevel = level;
        isBattleEnded = false;
        layoutTowerLoading.setVisibility(View.VISIBLE);

        txtTowerLevel.setText("NIVEL " + level + " DE 6");
        txtTeacherName.setText(level == 6 ? "Docente CESBA 6 (JEFE FINAL)" : "Docente CESBA " + level);
        imgTeacherPhoto.setImageResource(TEACHER_DRAWABLES[level - 1]);

        int enemyId = (level == 6) ? LEGENDARY_IDS[random.nextInt(LEGENDARY_IDS.length)] : random.nextInt(800) + 1;

        service.getPokemon(String.valueOf(enemyId)).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activeEnemyPokemon = response.body();
                    setupLevelBattle();
                } else handleLoadError();
            }
            @Override public void onFailure(Call<Pokemon> call, Throwable t) { handleLoadError(); }
        });
    }

    private void setupLevelBattle() {
        layoutTowerLoading.setVisibility(View.GONE);

        loadActivePlayerPokemon(activePlayerIndex);

        // Enemy setup
        txtEnemyNameTower.setText(activeEnemyPokemon.getName().toUpperCase(Locale.ROOT));
        if (activeEnemyPokemon.getSprites() != null) {
            Glide.with(this).load(activeEnemyPokemon.getSprites().getShowdownFront(false)).into(imgEnemyTower);
        }
        enemyMaxHp = getStat(activeEnemyPokemon, "hp");
        enemyCurrentHp = enemyMaxHp;
        pbEnemyHpTower.setMax(enemyMaxHp);
        pbEnemyHpTower.setProgress(enemyCurrentHp);

        txtBattleLogTower.setText("Nivel " + currentLevel + ": ¡Docente CESBA " + currentLevel + " envió a " + activeEnemyPokemon.getName().toUpperCase() + "!");

        new Handler(Looper.getMainLooper()).postDelayed(this::startPlayerTurn, 1500);
    }

    private void loadActivePlayerPokemon(int index) {
        activePlayerIndex = index;
        activePlayerPokemon = playerTeam.get(index);

        txtPlayerNameTower.setText(activePlayerPokemon.getName().toUpperCase(Locale.ROOT));
        if (activePlayerPokemon.getSprites() != null) {
            Glide.with(this).load(activePlayerPokemon.getSprites().getShowdownBack(false)).into(imgPlayerTower);
        }
        playerMaxHp = getStat(activePlayerPokemon, "hp");
        playerCurrentHp = playerMaxHp;
        pbPlayerHpTower.setMax(playerMaxHp);
        pbPlayerHpTower.setProgress(playerCurrentHp);
        txtPlayerHpNumbersTower.setText(playerCurrentHp + "/" + playerMaxHp);

        playerMoves.clear();
        if (activePlayerPokemon.getMoves() != null) {
            for (int i = 0; i < Math.min(4, activePlayerPokemon.getMoves().size()); i++) {
                playerMoves.add(activePlayerPokemon.getMoves().get(i));
            }
        }
        setupButtons();
    }

    private void setupButtons() {
        setupBtn(btnAtk1, playerMoves.size() > 0 ? playerMoves.get(0) : null);
        setupBtn(btnAtk2, playerMoves.size() > 1 ? playerMoves.get(1) : null);
        setupBtn(btnAtk3, playerMoves.size() > 2 ? playerMoves.get(2) : null);
        setupBtn(btnAtk4, playerMoves.size() > 3 ? playerMoves.get(3) : null);
    }

    private void setupBtn(Button b, Pokemon.PokemonMove move) {
        if (move != null) {
            b.setText(move.getMove().getName().toUpperCase(Locale.ROOT));
            b.setOnClickListener(v -> performAttack(move.getMove().getName(), true));
        } else {
            b.setText("-");
            b.setOnClickListener(null);
        }
    }

    private int getStat(Pokemon p, String name) {
        if (p.getStats() != null) {
            for (PokemonStat s : p.getStats()) {
                if (s.getStat() != null && name.equals(s.getStat().getName())) {
                    return s.getBaseStat();
                }
            }
        }
        return 50;
    }

    private void startPlayerTurn() {
        if (isBattleEnded) return;
        txtBattleLogTower.setText("¿Qué hará " + activePlayerPokemon.getName().toUpperCase() + "?");
        enableButtons();
    }

    private void performAttack(String moveName, boolean isPlayer) {
        if (isBattleEnded) return;
        disableButtons();

        String attackerName = isPlayer ? activePlayerPokemon.getName().toUpperCase() : activeEnemyPokemon.getName().toUpperCase();
        txtBattleLogTower.setText(attackerName + " usa " + moveName.toUpperCase() + "...");

        service.getMove(moveName).enqueue(new Callback<MoveDetail>() {
            @Override
            public void onResponse(Call<MoveDetail> call, Response<MoveDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executeMoveEffect(response.body(), moveName, isPlayer);
                } else {
                    executeFallback(moveName, isPlayer);
                }
            }
            @Override
            public void onFailure(Call<MoveDetail> call, Throwable t) { executeFallback(moveName, isPlayer); }
        });
    }

    private void executeMoveEffect(MoveDetail move, String moveName, boolean isPlayer) {
        boolean isStatus = move.getDamageClass() != null && "status".equals(move.getDamageClass().getName());
        int power = move.getPower() != null ? move.getPower() : 0;

        ImageView atkImg = isPlayer ? imgPlayerTower : imgEnemyTower;
        ImageView defImg = isPlayer ? imgEnemyTower : imgPlayerTower;

        ObjectAnimator atkAnim = ObjectAnimator.ofFloat(atkImg, "translationY", 0f, -25f, 0f);
        atkAnim.setDuration(200);
        atkAnim.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isStatus || power == 0) {
                txtBattleLogTower.setText("¡Ataque de estado! Sin daño directo.");
                checkTurnEnd(isPlayer);
            } else {
                ObjectAnimator defAnim = ObjectAnimator.ofFloat(defImg, "translationX", 0f, 15f, -15f, 15f, -15f, 0f);
                defAnim.setDuration(400);
                defAnim.start();

                int damage = (power / 3) + random.nextInt(10);
                if (damage < 1) damage = 1;

                if (isPlayer) {
                    enemyCurrentHp -= damage;
                    if (enemyCurrentHp < 0) enemyCurrentHp = 0;
                    pbEnemyHpTower.setProgress(enemyCurrentHp);
                } else {
                    playerCurrentHp -= damage;
                    if (playerCurrentHp < 0) playerCurrentHp = 0;
                    pbPlayerHpTower.setProgress(playerCurrentHp);
                    txtPlayerHpNumbersTower.setText(playerCurrentHp + "/" + playerMaxHp);
                }

                txtBattleLogTower.setText("¡Causó " + damage + " de daño!");
                new Handler(Looper.getMainLooper()).postDelayed(() -> checkTurnEnd(isPlayer), 1200);
            }
        }, 600);
    }

    private void executeFallback(String moveName, boolean isPlayer) {
        txtBattleLogTower.setText("Error de red.");
        enableButtons();
    }

    private void checkTurnEnd(boolean isPlayerAttacker) {
        if (enemyCurrentHp <= 0) {
            txtBattleLogTower.setText("¡Derrotaste a " + activeEnemyPokemon.getName().toUpperCase() + "!");
            if (currentLevel < 6) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> startLevel(currentLevel + 1), 2000);
            } else {
                completeTowerCampaign();
            }
            return;
        }

        if (playerCurrentHp <= 0) {
            playerFainted[activePlayerIndex] = true;
            txtBattleLogTower.setText("¡Tu " + activePlayerPokemon.getName().toUpperCase() + " se debilitó!");
            if (isAllPlayerFainted()) {
                endTowerGameOver();
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(this::promptPlayerSwitch, 1500);
            }
            return;
        }

        if (isPlayerAttacker) {
            new Handler(Looper.getMainLooper()).postDelayed(this::enemyTurn, 1200);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::startPlayerTurn, 1200);
        }
    }

    private void enemyTurn() {
        if (isBattleEnded) return;
        txtBattleLogTower.setText("Turno del Docente " + currentLevel);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (activeEnemyPokemon.getMoves() != null && !activeEnemyPokemon.getMoves().isEmpty()) {
                int r = random.nextInt(Math.min(4, activeEnemyPokemon.getMoves().size()));
                performAttack(activeEnemyPokemon.getMoves().get(r).getMove().getName(), false);
            } else {
                performAttack("Placaje", false);
            }
        }, 1000);
    }

    private void promptPlayerSwitch() {
        List<String> available = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (!playerFainted[i]) {
                available.add(playerTeam.get(i).getName().toUpperCase(Locale.ROOT));
                indices.add(i);
            }
        }

        CharSequence[] items = available.toArray(new CharSequence[0]);
        new AlertDialog.Builder(this)
                .setTitle("Selecciona tu siguiente Pokémon para la Torre")
                .setCancelable(false)
                .setItems(items, (dialog, which) -> {
                    int chosenIndex = indices.get(which);
                    loadActivePlayerPokemon(chosenIndex);
                    txtBattleLogTower.setText("¡Adelante " + playerTeam.get(chosenIndex).getName().toUpperCase() + "!");
                    new Handler(Looper.getMainLooper()).postDelayed(this::startPlayerTurn, 1500);
                })
                .show();
    }

    private boolean isAllPlayerFainted() {
        return playerFainted[0] && playerFainted[1] && playerFainted[2];
    }

    private void endTowerGameOver() {
        isBattleEnded = true;
        txtBattleLogTower.setText("¡Has sido derrotado en el Nivel " + currentLevel + "!");
        saveTowerResult(false, currentLevel);
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 3500);
    }

    private void completeTowerCampaign() {
        isBattleEnded = true;
        txtBattleLogTower.setText("¡¡FELICIDADES!! ¡SUPERASTE LA TORRE POKÉMON Y GANASTE UNA MEDALLA!");
        unlockNewMedal();
        saveTowerResult(true, 6);
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 4000);
    }

    private void unlockNewMedal() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // Otorgar medalla según nivel/aleatorio
        int medalId = random.nextInt(10) + 1;
        Map<String, Object> medalData = new HashMap<>();
        medalData.put("id", medalId);
        medalData.put("name", "Medalla " + medalId);
        medalData.put("unlocked", true);
        medalData.put("unlockedDate", dateStr);

        db.collection("users").document(user.getUid())
                .collection("medallas").document(String.valueOf(medalId))
                .set(medalData);
    }

    private void saveTowerResult(boolean won, int levelReached) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        List<String> teamNames = new ArrayList<>();
        for (Pokemon p : playerTeam) teamNames.add(p.getName());

        List<String> bossTeam = Collections.singletonList(activeEnemyPokemon != null ? activeEnemyPokemon.getName() : "Docente");
        String winnerStr = won ? "Entrenador (Torre Superada)" : "Docente CESBA " + levelReached;

        String recordId = db.collection("users").document(user.getUid()).collection("historial").document().getId();

        Map<String, Object> recordData = new HashMap<>();
        recordData.put("id", recordId);
        recordData.put("mode", "Torre Pokémon (Nvl " + levelReached + ")");
        recordData.put("date", dateStr);
        recordData.put("playerTeam", teamNames);
        recordData.put("enemyTeam", bossTeam);
        recordData.put("winner", winnerStr);
        recordData.put("userWon", won);

        db.collection("users").document(user.getUid())
                .collection("historial").document(recordId)
                .set(recordData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "¡Progreso de Torre guardado!", Toast.LENGTH_SHORT).show());

        Map<String, Object> statsData = new HashMap<>();
        statsData.put(won ? "victorias" : "derrotas", FieldValue.increment(1));
        db.collection("users").document(user.getUid())
                .set(statsData, SetOptions.merge());
    }

    private void enableButtons() {
        btnAtk1.setEnabled(true);
        btnAtk2.setEnabled(true);
        btnAtk3.setEnabled(true);
        btnAtk4.setEnabled(true);
    }

    private void disableButtons() {
        btnAtk1.setEnabled(false);
        btnAtk2.setEnabled(false);
        btnAtk3.setEnabled(false);
        btnAtk4.setEnabled(false);
    }
}