package com.cesba.pokeapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
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

public class BattleActivity extends AppCompatActivity {

    private ImageView imgPlayer, imgEnemy;
    private TextView txtPlayerName, txtEnemyName;
    private TextView txtPlayerStatus, txtEnemyStatus;
    private ProgressBar pbPlayerHp, pbEnemyHp;
    private TextView txtPlayerHpNumbers, txtEnemyHpNumbers, txtBattleLog;
    private MaterialButton btnTogglePause, btnSpeedUp;
    private RelativeLayout layoutBattleLoading, layoutBattleArea;

    private PokeApiService service;
    private Pokemon playerPokemon;
    private Pokemon enemyPokemon;
    private final List<Pokemon.PokemonMove> playerMoves = new ArrayList<>();
    private final List<Pokemon.PokemonMove> enemyMoves = new ArrayList<>();
    private final List<String> battleLogHistory = new ArrayList<>();

    private int playerMaxHp, playerCurrentHp;
    private int enemyMaxHp, enemyCurrentHp;
    private int turnNumber = 0;

    private int playerAtkMod = 0, playerDefMod = 0, playerSpAtkMod = 0, playerSpDefMod = 0, playerSpdMod = 0;
    private int enemyAtkMod = 0, enemyDefMod = 0, enemySpAtkMod = 0, enemySpDefMod = 0, enemySpdMod = 0;
    private TextView txtPlayerStatMods, txtEnemyStatMods;

    private String playerStatus = "none";
    private String enemyStatus = "none";
    private int playerConfusionTurns = 0;
    private int enemyConfusionTurns = 0;

    private boolean playerFlinched = false;
    private boolean enemyFlinched = false;

    private MediaPlayer mediaPlayer;
    private final Random random = new Random();
    private boolean isPlayerTurn = true;
    private boolean isBattleEnded = false;

    private final Handler autoBattleHandler = new Handler(Looper.getMainLooper());
    private boolean isPaused = false;
    private int turnDelayMs = 1800; // 1.8 segundos por turno

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        service = ApiClient.getClient().create(PokeApiService.class);

        imgPlayer = findViewById(R.id.imgPlayer);
        imgEnemy = findViewById(R.id.imgEnemy);
        txtPlayerName = findViewById(R.id.txtPlayerName);
        txtEnemyName = findViewById(R.id.txtEnemyName);
        txtPlayerStatus = findViewById(R.id.txtPlayerStatus);
        txtEnemyStatus = findViewById(R.id.txtEnemyStatus);
        pbPlayerHp = findViewById(R.id.pbPlayerHp);
        pbEnemyHp = findViewById(R.id.pbEnemyHp);
        txtPlayerHpNumbers = findViewById(R.id.txtPlayerHpNumbers);
        txtEnemyHpNumbers = findViewById(R.id.txtEnemyHpNumbers);
        txtPlayerStatMods = findViewById(R.id.txtPlayerStatMods);
        txtEnemyStatMods = findViewById(R.id.txtEnemyStatMods);
        txtBattleLog = findViewById(R.id.txtBattleLog);
        btnTogglePause = findViewById(R.id.btnTogglePause);
        btnSpeedUp = findViewById(R.id.btnSpeedUp);

        layoutBattleLoading = findViewById(R.id.layoutBattleLoading);
        layoutBattleArea = findViewById(R.id.layoutBattleArea);

        String p1Name = getIntent().getStringExtra("PLAYER_1");
        String p2Name = getIntent().getStringExtra("PLAYER_2");

        btnTogglePause.setOnClickListener(v -> {
            isPaused = !isPaused;
            btnTogglePause.setText(isPaused ? "▶️ Reanudar Simulación" : "⏸️ Pausar Simulación");
            if (!isPaused) {
                autoBattleHandler.post(this::executeNextAutoTurn);
            }
        });

        btnSpeedUp.setOnClickListener(v -> {
            turnDelayMs = (turnDelayMs == 1800) ? 800 : 1800;
            btnSpeedUp.setText(turnDelayMs == 800 ? "⚡ Modo Rápido (2x)" : "⚡ Modo Normal (1x)");
        });

        fetchPokemons(p1Name, p2Name);
    }

    private void fetchPokemons(String p1, String p2) {
        service.getPokemon(p1).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    playerPokemon = response.body();
                    checkBothLoaded();
                } else {
                    handleLoadError();
                }
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) { handleLoadError(); }
        });

        service.getPokemon(p2).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    enemyPokemon = response.body();
                    checkBothLoaded();
                } else {
                    handleLoadError();
                }
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) { handleLoadError(); }
        });
    }

    private void handleLoadError() {
        Toast.makeText(this, "El Pokémon solicitado no existe. Verifica el nombre o ID.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void checkBothLoaded() {
        if (playerPokemon != null && enemyPokemon != null) {
            setupBattle();
        }
    }

    private void setupBattle() {
        layoutBattleLoading.setVisibility(View.GONE);

        // Setup Player
        txtPlayerName.setText(playerPokemon.getName().toUpperCase(Locale.ROOT));
        if (playerPokemon.getSprites() != null) {
            Glide.with(this).load(playerPokemon.getSprites().getShowdownBack(false)).into(imgPlayer);
        }
        playerMaxHp = getStat(playerPokemon, "hp");
        playerCurrentHp = playerMaxHp;
        pbPlayerHp.setMax(playerMaxHp);
        pbPlayerHp.setProgress(playerCurrentHp);
        updatePlayerHpText();

        // Setup Enemy
        txtEnemyName.setText(enemyPokemon.getName().toUpperCase(Locale.ROOT));
        if (enemyPokemon.getSprites() != null) {
            Glide.with(this).load(enemyPokemon.getSprites().getShowdownFront(false)).into(imgEnemy);
        }
        enemyMaxHp = getStat(enemyPokemon, "hp");
        enemyCurrentHp = enemyMaxHp;
        pbEnemyHp.setMax(enemyMaxHp);
        pbEnemyHp.setProgress(enemyCurrentHp);
        updateEnemyHpText();

        // Moves
        extractMoves(playerPokemon, playerMoves);
        extractMoves(enemyPokemon, enemyMoves);

        playAudio(playerPokemon.getCries() != null ? playerPokemon.getCries().getLatest() : null);

        startAutoBattle();
    }

    private void extractMoves(Pokemon p, List<Pokemon.PokemonMove> targetList) {
        targetList.clear();
        targetList.addAll(OptimalMovesetUtils.getOptimalMoveset(p));
    }

    private void startAutoBattle() {
        if (isBattleEnded) return;

        int playerSpeed = getStat(playerPokemon, "speed");
        int enemySpeed = getStat(enemyPokemon, "speed");

        isPlayerTurn = (playerSpeed >= enemySpeed);

        String firstPokemon = isPlayerTurn ? playerPokemon.getName().toUpperCase(Locale.ROOT) : enemyPokemon.getName().toUpperCase(Locale.ROOT);
        int firstSpeed = isPlayerTurn ? playerSpeed : enemySpeed;

        txtBattleLog.setText("¡" + firstPokemon + " es más veloz (Vel: " + firstSpeed + ") y toma la delantera!");

        autoBattleHandler.postDelayed(this::executeNextAutoTurn, turnDelayMs);
    }

    private void executeNextAutoTurn() {
        if (isBattleEnded || isPaused || isFinishing()) return;

        if (playerCurrentHp <= 0 || enemyCurrentHp <= 0) {
            checkBattleEnd();
            return;
        }

        turnNumber++;

        if (isPlayerTurn) {
            Pokemon.PokemonMove selectedMove = (playerMoves != null && !playerMoves.isEmpty())
                    ? playerMoves.get(random.nextInt(playerMoves.size())) : null;
            String moveName = (selectedMove != null && selectedMove.getMove() != null)
                    ? selectedMove.getMove().getName() : "placaje";

            performAutoAttack(moveName, true);
        } else {
            Pokemon.PokemonMove selectedMove = (enemyMoves != null && !enemyMoves.isEmpty())
                    ? enemyMoves.get(random.nextInt(enemyMoves.size())) : null;
            String moveName = (selectedMove != null && selectedMove.getMove() != null)
                    ? selectedMove.getMove().getName() : "placaje";

            performAutoAttack(moveName, false);
        }
    }

    private void performAutoAttack(String moveName, boolean isPlayer) {
        if (isBattleEnded || isFinishing()) return;

        String attackerName = isPlayer ? playerPokemon.getName().toUpperCase(Locale.ROOT) : enemyPokemon.getName().toUpperCase(Locale.ROOT);
        String status = isPlayer ? playerStatus : enemyStatus;

        // 1. Verificación de Retroceso (Flinch)
        boolean isFlinched = isPlayer ? playerFlinched : enemyFlinched;
        if (isFlinched) {
            if (isPlayer) playerFlinched = false; else enemyFlinched = false;
            txtBattleLog.setText("Turno " + turnNumber + ": ¡" + attackerName + " amedrentó y retrocedió! ¡No pudo atacar!");
            isPlayerTurn = !isPlayerTurn;
            autoBattleHandler.postDelayed(this::executeNextAutoTurn, turnDelayMs);
            return;
        }

        // 2. Verificación de Parálisis
        if ("PAR".equals(status) && random.nextInt(100) < 25) {
            txtBattleLog.setText("Turno " + turnNumber + ": ¡" + attackerName + " está paralizado y no puede moverse!");
            isPlayerTurn = !isPlayerTurn;
            autoBattleHandler.postDelayed(this::executeNextAutoTurn, turnDelayMs);
            return;
        }

        // 3. Verificación de Confusión
        int confusionTurns = isPlayer ? playerConfusionTurns : enemyConfusionTurns;
        if (confusionTurns > 0) {
            if (isPlayer) playerConfusionTurns--; else enemyConfusionTurns--;
            if (random.nextInt(100) < 33) {
                txtBattleLog.setText("Turno " + turnNumber + ": ¡" + attackerName + " está confundido y se hirió a sí mismo!");
                int selfDamage = 10 + random.nextInt(10);
                applyDamageToSelf(selfDamage, isPlayer);

                if (checkBattleEnd()) return;

                isPlayerTurn = !isPlayerTurn;
                autoBattleHandler.postDelayed(this::executeNextAutoTurn, turnDelayMs);
                return;
            }
        }

        service.getMove(moveName).enqueue(new Callback<MoveDetail>() {
            @Override
            public void onResponse(Call<MoveDetail> call, Response<MoveDetail> response) {
                MoveDetail detail = (response.isSuccessful() && response.body() != null) ? response.body() : null;
                executeAutoMoveEffect(detail, moveName, isPlayer);
            }

            @Override
            public void onFailure(Call<MoveDetail> call, Throwable t) {
                executeAutoMoveEffect(null, moveName, isPlayer);
            }
        });
    }

    private void executeAutoMoveEffect(MoveDetail move, String moveName, boolean isPlayer) {
        if (isBattleEnded || isFinishing()) return;

        String attackerName = isPlayer ? playerPokemon.getName().toUpperCase(Locale.ROOT) : enemyPokemon.getName().toUpperCase(Locale.ROOT);
        String defenderName = isPlayer ? enemyPokemon.getName().toUpperCase(Locale.ROOT) : playerPokemon.getName().toUpperCase(Locale.ROOT);

        int power = (move != null && move.getPower() != null) ? move.getPower() : 40;
        String moveType = (move != null && move.getType() != null) ? move.getType().getName() : "normal";
        String dmgClass = (move != null && move.getDamageClass() != null) ? move.getDamageClass().getName() : "physical";

        // Multi-hit Check
        int hitCount = 1;
        if (move != null && move.getMeta() != null && move.getMeta().getMaxHits() > 1) {
            int minH = move.getMeta().getMinHits();
            int maxH = move.getMeta().getMaxHits();
            if (maxH > minH) {
                hitCount = minH + random.nextInt(maxH - minH + 1);
            } else {
                hitCount = maxH;
            }
        }

        ImageView attackerImg = isPlayer ? imgPlayer : imgEnemy;
        ImageView defenderImg = isPlayer ? imgEnemy : imgPlayer;

        // Reproducir Cry del Pokémon Atacante en cada ataque
        Pokemon attackerObj = isPlayer ? playerPokemon : enemyPokemon;
        Pokemon defenderObj = isPlayer ? enemyPokemon : playerPokemon;

        if (attackerObj != null && attackerObj.getCries() != null && attackerObj.getCries().getLatest() != null) {
            playAudio(attackerObj.getCries().getLatest());
        }

        // Animar Ataque con BattleAnimationUtils
        final int finalHitCount = hitCount;
        BattleAnimationUtils.playAttackAnimation(
                attackerImg,
                defenderImg,
                isPlayer,
                moveType,
                dmgClass,
                () -> {
                    if (isBattleEnded || isFinishing()) return;

                    StringBuilder logText = new StringBuilder();

                    // Prioridad de Ataque
                    if (move != null && move.getPriority() > 0) {
                        logText.append("⚡ ¡Ataque prioritario! ");
                    }

                    if (move != null) {
                        if (move.getStatChanges() != null && !move.getStatChanges().isEmpty()) {
                            boolean targetIsUser = move.getTarget() != null && (move.getTarget().getName().contains("user") || move.getTarget().getName().contains("self"));
                            for (MoveDetail.StatChange sc : move.getStatChanges()) {
                                if (sc.getStat() != null) {
                                    if (targetIsUser) applyStatModifier(isPlayer, sc.getStat().getName(), sc.getChange(), logText);
                                    else applyStatModifier(!isPlayer, sc.getStat().getName(), sc.getChange(), logText);
                                }
                            }
                        }
                        if (move.getMeta() != null) {
                            if (move.getMeta().getAilment() != null) {
                                String ailmentName = move.getMeta().getAilment().getName();
                                if (ailmentName != null && !"none".equals(ailmentName)) {
                                    boolean targetIsUser = move.getTarget() != null && (move.getTarget().getName().contains("user") || move.getTarget().getName().contains("self"));
                                    applyStatusAilment(targetIsUser ? isPlayer : !isPlayer, ailmentName, logText);
                                }
                            }
                            // Retroceso (Flinch)
                            if (move.getMeta().getFlinchChance() > 0) {
                                if (random.nextInt(100) < move.getMeta().getFlinchChance()) {
                                    if (isPlayer) enemyFlinched = true; else playerFlinched = true;
                                    logText.append(" ¡").append(defenderName).append(" retrocedió por el golpe!\n");
                                }
                            }
                        }
                    }

                    double singleDamage = calculateDamage(
                            attackerObj,
                            defenderObj,
                            power, moveType, dmgClass, isPlayer, logText
                    );

                    double totalDamage = singleDamage * finalHitCount;

                    applyDamage((int) totalDamage, isPlayer);

                    // Drain / Recoil / Life Steal
                    int drainVal = (move != null && move.getMeta() != null) ? move.getMeta().getDrain() : 0;
                    String lowerMove = moveName.toLowerCase(Locale.ROOT);
                    if (drainVal == 0 && (lowerMove.contains("drain") || lowerMove.contains("absorb") || lowerMove.contains("leech"))) {
                        drainVal = 50;
                    }

                    if (totalDamage > 0) {
                        if (drainVal > 0) {
                            int heal = (int) (totalDamage * (drainVal / 100.0));
                            if (heal < 1) heal = 1;
                            applyHealToAttacker(heal, isPlayer);
                            logText.append(" 🩸 ¡Absorbió ").append(heal).append(" HP del rival!\n");
                        } else if (drainVal < 0) {
                            int recoil = (int) (totalDamage * (Math.abs(drainVal) / 100.0));
                            if (recoil < 1) recoil = 1;
                            applyDamageToSelf(recoil, isPlayer);
                            logText.append(" 💥 ¡Sufrió ").append(recoil).append(" HP por retroceso!\n");
                        }
                    }

                    int remainingHp = isPlayer ? enemyCurrentHp : playerCurrentHp;

                    // Reproducir Cry del Defensor si recibió daño efectivo
                    if (totalDamage > 0 && defenderObj != null && defenderObj.getCries() != null && defenderObj.getCries().getLatest() != null) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            playAudio(defenderObj.getCries().getLatest());
                        }, 250);
                    }

                    // Formato del log:
                    String turnLog = "Turno " + turnNumber + ": " + attackerName + " ataca con " + moveName.toUpperCase(Locale.ROOT);
                    if (finalHitCount > 1) {
                        turnLog += " (¡Golpeó " + finalHitCount + " veces!)";
                    }
                    turnLog += ", causa " + (int) totalDamage + " puntos daño, " + defenderName + " queda con " + remainingHp + " HP.";

                    if (logText.length() > 0) {
                        turnLog += "\n" + logText.toString().trim();
                    }

                    battleLogHistory.add(turnLog);
                    txtBattleLog.setText(turnLog);

                    if (checkBattleEnd()) return;

                    isPlayerTurn = !isPlayerTurn;
                    autoBattleHandler.postDelayed(this::executeNextAutoTurn, turnDelayMs);
                }
        );
    }

    private void playTypeAnimation(ImageView target, String type) {
        int color = Color.TRANSPARENT;
        switch (type) {
            case "fire": color = Color.parseColor("#80FF3D00"); break;
            case "water": color = Color.parseColor("#8000B0FF"); break;
            case "electric": color = Color.parseColor("#80FFEA00"); break;
            case "grass": color = Color.parseColor("#8000E676"); break;
            case "poison": color = Color.parseColor("#80AA00FF"); break;
            case "psychic": color = Color.parseColor("#80FF4081"); break;
            case "ice": color = Color.parseColor("#8000E5FF"); break;
        }
        if (color != Color.TRANSPARENT) {
            target.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            new Handler(Looper.getMainLooper()).postDelayed(target::clearColorFilter, 400);
        }
    }

    private void applyStatusAilment(boolean isPlayerTarget, String ailment, StringBuilder logText) {
        String who = isPlayerTarget ? playerPokemon.getName().toUpperCase() : enemyPokemon.getName().toUpperCase();
        TextView statusTv = isPlayerTarget ? txtPlayerStatus : txtEnemyStatus;

        if (ailment.contains("poison")) {
            if (isPlayerTarget) playerStatus = "PSN"; else enemyStatus = "PSN";
            statusTv.setText("PSN");
            statusTv.setBackgroundColor(Color.parseColor("#A040A0"));
            statusTv.setVisibility(View.VISIBLE);
            logText.append("¡").append(who).append(" fue envenenado!\n");
        } else if (ailment.contains("burn")) {
            if (isPlayerTarget) playerStatus = "BRN"; else enemyStatus = "BRN";
            statusTv.setText("BRN");
            statusTv.setBackgroundColor(Color.parseColor("#F08030"));
            statusTv.setVisibility(View.VISIBLE);
            logText.append("¡").append(who).append(" sufrió una quemadura!\n");
        } else if (ailment.contains("paralysis")) {
            if (isPlayerTarget) playerStatus = "PAR"; else enemyStatus = "PAR";
            statusTv.setText("PAR");
            statusTv.setBackgroundColor(Color.parseColor("#F8D030"));
            statusTv.setVisibility(View.VISIBLE);
            logText.append("¡").append(who).append(" quedó paralizado!\n");
        } else if (ailment.contains("freeze")) {
            if (isPlayerTarget) playerStatus = "FRZ"; else enemyStatus = "FRZ";
            statusTv.setText("FRZ");
            statusTv.setBackgroundColor(Color.parseColor("#98D8D8"));
            statusTv.setVisibility(View.VISIBLE);
            logText.append("¡").append(who).append(" quedó congelado!\n");
        } else if (ailment.contains("confus")) {
            if (isPlayerTarget) playerConfusionTurns = 3; else enemyConfusionTurns = 3;
            logText.append("¡").append(who).append(" quedó confundido!\n");
        }
    }

    private void applyHealToAttacker(int healAmount, boolean isPlayer) {
        if (isPlayer) {
            playerCurrentHp = Math.min(playerMaxHp, playerCurrentHp + healAmount);
            pbPlayerHp.setProgress(playerCurrentHp);
            updatePlayerHpText();
        } else {
            enemyCurrentHp = Math.min(enemyMaxHp, enemyCurrentHp + healAmount);
            pbEnemyHp.setProgress(enemyCurrentHp);
            updateEnemyHpText();
        }
    }

    private void applyStatModifier(boolean toPlayer, String statName, int change, StringBuilder log) {
        String who = toPlayer ? playerPokemon.getName().toUpperCase() : enemyPokemon.getName().toUpperCase();
        String changeStr = change > 0 ? "subió" : "bajó";
        String statEs = statName;
        switch (statName) {
            case "attack": statEs = "el Ataque"; if (toPlayer) playerAtkMod += change; else enemyAtkMod += change; break;
            case "defense": statEs = "la Defensa"; if (toPlayer) playerDefMod += change; else enemyDefMod += change; break;
            case "special-attack": statEs = "el At. Especial"; if (toPlayer) playerSpAtkMod += change; else enemySpAtkMod += change; break;
            case "special-defense": statEs = "la Def. Especial"; if (toPlayer) playerSpDefMod += change; else enemySpDefMod += change; break;
            case "speed": statEs = "la Velocidad"; if (toPlayer) playerSpdMod += change; else enemySpdMod += change; break;
        }
        log.append("¡").append(statEs).append(" de ").append(who).append(" ").append(changeStr).append("!\n");
        actualizarInsigniasStats();
    }

    private void actualizarInsigniasStats() {
        actualizarInsigniaStatIndividual(txtPlayerStatMods, playerAtkMod, playerDefMod, playerSpAtkMod, playerSpDefMod, playerSpdMod);
        actualizarInsigniaStatIndividual(txtEnemyStatMods, enemyAtkMod, enemyDefMod, enemySpAtkMod, enemySpDefMod, enemySpdMod);
    }

    private void actualizarInsigniaStatIndividual(TextView tv, int atk, int def, int spAtk, int spDef, int spd) {
        if (tv == null) return;

        StringBuilder sb = new StringBuilder();
        if (atk != 0) sb.append(atk > 0 ? "⚔️+" : "⚔️").append(atk).append(" Atk ");
        if (def != 0) sb.append(def > 0 ? "🛡️+" : "🛡️").append(def).append(" Def ");
        if (spAtk != 0) sb.append(spAtk > 0 ? "✨+" : "✨").append(spAtk).append(" SpAtk ");
        if (spDef != 0) sb.append(spDef > 0 ? "🔰+" : "🔰").append(spDef).append(" SpDef ");
        if (spd != 0) sb.append(spd > 0 ? "⚡+" : "⚡").append(spd).append(" Vel ");

        String text = sb.toString().trim();
        if (text.isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(text);
            tv.setTextColor(text.contains("+") ? Color.parseColor("#2E7D32") : Color.parseColor("#E65100"));
            tv.setVisibility(View.VISIBLE);
        }
    }

    private double getModMultiplier(int stage) {
        if (stage > 6) stage = 6;
        if (stage < -6) stage = -6;
        if (stage >= 0) return (2.0 + stage) / 2.0;
        else return 2.0 / (2.0 - stage);
    }

    private double calculateDamage(Pokemon attacker, Pokemon defender, int power, String moveType, String dmgClass, boolean isPlayer, StringBuilder logText) {
        double stab = 1.0;
        if (attacker.getTypes() != null) {
            for (Pokemon.PokemonType pt : attacker.getTypes()) {
                if (pt.getType() != null && moveType.equals(pt.getType().getName())) {
                    stab = 1.5;
                    break;
                }
            }
        }

        double effectiveness = 1.0;
        if (defender.getTypes() != null) {
            for (Pokemon.PokemonType pt : defender.getTypes()) {
                if (pt.getType() != null) {
                    effectiveness *= TypeUtils.getMultiplier(moveType, pt.getType().getName());
                }
            }
        }

        if (effectiveness > 1.1) logText.append("¡Es súper efectivo!\n");
        else if (effectiveness > 0 && effectiveness < 0.9) logText.append("No es muy efectivo...\n");
        else if (effectiveness == 0) {
            logText.append("¡No afectó a ").append(defender.getName().toUpperCase()).append("!\n");
            return 0;
        }

        double attackStat, defenseStat;
        if ("special".equals(dmgClass)) {
            attackStat = getStat(attacker, "special-attack") * getModMultiplier(isPlayer ? playerSpAtkMod : enemySpAtkMod);
            defenseStat = getStat(defender, "special-defense") * getModMultiplier(isPlayer ? enemySpDefMod : playerSpDefMod);
        } else {
            attackStat = getStat(attacker, "attack") * getModMultiplier(isPlayer ? playerAtkMod : enemyAtkMod);
            defenseStat = getStat(defender, "defense") * getModMultiplier(isPlayer ? enemyDefMod : playerDefMod);
        }

        double damage = (((2.0 * 50 / 5.0 + 2.0) * power * (attackStat / defenseStat)) / 50.0 + 2.0) * stab * effectiveness;

        if ("physical".equals(dmgClass) && "BRN".equals(isPlayer ? playerStatus : enemyStatus)) {
            damage *= 0.5;
        }

        double randomRoll = 0.85 + (random.nextDouble() * 0.15);
        damage *= randomRoll;

        return damage;
    }

    private void applyDamageToSelf(int damage, boolean isPlayer) {
        if (isPlayer) {
            playerCurrentHp -= damage;
            if (playerCurrentHp < 0) playerCurrentHp = 0;
            pbPlayerHp.setProgress(playerCurrentHp);
            updatePlayerHpText();
        } else {
            enemyCurrentHp -= damage;
            if (enemyCurrentHp < 0) enemyCurrentHp = 0;
            pbEnemyHp.setProgress(enemyCurrentHp);
            updateEnemyHpText();
        }
    }

    private void applyDamage(int damage, boolean isPlayer) {
        if (isPlayer) {
            enemyCurrentHp -= damage;
            if (enemyCurrentHp < 0) enemyCurrentHp = 0;
            pbEnemyHp.setProgress(enemyCurrentHp);
            updateEnemyHpText();
        } else {
            playerCurrentHp -= damage;
            if (playerCurrentHp < 0) playerCurrentHp = 0;
            pbPlayerHp.setProgress(playerCurrentHp);
            updatePlayerHpText();
        }
    }

    private void updateEnemyHpText() {
        if (txtEnemyHpNumbers != null) {
            txtEnemyHpNumbers.setText(enemyCurrentHp + "/" + enemyMaxHp);
        }
    }

    private boolean checkBattleEnd() {
        if (enemyCurrentHp <= 0) {
            isBattleEnded = true;
            autoBattleHandler.removeCallbacksAndMessages(null);
            txtBattleLog.setText("¡" + enemyPokemon.getName().toUpperCase() + " se debilitó! ¡Has ganado!");
            playAudio(enemyPokemon.getCries() != null ? enemyPokemon.getCries().getLatest() : null);
            saveBattleResult(true);
            new Handler(Looper.getMainLooper()).postDelayed(() -> showResultDialog(true), 2000);
            return true;
        } else if (playerCurrentHp <= 0) {
            isBattleEnded = true;
            autoBattleHandler.removeCallbacksAndMessages(null);
            txtBattleLog.setText("¡" + playerPokemon.getName().toUpperCase() + " se debilitó! Has perdido.");
            playAudio(playerPokemon.getCries() != null ? playerPokemon.getCries().getLatest() : null);
            saveBattleResult(false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> showResultDialog(false), 2000);
            return true;
        }
        return false;
    }

    private void showResultDialog(boolean userWon) {
        if (isFinishing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_battle_victory, null);

        TextView txtVictoryTitle = dialogView.findViewById(R.id.txtVictoryTitle);
        ImageView imgWinnerSprite = dialogView.findViewById(R.id.imgWinnerSprite);
        TextView txtWinnerName = dialogView.findViewById(R.id.txtWinnerName);
        TextView txtWinnerHp = dialogView.findViewById(R.id.txtWinnerHp);
        TextView txtWinnerTurns = dialogView.findViewById(R.id.txtWinnerTurns);
        TextView txtBattleSummary = dialogView.findViewById(R.id.txtBattleSummary);

        MaterialButton btnRematch = dialogView.findViewById(R.id.btnRematch);
        MaterialButton btnChooseOther = dialogView.findViewById(R.id.btnChooseOther);
        MaterialButton btnReturnMenu = dialogView.findViewById(R.id.btnReturnMenu);

        Pokemon winnerPokemon = userWon ? playerPokemon : enemyPokemon;
        int winnerHp = userWon ? playerCurrentHp : enemyCurrentHp;
        int winnerMaxHp = userWon ? playerMaxHp : enemyMaxHp;

        txtVictoryTitle.setText(userWon ? "¡VICTORIA!" : "¡DERROTA!");
        txtVictoryTitle.setTextColor(userWon ? Color.parseColor("#2E7D32") : Color.parseColor("#DC0A2D"));

        txtWinnerName.setText("Ganador: " + winnerPokemon.getName().toUpperCase(Locale.ROOT));
        txtWinnerHp.setText("HP Restante: " + winnerHp + " / " + winnerMaxHp + " HP");
        txtWinnerTurns.setText("Número de Turnos: " + turnNumber);

        if (txtBattleSummary != null) {
            StringBuilder sb = new StringBuilder();
            for (String line : battleLogHistory) {
                sb.append(line).append("\n\n");
            }
            txtBattleSummary.setText(sb.toString().trim());
        }

        ScrollView scrollSummary = dialogView.findViewById(R.id.scrollBattleSummary);
        if (scrollSummary != null) {
            scrollSummary.setOnTouchListener((v, event) -> {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });
        }

        if (winnerPokemon.getSprites() != null) {
            String sprite = userWon ? winnerPokemon.getSprites().getBackDefault() : winnerPokemon.getSprites().getFrontDefault();
            if (sprite == null) sprite = winnerPokemon.getSprites().getFrontDefault();
            Glide.with(this).load(sprite).into(imgWinnerSprite);
        }

        AlertDialog dialog = builder.setView(dialogView).setCancelable(false).create();

        btnRematch.setOnClickListener(v -> {
            dialog.dismiss();
            recreate();
        });

        btnChooseOther.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        btnReturnMenu.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainMenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    private void saveBattleResult(boolean userWon) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Inicia sesión para guardar tu batalla en el historial", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        List<String> playerTeam = Collections.singletonList(playerPokemon.getName());
        List<String> enemyTeam = Collections.singletonList(enemyPokemon.getName());
        String winner = userWon ? playerPokemon.getName() : enemyPokemon.getName();

        String recordId = db.collection("users").document(user.getUid()).collection("historial").document().getId();

        Map<String, Object> recordData = new HashMap<>();
        recordData.put("id", recordId);
        recordData.put("mode", "Battle Emulator");
        recordData.put("date", dateStr);
        recordData.put("playerTeam", playerTeam);
        recordData.put("enemyTeam", enemyTeam);
        recordData.put("winner", winner);
        recordData.put("userWon", userWon);
        recordData.put("battleLogs", new ArrayList<>(battleLogHistory));

        db.collection("users").document(user.getUid())
                .collection("historial").document(recordId)
                .set(recordData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "¡Batalla guardada en el Historial! 📝", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar en historial: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        Map<String, Object> statsData = new HashMap<>();
        statsData.put(userWon ? "victorias" : "derrotas", FieldValue.increment(1));
        db.collection("users").document(user.getUid())
                .set(statsData, SetOptions.merge());
    }

    private int getStat(Pokemon p, String statName) {
        if (p.getStats() != null) {
            for (PokemonStat s : p.getStats()) {
                if (s.getStat() != null && statName.equals(s.getStat().getName())) {
                    return s.getBaseStat();
                }
            }
        }
        return 50;
    }

    private void updatePlayerHpText() {
        txtPlayerHpNumbers.setText(playerCurrentHp + "/" + playerMaxHp);
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
        autoBattleHandler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}