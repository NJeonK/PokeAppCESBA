package com.cesba.pokeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class VersusSetupActivity extends AppCompatActivity {

    private ImageView btnBackVersusSetup;
    private EditText etP1_1, etP1_2, etP1_3;
    private EditText etP2_1, etP2_2, etP2_3;
    private MaterialButton btnRandomVersus, btnStartVersus;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versus_setup);

        btnBackVersusSetup = findViewById(R.id.btnBackVersusSetup);
        etP1_1 = findViewById(R.id.etP1_1);
        etP1_2 = findViewById(R.id.etP1_2);
        etP1_3 = findViewById(R.id.etP1_3);

        etP2_1 = findViewById(R.id.etP2_1);
        etP2_2 = findViewById(R.id.etP2_2);
        etP2_3 = findViewById(R.id.etP2_3);

        btnRandomVersus = findViewById(R.id.btnRandomVersus);
        btnStartVersus = findViewById(R.id.btnStartVersus);

        btnBackVersusSetup.setOnClickListener(v -> finish());

        btnRandomVersus.setOnClickListener(v -> {
            Set<Integer> chosenIds = new HashSet<>();
            while (chosenIds.size() < 6) {
                chosenIds.add(random.nextInt(1025) + 1);
            }
            ArrayList<Integer> list = new ArrayList<>(chosenIds);
            etP1_1.setText(String.valueOf(list.get(0)));
            etP1_2.setText(String.valueOf(list.get(1)));
            etP1_3.setText(String.valueOf(list.get(2)));

            etP2_1.setText(String.valueOf(list.get(3)));
            etP2_2.setText(String.valueOf(list.get(4)));
            etP2_3.setText(String.valueOf(list.get(5)));

            Toast.makeText(this, "¡Equipos 3v3 generados al azar! 🎲", Toast.LENGTH_SHORT).show();
        });

        btnStartVersus.setOnClickListener(v -> {
            String p1_1 = etP1_1.getText().toString().trim().toLowerCase(Locale.ROOT);
            String p1_2 = etP1_2.getText().toString().trim().toLowerCase(Locale.ROOT);
            String p1_3 = etP1_3.getText().toString().trim().toLowerCase(Locale.ROOT);

            String p2_1 = etP2_1.getText().toString().trim().toLowerCase(Locale.ROOT);
            String p2_2 = etP2_2.getText().toString().trim().toLowerCase(Locale.ROOT);
            String p2_3 = etP2_3.getText().toString().trim().toLowerCase(Locale.ROOT);

            if (p1_1.isEmpty() || p1_2.isEmpty() || p1_3.isEmpty() ||
                p2_1.isEmpty() || p2_2.isEmpty() || p2_3.isEmpty()) {
                Toast.makeText(this, "Completa los 3 Pokémon para ambos jugadores", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> teamP1 = new ArrayList<>();
            teamP1.add(p1_1); teamP1.add(p1_2); teamP1.add(p1_3);

            ArrayList<String> teamP2 = new ArrayList<>();
            teamP2.add(p2_1); teamP2.add(p2_2); teamP2.add(p2_3);

            Intent intent = new Intent(this, VersusActivity.class);
            intent.putStringArrayListExtra("TEAM_P1", teamP1);
            intent.putStringArrayListExtra("TEAM_P2", teamP2);
            startActivity(intent);
        });
    }
}