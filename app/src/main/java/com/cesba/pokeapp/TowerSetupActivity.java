package com.cesba.pokeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TowerSetupActivity extends AppCompatActivity {

    private ImageView btnBackTowerSetup;
    private TextView txtDraftCount;
    private RecyclerView rvDraft;
    private ProgressBar pbDraftLoading;
    private MaterialButton btnStartTower;

    private PokeApiService service;
    private List<Pokemon> draftOptions = new ArrayList<>();
    private List<Pokemon> selectedTeam = new ArrayList<>();
    private DraftAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tower_setup);

        service = ApiClient.getClient().create(PokeApiService.class);

        btnBackTowerSetup = findViewById(R.id.btnBackTowerSetup);
        txtDraftCount = findViewById(R.id.txtDraftCount);
        rvDraft = findViewById(R.id.rvDraft);
        pbDraftLoading = findViewById(R.id.pbDraftLoading);
        btnStartTower = findViewById(R.id.btnStartTower);

        btnBackTowerSetup.setOnClickListener(v -> finish());

        rvDraft.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new DraftAdapter();
        rvDraft.setAdapter(adapter);

        btnStartTower.setOnClickListener(v -> {
            if (selectedTeam.size() != 3) {
                Toast.makeText(this, "Debes seleccionar exactamente 3 Pokémon", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> teamNames = new ArrayList<>();
            for (Pokemon p : selectedTeam) teamNames.add(p.getName());

            Intent intent = new Intent(this, TowerActivity.class);
            intent.putStringArrayListExtra("PLAYER_TEAM", teamNames);
            startActivity(intent);
            finish();
        });

        generateDraftOptions();
    }

    private void generateDraftOptions() {
        Random random = new Random();
        List<Integer> randomIds = new ArrayList<>();
        while (randomIds.size() < 6) {
            int id = random.nextInt(1000) + 1;
            if (!randomIds.contains(id)) randomIds.add(id);
        }

        for (int i = 0; i < 6; i++) {
            draftOptions.add(null);
        }

        for (int i = 0; i < 6; i++) {
            final int index = i;
            service.getPokemon(String.valueOf(randomIds.get(i))).enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        draftOptions.set(index, response.body());
                        checkAllDraftLoaded();
                    } else handleLoadError();
                }
                @Override
                public void onFailure(Call<Pokemon> call, Throwable t) {
                    handleLoadError();
                }
            });
        }
    }

    private void handleLoadError() {
        Toast.makeText(this, "Error cargando reclutamiento. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void checkAllDraftLoaded() {
        for (Pokemon p : draftOptions) {
            if (p == null) return;
        }
        pbDraftLoading.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private class DraftAdapter extends RecyclerView.Adapter<DraftAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tower_draft, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Pokemon pokemon = draftOptions.get(position);
            if (pokemon == null) return;

            holder.txtDraftName.setText(pokemon.getName().substring(0, 1).toUpperCase(Locale.ROOT) + pokemon.getName().substring(1));
            if (pokemon.getSprites() != null) {
                Glide.with(holder.itemView.getContext())
                        .load(pokemon.getSprites().getFrontDefault())
                        .into(holder.imgDraftPokemon);
            }

            boolean isSelected = selectedTeam.contains(pokemon);
            holder.cbDraftSelect.setChecked(isSelected);
            holder.cardDraftItem.setStrokeWidth(isSelected ? 6 : 0);

            holder.itemView.setOnClickListener(v -> {
                if (isSelected) {
                    selectedTeam.remove(pokemon);
                } else {
                    if (selectedTeam.size() >= 3) {
                        Toast.makeText(TowerSetupActivity.this, "Ya seleccionaste 3 Pokémon", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedTeam.add(pokemon);
                }
                updateDraftUI();
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() {
            return draftOptions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardDraftItem;
            ImageView imgDraftPokemon;
            TextView txtDraftName;
            CheckBox cbDraftSelect;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardDraftItem = itemView.findViewById(R.id.cardDraftItem);
                imgDraftPokemon = itemView.findViewById(R.id.imgDraftPokemon);
                txtDraftName = itemView.findViewById(R.id.txtDraftName);
                cbDraftSelect = itemView.findViewById(R.id.cbDraftSelect);
            }
        }
    }

    private void updateDraftUI() {
        int count = selectedTeam.size();
        txtDraftCount.setText("Seleccionados: " + count + " / 3");
        btnStartTower.setText("ENTRAR A LA TORRE (" + count + "/3)");
        btnStartTower.setEnabled(count == 3);
    }
}