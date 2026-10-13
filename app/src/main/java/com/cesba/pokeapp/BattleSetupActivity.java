package com.cesba.pokeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Random;

public class BattleSetupActivity extends AppCompatActivity {

    private ImageView btnBackSetup;
    private EditText etPlayer1, etPlayer2;
    private MaterialButton btnRandomBattle, btnStartBattle;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_setup);

        btnBackSetup = findViewById(R.id.btnBackSetup);
        etPlayer1 = findViewById(R.id.etPlayer1);
        etPlayer2 = findViewById(R.id.etPlayer2);
        btnRandomBattle = findViewById(R.id.btnRandomBattle);
        btnStartBattle = findViewById(R.id.btnStartBattle);

        btnBackSetup.setOnClickListener(v -> finish());

        btnRandomBattle.setOnClickListener(v -> {
            int p1Id = random.nextInt(1025) + 1;
            int p2Id = random.nextInt(1025) + 1;
            while (p2Id == p1Id) {
                p2Id = random.nextInt(1025) + 1;
            }
            etPlayer1.setText(String.valueOf(p1Id));
            etPlayer2.setText(String.valueOf(p2Id));
            Toast.makeText(this, "¡Pokémon seleccionados al azar! 🎲", Toast.LENGTH_SHORT).show();
        });

        btnStartBattle.setOnClickListener(v -> {
            String p1 = etPlayer1.getText().toString().trim().toLowerCase();
            String p2 = etPlayer2.getText().toString().trim().toLowerCase();

            if (p1.isEmpty() || p2.isEmpty()) {
                Toast.makeText(this, "Debe ingresar ambos Pokémon", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, BattleActivity.class);
            intent.putExtra("PLAYER_1", p1);
            intent.putExtra("PLAYER_2", p2);
            startActivity(intent);
        });
    }
}