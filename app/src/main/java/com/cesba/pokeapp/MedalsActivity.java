package com.cesba.pokeapp;

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

public class MedalsActivity extends AppCompatActivity {

    private ImageView btnBackMedals;
    private RecyclerView rvMedals;
    private TextView txtEmptyMedals;

    private MedalsAdapter adapter;
    private List<Medal> medalList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medals);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBackMedals = findViewById(R.id.btnBackMedals);
        rvMedals = findViewById(R.id.rvMedals);
        txtEmptyMedals = findViewById(R.id.txtEmptyMedals);

        btnBackMedals.setOnClickListener(v -> finish());

        rvMedals.setLayoutManager(new GridLayoutManager(this, 2));
        medalList = new ArrayList<>();
        
        // Inicializar las 10 medallas bloqueadas por defecto
        for (int i = 1; i <= 10; i++) {
            medalList.add(new Medal(i, "Medalla " + i, false, ""));
        }

        adapter = new MedalsAdapter(medalList);
        rvMedals.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedals();
    }

    private void loadMedals() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users").document(user.getUid())
                .collection("medallas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Medal unlockedMedal = doc.toObject(Medal.class);
                        if (unlockedMedal != null && unlockedMedal.getId() >= 1 && unlockedMedal.getId() <= 10) {
                            int index = unlockedMedal.getId() - 1;
                            medalList.set(index, unlockedMedal);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    boolean anyUnlocked = false;
                    for (Medal m : medalList) {
                        if (m.isUnlocked()) {
                            anyUnlocked = true;
                            break;
                        }
                    }

                    txtEmptyMedals.setVisibility(anyUnlocked ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar medallas", Toast.LENGTH_SHORT).show();
                });
    }
}