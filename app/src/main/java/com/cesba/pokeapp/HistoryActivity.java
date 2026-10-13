package com.cesba.pokeapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ImageView btnBackHistory;
    private RecyclerView rvHistory;
    private TextView txtEmptyHistory;

    private HistoryAdapter adapter;
    private List<BattleRecord> recordList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBackHistory = findViewById(R.id.btnBackHistory);
        rvHistory = findViewById(R.id.rvHistory);
        txtEmptyHistory = findViewById(R.id.txtEmptyHistory);

        btnBackHistory.setOnClickListener(v -> finish());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        recordList = new ArrayList<>();
        adapter = new HistoryAdapter(recordList);
        rvHistory.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users").document(user.getUid())
                .collection("historial")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recordList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        BattleRecord rec = doc.toObject(BattleRecord.class);
                        if (rec != null) {
                            recordList.add(rec);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (recordList.isEmpty()) {
                        txtEmptyHistory.setVisibility(View.VISIBLE);
                        rvHistory.setVisibility(View.GONE);
                    } else {
                        txtEmptyHistory.setVisibility(View.GONE);
                        rvHistory.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar historial", Toast.LENGTH_SHORT).show();
                });
    }
}