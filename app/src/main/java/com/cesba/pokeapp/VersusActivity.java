package com.cesba.pokeapp;

import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

public class VersusActivity extends AppCompatActivity {

    private ImageView imgPlayerVersus, imgEnemyVersus;
    private TextView txtPlayerNameVersus, txtEnemyNameVersus;
    private TextView txtPlayerStatusVersus, txtEnemyStatusVersus;
    private ProgressBar pbPlayerHpVersus, pbEnemyHpVersus;
    private TextView txtPlayerHpNumbersVersus, txtBattleLogVersus;
    private TextView txtPlayerStatModsVersus, txtEnemyStatModsVersus;
    private Button btnAtk1, btnAtk2, btnAtk3, btnAtk4;
    private RelativeLayout layoutVersusLoading, layoutVersusArea;

    private int p1AtkMod = 0, p1DefMod = 0, p1SpAtkMod = 0, p1SpDefMod = 0, p1SpdMod = 0;
    private int p2AtkMod = 0, p2DefMod = 0, p2SpAtkMod = 0, p2SpDefMod = 0, p2SpdMod = 0;

    private String p1Status = "none";
    private String p2Status = "none";
    private int p1ConfusionTurns = 0;
    private int p2ConfusionTurns = 0;

    private boolean p1ToxicSpikes = false, p1Spikes = false, p1StealthRock = false;
    private boolean p2ToxicSpikes = false, p2Spikes = false, p2StealthRock = false;

    private ImageView pbP1_1, pbP1_2, pbP1_3;
    private ImageView pbP2_1, pbP2_2, pbP2_3;

    private PokeApiService service;

    private List<Pokemon> teamP1 = new ArrayList<>();
    private List<Pokemon> teamP2 = new ArrayList<>();

    private int activeP1 = 0;
    private int activeP2 = 0;

    private boolean[] faintedP1 = new boolean[]{false, false, false};
    private boolean[] faintedP2 = new boolean[]{false, false, false};

    private int p1CurrentHp, p1MaxHp;
    private int p2CurrentHp, p2MaxHp;

    private List<Pokemon.PokemonMove> activeP1Moves = new ArrayList<>();
    private List<Pokemon.PokemonMove> activeP2Moves = new ArrayList<>();

    private String p1SelectedMove = null;
    private String p2SelectedMove = null;

    private MediaPlayer mediaPlayer;
    private Random random = new Random();
    private boolean isBattleEnded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versus);

        service = ApiClient.getClient().create(PokeApiService.class);

        imgPlayerVersus = findViewById(R.id.imgPlayerVersus);
        imgEnemyVersus = findViewById(R.id.imgEnemyVersus);
        txtPlayerNameVersus = findViewById(R.id.txtPlayerNameVersus);
        txtEnemyNameVersus = findViewById(R.id.txtEnemyNameVersus);
        txtPlayerStatusVersus = findViewById(R.id.txtPlayerStatusVersus);
        txtEnemyStatusVersus = findViewById(R.id.txtEnemyStatusVersus);

        pbPlayerHpVersus = findViewById(R.id.pbPlayerHpVersus);
        pbEnemyHpVersus = findViewById(R.id.pbEnemyHpVersus);
        txtPlayerHpNumbersVersus = findViewById(R.id.txtPlayerHpNumbersVersus);
        txtBattleLogVersus = findViewById(R.id.txtBattleLogVersus);

        txtPlayerStatModsVersus = findViewById(R.id.txtPlayerStatModsVersus);
        txtEnemyStatModsVersus = findViewById(R.id.txtEnemyStatModsVersus);

        btnAtk1 = findViewById(R.id.btnAtkVersus1);
        btnAtk2 = findViewById(R.id.btnAtkVersus2);
        btnAtk3 = findViewById(R.id.btnAtkVersus3);
        btnAtk4 = findViewById(R.id.btnAtkVersus4);

        pbP1_1 = findViewById(R.id.pbP1_1);
        pbP1_2 = findViewById(R.id.pbP1_2);
        pbP1_3 = findViewById(R.id.pbP1_3);

        pbP2_1 = findViewById(R.id.pbP2_1);
        pbP2_2 = findViewById(R.id.pbP2_2);
        pbP2_3 = findViewById(R.id.pbP2_3);

        layoutVersusLoading = findViewById(R.id.layoutVersusLoading);
        layoutVersusArea = findViewById(R.id.layoutVersusArea);

        ArrayList<String> namesP1 = getIntent().getStringArrayListExtra("TEAM_P1");
        ArrayList<String> namesP2 = getIntent().getStringArrayListExtra("TEAM_P2");

        if (namesP1 == null || namesP2 == null || namesP1.size() < 3 || namesP2.size() < 3) {
            Toast.makeText(this, "Equipos incompletos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        disableButtons();
        fetchTeams(namesP1, namesP2);
    }

    private void fetchTeams(List<String> namesP1, List<String> namesP2) {
        for (int i = 0; i < 3; i++) {
            teamP1.add(null);
            teamP2.add(null);
        }

        for (int i = 0; i < 3; i++) {
            final int index = i;
            service.getPokemon(namesP1.get(i)).enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        teamP1.set(index, response.body());
                        checkAllLoaded();
                    } else handleLoadError();
                }
                @Override public void onFailure(Call<Pokemon> call, Throwable t) { handleLoadError(); }
            });

            service.getPokemon(namesP2.get(i)).enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        teamP2.set(index, response.body());
                        checkAllLoaded();
                    } else handleLoadError();
                }
                @Override public void onFailure(Call<Pokemon> call, Throwable t) { handleLoadError(); }
            });
        }
    }

    private void handleLoadError() {
        Toast.makeText(this, "Error al cargar uno de los Pokémon", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void checkAllLoaded() {
        for (Pokemon p : teamP1) if (p == null) return;
        for (Pokemon p : teamP2) if (p == null) return;
        setupVersusBattle();
    }

    private void setupVersusBattle() {
        layoutVersusLoading.setVisibility(View.GONE);
        txtBattleLogVersus.setText("¡Comienza el duelo Versus 3v3!");

        loadP1Pokemon(0);
        loadP2Pokemon(0);

        new Handler(Looper.getMainLooper()).postDelayed(this::startTurnP1, 1500);
    }

    private void loadP1Pokemon(int index) {
        activeP1 = index;
        Pokemon p = teamP1.get(index);
        txtPlayerNameVersus.setText(p.getName().toUpperCase(Locale.ROOT));

        p1Status = "none";
        txtPlayerStatusVersus.setVisibility(View.GONE);

        if (p.getSprites() != null) {
            String gifUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/back/" + p.getId() + ".gif";
            Glide.with(this)
                    .asGif()
                    .load(gifUrl)
                    .error(p.getSprites().getBackDefault())
                    .into(imgPlayerVersus);
        }
        p1MaxHp = getStat(p, "hp");
        p1CurrentHp = p1MaxHp;
        pbPlayerHpVersus.setMax(p1MaxHp);
        pbPlayerHpVersus.setProgress(p1CurrentHp);
        txtPlayerHpNumbersVersus.setText(p1CurrentHp + "/" + p1MaxHp);

        checkAndApplyEntryHazardsP1(p);

        p1AtkMod = 0; p1DefMod = 0; p1SpAtkMod = 0; p1SpDefMod = 0; p1SpdMod = 0;
        actualizarVersusInsigniasStats();

        activeP1Moves.clear();
        activeP1Moves.addAll(OptimalMovesetUtils.getOptimalMoveset(p));
        setupP1MoveButtons();
        playAudio(p.getCries() != null ? p.getCries().getLatest() : null);
    }

    private void loadP2Pokemon(int index) {
        activeP2 = index;
        Pokemon p = teamP2.get(index);
        txtEnemyNameVersus.setText(p.getName().toUpperCase(Locale.ROOT));

        p2Status = "none";
        txtEnemyStatusVersus.setVisibility(View.GONE);

        if (p.getSprites() != null) {
            String gifUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/" + p.getId() + ".gif";
            Glide.with(this)
                    .asGif()
                    .load(gifUrl)
                    .error(p.getSprites().getFrontDefault())
                    .into(imgEnemyVersus);
        }
        p2MaxHp = getStat(p, "hp");
        p2CurrentHp = p2MaxHp;
        pbEnemyHpVersus.setMax(p2MaxHp);
        pbEnemyHpVersus.setProgress(p2CurrentHp);

        checkAndApplyEntryHazardsP2(p);

        p2AtkMod = 0; p2DefMod = 0; p2SpAtkMod = 0; p2SpDefMod = 0; p2SpdMod = 0;
        actualizarVersusInsigniasStats();

        activeP2Moves.clear();
        activeP2Moves.addAll(OptimalMovesetUtils.getOptimalMoveset(p));
    }

    private void startTurnP1() {
        if (isBattleEnded) return;
        p1SelectedMove = null;
        p2SelectedMove = null;

        setVersusLogHtml("Selecciona ataque con " + teamP1.get(activeP1).getName().toUpperCase(Locale.ROOT), true);
        setupP1MoveButtons();
        enableButtons();
    }

    private void startTurnP2() {
        if (isBattleEnded) return;
        disableButtons();
        setVersusLogHtml("Selecciona ataque con " + teamP2.get(activeP2).getName().toUpperCase(Locale.ROOT), false);
        setupP2MoveButtons();
        enableButtons();
    }

    private void setVersusLogHtml(String text, boolean isP1) {
        String colorHex = isP1 ? "#1565C0" : "#DC0A2D";
        String tag = isP1 ? "🔵 JUGADOR 1: " : "🔴 JUGADOR 2: ";
        String html = "<font color='" + colorHex + "'><b>" + tag + "</b>" + text + "</font>";
        txtBattleLogVersus.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
    }

    private void setupP1MoveButtons() {
        setupBtn(btnAtk1, activeP1Moves.size() > 0 ? activeP1Moves.get(0) : null, true);
        setupBtn(btnAtk2, activeP1Moves.size() > 1 ? activeP1Moves.get(1) : null, true);
        setupBtn(btnAtk3, activeP1Moves.size() > 2 ? activeP1Moves.get(2) : null, true);
        setupBtn(btnAtk4, activeP1Moves.size() > 3 ? activeP1Moves.get(3) : null, true);
    }

    private void setupP2MoveButtons() {
        setupBtn(btnAtk1, activeP2Moves.size() > 0 ? activeP2Moves.get(0) : null, false);
        setupBtn(btnAtk2, activeP2Moves.size() > 1 ? activeP2Moves.get(1) : null, false);
        setupBtn(btnAtk3, activeP2Moves.size() > 2 ? activeP2Moves.get(2) : null, false);
        setupBtn(btnAtk4, activeP2Moves.size() > 3 ? activeP2Moves.get(3) : null, false);
    }

    private void setupBtn(Button b, Pokemon.PokemonMove move, boolean isP1) {
        if (move != null) {
            String moveName = move.getMove().getName();
            b.setText(moveName.toUpperCase(Locale.ROOT));

            Pokemon opponent = isP1 ? teamP2.get(activeP2) : teamP1.get(activeP1);

            service.getMove(moveName).enqueue(new Callback<MoveDetail>() {
                @Override
                public void onResponse(Call<MoveDetail> call, Response<MoveDetail> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MoveDetail detail = response.body();
                        String moveType = (detail.getType() != null && detail.getType().getName() != null)
                                ? detail.getType().getName() : "normal";
                        int typeColor = TypeColorUtils.getTypeColor(moveType);

                        b.setBackgroundTintList(ColorStateList.valueOf(typeColor));
                        b.setTextColor(Color.WHITE);

                        b.setOnLongClickListener(v -> {
                            showVersusMoveInfoDialog(detail, opponent);
                            return true;
                        });
                    }
                }

                @Override public void onFailure(Call<MoveDetail> call, Throwable t) { }
            });

            b.setOnClickListener(v -> {
                if (isP1) {
                    p1SelectedMove = moveName;
                    startTurnP2();
                } else {
                    p2SelectedMove = moveName;
                    resolveVersusTurn();
                }
            });
        } else {
            b.setText("-");
            b.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            b.setTextColor(Color.BLACK);
            b.setOnClickListener(null);
            b.setOnLongClickListener(null);
        }
    }

    private void showVersusMoveInfoDialog(MoveDetail moveDetail, Pokemon targetOpponent) {
        if (moveDetail == null || isFinishing()) return;

        String moveName = moveDetail.getName() != null ? moveDetail.getName().toUpperCase(Locale.ROOT) : "MOVIMIENTO";
        String moveType = (moveDetail.getType() != null && moveDetail.getType().getName() != null)
                ? moveDetail.getType().getName().toUpperCase(Locale.ROOT) : "NORMAL";
        int typeColor = TypeColorUtils.getTypeColor(moveType);

        String category = (moveDetail.getDamageClass() != null && moveDetail.getDamageClass().getName() != null)
                ? moveDetail.getDamageClass().getName().toUpperCase(Locale.ROOT) : "FISICO";

        int power = moveDetail.getPower() != null ? moveDetail.getPower() : 0;
        int accuracy = moveDetail.getAccuracy() != null ? moveDetail.getAccuracy() : 100;

        String t1 = "normal";
        String t2 = null;
        if (targetOpponent != null && targetOpponent.getTypes() != null && !targetOpponent.getTypes().isEmpty()) {
            t1 = targetOpponent.getTypes().get(0).getType().getName();
            if (targetOpponent.getTypes().size() > 1) {
                t2 = targetOpponent.getTypes().get(1).getType().getName();
            }
        }

        double mult = TypeUtils.getMultiplier(moveType.toLowerCase(Locale.ROOT), t1);
        if (t2 != null) {
            mult *= TypeUtils.getMultiplier(moveType.toLowerCase(Locale.ROOT), t2);
        }

        String effectivenessText;
        int effColor;
        if (mult >= 2.0) {
            effectivenessText = "🔥 ¡Súper Efectivo! (" + (mult == 4.0 ? "4x" : "2x") + ")";
            effColor = Color.parseColor("#2E7D32");
        } else if (mult == 1.0) {
            effectivenessText = "⚖️ Efectividad Normal (1x)";
            effColor = Color.parseColor("#1565C0");
        } else if (mult > 0.0) {
            effectivenessText = "🛡️ Poco Efectivo (" + (mult == 0.25 ? "0.25x" : "0.5x") + ")";
            effColor = Color.parseColor("#E65100");
        } else {
            effectivenessText = "🚫 Sin Efecto (0x)";
            effColor = Color.parseColor("#C2185B");
        }

        String description = "Categoría: " + category + ". Potencia: " + (power > 0 ? power : "Estado") + ". Precisión: " + accuracy + "%.";
        String lowerType = moveType.toLowerCase(Locale.ROOT);
        if (lowerType.contains("poison") || moveName.contains("POISON") || moveName.contains("TOXIC")) {
            description += "\n💡 Puede causar Veneno (daño por turno).";
        } else if (lowerType.contains("fire") || moveName.contains("BURN") || moveName.contains("FLAME")) {
            description += "\n💡 Puede causar Quemadura (daño por turno y -50% Atk físico).";
        } else if (lowerType.contains("electric") || moveName.contains("THUNDER") || moveName.contains("PARALYZ")) {
            description += "\n💡 Puede causar Parálisis (-50% Vel y 25% inacción).";
        }

        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_move_info, null);

        TextView txtMoveName = dialogView.findViewById(R.id.txtDialogMoveName);
        TextView txtMoveType = dialogView.findViewById(R.id.txtDialogMoveType);
        TextView txtMovePower = dialogView.findViewById(R.id.txtDialogMovePower);
        TextView txtMoveAcc = dialogView.findViewById(R.id.txtDialogMoveAcc);
        TextView txtMoveEff = dialogView.findViewById(R.id.txtDialogMoveEff);
        TextView txtMoveDesc = dialogView.findViewById(R.id.txtDialogMoveDesc);

        txtMoveName.setText(moveName);
        txtMoveType.setText(moveType);
        txtMoveType.setBackgroundColor(typeColor);

        txtMovePower.setText("Potencia: " + (power > 0 ? power : "--"));
        txtMoveAcc.setText("Precisión: " + accuracy + "%");

        txtMoveEff.setText(effectivenessText);
        txtMoveEff.setTextColor(effColor);

        txtMoveDesc.setText(description);

        builder.setView(dialogView);
        builder.setPositiveButton("Entendido", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void resolveVersusTurn() {
        if (isBattleEnded) return;
        disableButtons();

        int p1Speed = getStat(teamP1.get(activeP1), "speed");
        int p2Speed = getStat(teamP2.get(activeP2), "speed");

        boolean p1First = p1Speed >= p2Speed;

        if (p1First) {
            executeAttacksSequentially(p1SelectedMove, true, p2SelectedMove, false);
        } else {
            executeAttacksSequentially(p2SelectedMove, false, p1SelectedMove, true);
        }
    }

    private void executeAttacksSequentially(String move1, boolean isP1_1, String move2, boolean isP1_2) {
        performAttackSingle(move1, isP1_1, () -> {
            boolean defender2Alive = isP1_2 ? (p1CurrentHp > 0) : (p2CurrentHp > 0);
            if (defender2Alive && !isBattleEnded) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    performAttackSingle(move2, isP1_2, this::checkVersusTurnEnd);
                }, 1000);
            } else {
                checkVersusTurnEnd();
            }
        });
    }

    private void performAttackSingle(String moveName, boolean isP1Attacker, Runnable onComplete) {
        if (isBattleEnded) {
            onComplete.run();
            return;
        }

        Pokemon attacker = isP1Attacker ? teamP1.get(activeP1) : teamP2.get(activeP2);
        txtBattleLogVersus.setText(attacker.getName().toUpperCase(Locale.ROOT) + " usa " + moveName.toUpperCase(Locale.ROOT) + "...");

        service.getMove(moveName).enqueue(new Callback<MoveDetail>() {
            @Override
            public void onResponse(Call<MoveDetail> call, Response<MoveDetail> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executeVersusMove(response.body(), moveName, isP1Attacker, onComplete);
                } else {
                    executeFallbackVersus(moveName, isP1Attacker, onComplete);
                }
            }
            @Override
            public void onFailure(Call<MoveDetail> call, Throwable t) {
                executeFallbackVersus(moveName, isP1Attacker, onComplete);
            }
        });
    }

    private void executeVersusMove(MoveDetail move, String moveName, boolean isP1Attacker, Runnable onComplete) {
        boolean isStatus = move.getDamageClass() != null && "status".equals(move.getDamageClass().getName());
        int power = move.getPower() != null ? move.getPower() : 0;
        String moveType = move.getType() != null ? move.getType().getName() : "normal";
        String dmgClass = move.getDamageClass() != null ? move.getDamageClass().getName() : "physical";

        ImageView atkImg = isP1Attacker ? imgPlayerVersus : imgEnemyVersus;
        ImageView defImg = isP1Attacker ? imgEnemyVersus : imgPlayerVersus;

        // Play Attacker Cry Audio on Attack
        Pokemon attackerObj = isP1Attacker ? teamP1.get(activeP1) : teamP2.get(activeP2);
        if (attackerObj != null && attackerObj.getCries() != null && attackerObj.getCries().getLatest() != null) {
            playAudio(attackerObj.getCries().getLatest());
        }

        BattleAnimationUtils.playAttackAnimation(
                atkImg,
                defImg,
                isP1Attacker,
                moveType,
                dmgClass,
                () -> {
                    StringBuilder logText = new StringBuilder();

                    // Evaluate Stat Changes (Swords Dance, Growl, Tail Whip, etc.)
                    if (move.getStatChanges() != null && !move.getStatChanges().isEmpty()) {
                        boolean targetIsUser = move.getTarget() != null && (move.getTarget().getName().contains("user") || move.getTarget().getName().contains("self"));
                        for (MoveDetail.StatChange sc : move.getStatChanges()) {
                            if (sc.getStat() != null) {
                                if (targetIsUser) applyVersusStatModifier(isP1Attacker, sc.getStat().getName(), sc.getChange(), logText);
                                else applyVersusStatModifier(!isP1Attacker, sc.getStat().getName(), sc.getChange(), logText);
                            }
                        }
                    }

                    // Evaluate Ailments (Toxic, Poison, Burn, Paralysis, Freeze)
                    if (move.getMeta() != null && move.getMeta().getAilment() != null) {
                        String ailmentName = move.getMeta().getAilment().getName();
                        if (ailmentName != null && !"none".equals(ailmentName)) {
                            boolean targetIsUser = move.getTarget() != null && (move.getTarget().getName().contains("user") || move.getTarget().getName().contains("self"));
                            applyVersusStatusAilment(targetIsUser ? isP1Attacker : !isP1Attacker, ailmentName, logText);
                        }
                    }

                    String lowerMove = moveName.toLowerCase(Locale.ROOT);
                    if (lowerMove.contains("toxic-spikes")) {
                        if (isP1Attacker) p2ToxicSpikes = true; else p1ToxicSpikes = true;
                        logText.append(" ☣️ ¡Se esparcieron Picos Venenosos!");
                    } else if (lowerMove.contains("stealth-rock") || lowerMove.contains("trampa-rocas")) {
                        if (isP1Attacker) p2StealthRock = true; else p1StealthRock = true;
                        logText.append(" 🪨 ¡Se colocaron Trampa Rocas!");
                    } else if (lowerMove.contains("spikes")) {
                        if (isP1Attacker) p2Spikes = true; else p1Spikes = true;
                        logText.append(" 📌 ¡Se sembraron Púas!");
                    }

                    int drainVal = (move != null && move.getMeta() != null) ? move.getMeta().getDrain() : 0;
                    if (drainVal == 0 && (lowerMove.contains("drain") || lowerMove.contains("absorb") || lowerMove.contains("leech"))) {
                        drainVal = 50;
                    }

                    if (isStatus || power == 0) {
                        String statusMsg = logText.length() > 0 ? logText.toString().trim() : "¡Ataque de estado realizado!";
                        txtBattleLogVersus.setText(statusMsg);
                        new Handler(Looper.getMainLooper()).postDelayed(onComplete, 1400);
                    } else {
                        Pokemon attacker = isP1Attacker ? teamP1.get(activeP1) : teamP2.get(activeP2);
                        Pokemon defender = isP1Attacker ? teamP2.get(activeP2) : teamP1.get(activeP1);

                        double damage = calculateVersusDamage(attacker, defender, power, moveType, dmgClass, isP1Attacker, logText);

                        if (isP1Attacker) {
                            p2CurrentHp -= (int) damage;
                            if (p2CurrentHp < 0) p2CurrentHp = 0;
                            pbEnemyHpVersus.setProgress(p2CurrentHp);
                        } else {
                            p1CurrentHp -= (int) damage;
                            if (p1CurrentHp < 0) p1CurrentHp = 0;
                            pbPlayerHpVersus.setProgress(p1CurrentHp);
                            txtPlayerHpNumbersVersus.setText(p1CurrentHp + "/" + p1MaxHp);
                        }

                        if (damage > 0) {
                            if (drainVal > 0) {
                                int heal = (int) (damage * (drainVal / 100.0));
                                if (heal < 1) heal = 1;
                                applyHealVersus(heal, isP1Attacker);
                                logText.append(" 🩸 ¡Absorbió ").append(heal).append(" HP!");
                            } else if (drainVal < 0) {
                                int recoil = (int) (damage * (Math.abs(drainVal) / 100.0));
                                if (recoil < 1) recoil = 1;
                                applyRecoilVersus(recoil, isP1Attacker);
                                logText.append(" 💥 ¡Sufrió ").append(recoil).append(" HP por retroceso!");
                            }
                        }

                        String hitLog = "¡Causó " + (int) damage + " de daño!";
                        if (logText.length() > 0) {
                            hitLog = logText.toString().trim() + "\n" + hitLog;
                        }
                        txtBattleLogVersus.setText(hitLog);
                        new Handler(Looper.getMainLooper()).postDelayed(onComplete, 1400);
                    }
                }
        );
    }

    private void applyVersusStatModifier(boolean toP1, String statName, int change, StringBuilder log) {
        String who = toP1 ? "J1" : "J2";
        String changeStr = change > 0 ? "subió" : "bajó";
        String statEs = statName;
        switch (statName) {
            case "attack": statEs = "el Ataque"; if (toP1) p1AtkMod += change; else p2AtkMod += change; break;
            case "defense": statEs = "la Defensa"; if (toP1) p1DefMod += change; else p2DefMod += change; break;
            case "special-attack": statEs = "el At. Especial"; if (toP1) p1SpAtkMod += change; else p2SpAtkMod += change; break;
            case "special-defense": statEs = "la Def. Especial"; if (toP1) p1SpDefMod += change; else p2SpDefMod += change; break;
            case "speed": statEs = "la Velocidad"; if (toP1) p1SpdMod += change; else p2SpdMod += change; break;
        }
        log.append("¡").append(statEs).append(" de ").append(who).append(" ").append(changeStr).append("!\n");
        actualizarVersusInsigniasStats();
    }

    private void applyVersusStatusAilment(boolean isP1Target, String ailment, StringBuilder logText) {
        String who = isP1Target ? teamP1.get(activeP1).getName().toUpperCase(Locale.ROOT) : teamP2.get(activeP2).getName().toUpperCase(Locale.ROOT);
        TextView statusTv = isP1Target ? txtPlayerStatusVersus : txtEnemyStatusVersus;

        if (ailment.contains("poison") || ailment.contains("toxic")) {
            if (isP1Target) p1Status = "PSN"; else p2Status = "PSN";
            statusTv.setText("PSN");
            statusTv.setBackgroundColor(Color.parseColor("#A040A0"));
            statusTv.setVisibility(View.VISIBLE);
            logText.append(" ¡").append(who).append(" fue envenenado!");
        } else if (ailment.contains("burn")) {
            if (isP1Target) p1Status = "BRN"; else p2Status = "BRN";
            statusTv.setText("BRN");
            statusTv.setBackgroundColor(Color.parseColor("#F08030"));
            statusTv.setVisibility(View.VISIBLE);
            logText.append(" ¡").append(who).append(" sufrió una quemadura!");
        } else if (ailment.contains("paralysis")) {
            if (isP1Target) p1Status = "PAR"; else p2Status = "PAR";
            statusTv.setText("PAR");
            statusTv.setBackgroundColor(Color.parseColor("#F8D030"));
            statusTv.setVisibility(View.VISIBLE);
            logText.append(" ¡").append(who).append(" quedó paralizado!");
        } else if (ailment.contains("freeze")) {
            if (isP1Target) p1Status = "FRZ"; else p2Status = "FRZ";
            statusTv.setText("FRZ");
            statusTv.setBackgroundColor(Color.parseColor("#98D8D8"));
            statusTv.setVisibility(View.VISIBLE);
            logText.append(" ¡").append(who).append(" quedó congelado!");
        }
    }

    private void checkAndApplyEntryHazardsP1(Pokemon p) {
        if (p1ToxicSpikes) {
            p1Status = "PSN";
            txtPlayerStatusVersus.setText("PSN");
            txtPlayerStatusVersus.setBackgroundColor(Color.parseColor("#A040A0"));
            txtPlayerStatusVersus.setVisibility(View.VISIBLE);
            Toast.makeText(this, "¡" + p.getName().toUpperCase(Locale.ROOT) + " pisó Picos Venenosos!", Toast.LENGTH_SHORT).show();
        }
        if (p1Spikes) {
            int spikeDmg = Math.max(1, p1MaxHp / 8);
            p1CurrentHp = Math.max(0, p1CurrentHp - spikeDmg);
            pbPlayerHpVersus.setProgress(p1CurrentHp);
            txtPlayerHpNumbersVersus.setText(p1CurrentHp + "/" + p1MaxHp);
            Toast.makeText(this, "¡" + p.getName().toUpperCase(Locale.ROOT) + " sufrió daño por las Púas!", Toast.LENGTH_SHORT).show();
        }
        if (p1StealthRock) {
            double rockMult = 1.0;
            if (p.getTypes() != null) {
                for (Pokemon.PokemonType pt : p.getTypes()) {
                    if (pt.getType() != null) {
                        rockMult *= TypeUtils.getMultiplier("rock", pt.getType().getName());
                    }
                }
            }
            int rockDmg = (int) (p1MaxHp * (0.125 * rockMult));
            if (rockDmg < 1) rockDmg = 1;
            p1CurrentHp = Math.max(0, p1CurrentHp - rockDmg);
            pbPlayerHpVersus.setProgress(p1CurrentHp);
            txtPlayerHpNumbersVersus.setText(p1CurrentHp + "/" + p1MaxHp);
            Toast.makeText(this, "¡Las Trampa Rocas hirieron a " + p.getName().toUpperCase(Locale.ROOT) + "!", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndApplyEntryHazardsP2(Pokemon p) {
        if (p2ToxicSpikes) {
            p2Status = "PSN";
            txtEnemyStatusVersus.setText("PSN");
            txtEnemyStatusVersus.setBackgroundColor(Color.parseColor("#A040A0"));
            txtEnemyStatusVersus.setVisibility(View.VISIBLE);
            Toast.makeText(this, "¡" + p.getName().toUpperCase(Locale.ROOT) + " pisó Picos Venenosos!", Toast.LENGTH_SHORT).show();
        }
        if (p2Spikes) {
            int spikeDmg = Math.max(1, p2MaxHp / 8);
            p2CurrentHp = Math.max(0, p2CurrentHp - spikeDmg);
            pbEnemyHpVersus.setProgress(p2CurrentHp);
            Toast.makeText(this, "¡" + p.getName().toUpperCase(Locale.ROOT) + " sufrió daño por las Púas!", Toast.LENGTH_SHORT).show();
        }
        if (p2StealthRock) {
            double rockMult = 1.0;
            if (p.getTypes() != null) {
                for (Pokemon.PokemonType pt : p.getTypes()) {
                    if (pt.getType() != null) {
                        rockMult *= TypeUtils.getMultiplier("rock", pt.getType().getName());
                    }
                }
            }
            int rockDmg = (int) (p2MaxHp * (0.125 * rockMult));
            if (rockDmg < 1) rockDmg = 1;
            p2CurrentHp = Math.max(0, p2CurrentHp - rockDmg);
            pbEnemyHpVersus.setProgress(p2CurrentHp);
            Toast.makeText(this, "¡Las Trampa Rocas hirieron a " + p.getName().toUpperCase(Locale.ROOT) + "!", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyHealVersus(int heal, boolean isP1) {
        if (isP1) {
            p1CurrentHp = Math.min(p1MaxHp, p1CurrentHp + heal);
            pbPlayerHpVersus.setProgress(p1CurrentHp);
            txtPlayerHpNumbersVersus.setText(p1CurrentHp + "/" + p1MaxHp);
        } else {
            p2CurrentHp = Math.min(p2MaxHp, p2CurrentHp + heal);
            pbEnemyHpVersus.setProgress(p2CurrentHp);
        }
    }

    private void applyRecoilVersus(int recoil, boolean isP1) {
        if (isP1) {
            p1CurrentHp = Math.max(0, p1CurrentHp - recoil);
            pbPlayerHpVersus.setProgress(p1CurrentHp);
            txtPlayerHpNumbersVersus.setText(p1CurrentHp + "/" + p1MaxHp);
        } else {
            p2CurrentHp = Math.max(0, p2CurrentHp - recoil);
            pbEnemyHpVersus.setProgress(p2CurrentHp);
        }
    }

    private void actualizarVersusInsigniasStats() {
        actualizarVersusInsigniaIndividual(txtPlayerStatModsVersus, p1AtkMod, p1DefMod, p1SpAtkMod, p1SpDefMod, p1SpdMod);
        actualizarVersusInsigniaIndividual(txtEnemyStatModsVersus, p2AtkMod, p2DefMod, p2SpAtkMod, p2SpDefMod, p2SpdMod);
    }

    private void actualizarVersusInsigniaIndividual(TextView tv, int atk, int def, int spAtk, int spDef, int spd) {
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

    private double calculateVersusDamage(Pokemon attacker, Pokemon defender, int power, String moveType, String dmgClass, boolean isP1, StringBuilder logText) {
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
            logText.append("¡No afectó al rival!\n");
            return 0;
        }

        double attackStat, defenseStat;
        if ("special".equals(dmgClass)) {
            attackStat = getStat(attacker, "special-attack") * getModMultiplier(isP1 ? p1SpAtkMod : p2SpAtkMod);
            defenseStat = getStat(defender, "special-defense") * getModMultiplier(isP1 ? p2SpDefMod : p1SpDefMod);
        } else {
            attackStat = getStat(attacker, "attack") * getModMultiplier(isP1 ? p1AtkMod : p2AtkMod);
            defenseStat = getStat(defender, "defense") * getModMultiplier(isP1 ? p2DefMod : p1DefMod);
        }

        double damage = (((2.0 * 50 / 5.0 + 2.0) * power * (attackStat / defenseStat)) / 50.0 + 2.0) * stab * effectiveness;

        double randomRoll = 0.85 + (random.nextDouble() * 0.15);
        damage *= randomRoll;

        return damage;
    }

    private double getModMultiplier(int stage) {
        if (stage > 6) stage = 6;
        if (stage < -6) stage = -6;
        if (stage >= 0) return (2.0 + stage) / 2.0;
        else return 2.0 / (2.0 - stage);
    }

    private void executeFallbackVersus(String moveName, boolean isP1Attacker, Runnable onComplete) {
        txtBattleLogVersus.setText("Error de red.");
        onComplete.run();
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

    private void checkVersusTurnEnd() {
        if (p2CurrentHp <= 0) {
            faintedP2[activeP2] = true;
            updatePokeballIndicators();
            txtBattleLogVersus.setText("¡" + teamP2.get(activeP2).getName().toUpperCase(Locale.ROOT) + " de J2 se debilitó!");
            if (isAllFainted(faintedP2)) {
                endVersusBattle(1);
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(this::promptP2Switch, 1500);
            }
            return;
        }

        if (p1CurrentHp <= 0) {
            faintedP1[activeP1] = true;
            updatePokeballIndicators();
            txtBattleLogVersus.setText("¡" + teamP1.get(activeP1).getName().toUpperCase(Locale.ROOT) + " de J1 se debilitó!");
            if (isAllFainted(faintedP1)) {
                endVersusBattle(2);
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(this::promptP1Switch, 1500);
            }
            return;
        }

        // Si ambos siguen vivos, iniciar nueva ronda de selección con J1
        new Handler(Looper.getMainLooper()).postDelayed(this::startTurnP1, 1500);
    }

    private void promptP1Switch() {
        showCustomSwitchDialog(1);
    }

    private void promptP2Switch() {
        showCustomSwitchDialog(2);
    }

    private void showCustomSwitchDialog(int playerNum) {
        if (isFinishing() || isBattleEnded) return;

        boolean isP1 = (playerNum == 1);
        boolean[] faintedList = isP1 ? faintedP1 : faintedP2;
        List<Pokemon> teamList = isP1 ? teamP1 : teamP2;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pokemon_switch, null);

        TextView txtSwitchTitle = dialogView.findViewById(R.id.txtSwitchTitle);
        LinearLayout layoutSwitchOptions = dialogView.findViewById(R.id.layoutSwitchOptions);

        txtSwitchTitle.setText(isP1 ? "🔵 JUGADOR 1: Elige tu siguiente Pokémon" : "🔴 JUGADOR 2: Elige tu siguiente Pokémon");

        AlertDialog dialog = builder.setView(dialogView).setCancelable(false).create();

        for (int i = 0; i < 3; i++) {
            if (!faintedList[i]) {
                final int pokemonIndex = i;
                Pokemon p = teamList.get(i);

                View itemView = getLayoutInflater().inflate(R.layout.item_switch_pokemon, layoutSwitchOptions, false);

                ImageView imgSwitchSprite = itemView.findViewById(R.id.imgSwitchSprite);
                TextView txtSwitchName = itemView.findViewById(R.id.txtSwitchName);
                LinearLayout layoutSwitchTypes = itemView.findViewById(R.id.layoutSwitchTypes);

                txtSwitchName.setText(p.getName().toUpperCase(Locale.ROOT));

                if (p.getSprites() != null) {
                    Glide.with(this).load(p.getSprites().getFrontDefault()).into(imgSwitchSprite);
                }

                layoutSwitchTypes.removeAllViews();
                if (p.getTypes() != null) {
                    for (Pokemon.PokemonType pt : p.getTypes()) {
                        if (pt.getType() != null) {
                            String typeName = pt.getType().getName().toUpperCase(Locale.ROOT);
                            int typeColor = TypeColorUtils.getTypeColor(typeName);

                            TextView typeTv = new TextView(this);
                            typeTv.setText(typeName);
                            typeTv.setTextColor(Color.WHITE);
                            typeTv.setTextSize(10f);
                            typeTv.setTypeface(null, Typeface.BOLD);
                            typeTv.setPadding(12, 4, 12, 4);
                            typeTv.setBackgroundColor(typeColor);

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 0, 8, 0);
                            typeTv.setLayoutParams(lp);

                            layoutSwitchTypes.addView(typeTv);
                        }
                    }
                }

                itemView.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (isP1) {
                        loadP1Pokemon(pokemonIndex);
                        setVersusLogHtml("¡J1 envió a " + teamP1.get(pokemonIndex).getName().toUpperCase(Locale.ROOT) + "!", true);
                    } else {
                        loadP2Pokemon(pokemonIndex);
                        setVersusLogHtml("¡J2 envió a " + teamP2.get(pokemonIndex).getName().toUpperCase(Locale.ROOT) + "!", false);
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(this::startTurnP1, 1500);
                });

                layoutSwitchOptions.addView(itemView);
            }
        }

        dialog.show();
    }

    private boolean isAllFainted(boolean[] fainted) {
        return fainted[0] && fainted[1] && fainted[2];
    }

    private void updatePokeballIndicators() {
        if (faintedP1[0]) pbP1_1.setImageResource(android.R.drawable.presence_invisible);
        if (faintedP1[1]) pbP1_2.setImageResource(android.R.drawable.presence_invisible);
        if (faintedP1[2]) pbP1_3.setImageResource(android.R.drawable.presence_invisible);

        if (faintedP2[0]) pbP2_1.setImageResource(android.R.drawable.presence_invisible);
        if (faintedP2[1]) pbP2_2.setImageResource(android.R.drawable.presence_invisible);
        if (faintedP2[2]) pbP2_3.setImageResource(android.R.drawable.presence_invisible);
    }

    private void endVersusBattle(int winnerPlayer) {
        isBattleEnded = true;
        String winnerText = "¡JUGADOR " + winnerPlayer + " HA GANADO LA BATALLA!";
        txtBattleLogVersus.setText(winnerText);

        saveVersusResult(winnerPlayer);
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, 3500);
    }

    private void saveVersusResult(int winnerPlayer) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

        List<String> p1TeamNames = new ArrayList<>();
        for (Pokemon p : teamP1) p1TeamNames.add(p.getName());

        List<String> p2TeamNames = new ArrayList<>();
        for (Pokemon p : teamP2) p2TeamNames.add(p.getName());

        String winnerStr = winnerPlayer == 1 ? "Jugador 1" : "Jugador 2";
        boolean userWon = (winnerPlayer == 1);

        String recordId = db.collection("users").document(user.getUid()).collection("historial").document().getId();

        Map<String, Object> recordData = new HashMap<>();
        recordData.put("id", recordId);
        recordData.put("mode", "Battle Versus 3v3");
        recordData.put("date", dateStr);
        recordData.put("playerTeam", p1TeamNames);
        recordData.put("enemyTeam", p2TeamNames);
        recordData.put("winner", winnerStr);
        recordData.put("userWon", userWon);

        db.collection("users").document(user.getUid())
                .collection("historial").document(recordId)
                .set(recordData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "¡Versus guardado en historial!", Toast.LENGTH_SHORT).show());

        Map<String, Object> statsData = new HashMap<>();
        statsData.put(userWon ? "victorias" : "derrotas", FieldValue.increment(1));
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