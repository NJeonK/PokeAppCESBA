package com.cesba.pokeapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<BattleRecord> recordList;

    public HistoryAdapter(List<BattleRecord> recordList) {
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BattleRecord record = recordList.get(position);

        holder.txtHistoryMode.setText(record.getMode() != null ? record.getMode() : "Batalla");
        holder.txtHistoryDate.setText(record.getDate() != null ? record.getDate() : "");

        if (record.isUserWon()) {
            holder.txtHistoryResult.setText("VICTORIA");
            holder.txtHistoryResult.setBackgroundColor(holder.itemView.getContext().getColor(R.color.pokedex_led_green));
        } else {
            holder.txtHistoryResult.setText("DERROTA");
            holder.txtHistoryResult.setBackgroundColor(holder.itemView.getContext().getColor(R.color.pokedex_led_red));
        }

        String pTeam = record.getPlayerTeam() != null ? String.join(", ", record.getPlayerTeam()) : "?";
        String eTeam = record.getEnemyTeam() != null ? String.join(", ", record.getEnemyTeam()) : "?";
        holder.txtHistoryTeams.setText(String.format(Locale.getDefault(), "Aspirantes: %s  VS  %s", pTeam.toUpperCase(Locale.ROOT), eTeam.toUpperCase(Locale.ROOT)));

        String winner = record.getWinner() != null ? record.getWinner().toUpperCase(Locale.ROOT) : "?";
        holder.txtHistoryWinner.setText(String.format(Locale.getDefault(), "Ganador: %s", winner));

        holder.itemView.setOnClickListener(v -> showRecordLogsDialog(v.getContext(), record));
    }

    private void showRecordLogsDialog(Context context, BattleRecord record) {
        if (record == null) return;

        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_history_detail, null);

        TextView txtDialogTitle = dialogView.findViewById(R.id.txtDialogTitle);
        TextView txtDialogDate = dialogView.findViewById(R.id.txtDialogDate);
        TextView txtDialogWinner = dialogView.findViewById(R.id.txtDialogWinner);
        TextView txtDialogTeams = dialogView.findViewById(R.id.txtDialogTeams);
        TextView txtHistoryLogs = dialogView.findViewById(R.id.txtHistoryLogs);

        ImageView imgHistoryP1Sprite = dialogView.findViewById(R.id.imgHistoryP1Sprite);
        ImageView imgHistoryP2Sprite = dialogView.findViewById(R.id.imgHistoryP2Sprite);

        txtDialogTitle.setText(record.getMode() != null ? record.getMode().toUpperCase(Locale.ROOT) : "BITÁCORA");
        txtDialogDate.setText(record.getDate() != null ? record.getDate() : "");

        String winnerStr = record.getWinner() != null ? record.getWinner().toUpperCase(Locale.ROOT) : "DESCONOCIDO";
        txtDialogWinner.setText("Ganador: " + winnerStr);

        String pTeam = record.getPlayerTeam() != null ? String.join(", ", record.getPlayerTeam()) : "?";
        String eTeam = record.getEnemyTeam() != null ? String.join(", ", record.getEnemyTeam()) : "?";
        txtDialogTeams.setText(pTeam.toUpperCase(Locale.ROOT) + "  VS  " + eTeam.toUpperCase(Locale.ROOT));

        PokeApiService apiService = ApiClient.getClient().create(PokeApiService.class);

        if (record.getPlayerTeam() != null && !record.getPlayerTeam().isEmpty()) {
            String p1Name = record.getPlayerTeam().get(0);
            apiService.getPokemon(p1Name).enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getSprites() != null) {
                        Glide.with(context)
                                .load(response.body().getSprites().getFrontDefault())
                                .into(imgHistoryP1Sprite);
                    }
                }
                @Override public void onFailure(Call<Pokemon> call, Throwable t) { }
            });
        }

        if (record.getEnemyTeam() != null && !record.getEnemyTeam().isEmpty()) {
            String p2Name = record.getEnemyTeam().get(0);
            apiService.getPokemon(p2Name).enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getSprites() != null) {
                        Glide.with(context)
                                .load(response.body().getSprites().getFrontDefault())
                                .into(imgHistoryP2Sprite);
                    }
                }
                @Override public void onFailure(Call<Pokemon> call, Throwable t) { }
            });
        }

        StringBuilder logsSb = new StringBuilder();
        if (record.getBattleLogs() != null && !record.getBattleLogs().isEmpty()) {
            for (String line : record.getBattleLogs()) {
                logsSb.append(line).append("\n\n");
            }
        } else {
            logsSb.append("Resumen del combate guardado:\n\n");
            logsSb.append("Enfrentamiento entre ").append(pTeam.toUpperCase(Locale.ROOT)).append(" y ").append(eTeam.toUpperCase(Locale.ROOT)).append(".\n\n");
            logsSb.append("Ganador del encuentro: ").append(winnerStr).append(".");
        }

        txtHistoryLogs.setText(logsSb.toString().trim());

        ScrollView scrollLogs = dialogView.findViewById(R.id.scrollHistoryLogs);
        if (scrollLogs != null) {
            scrollLogs.setOnTouchListener((v, event) -> {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtHistoryMode, txtHistoryResult, txtHistoryDate, txtHistoryTeams, txtHistoryWinner;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtHistoryMode = itemView.findViewById(R.id.txtHistoryMode);
            txtHistoryResult = itemView.findViewById(R.id.txtHistoryResult);
            txtHistoryDate = itemView.findViewById(R.id.txtHistoryDate);
            txtHistoryTeams = itemView.findViewById(R.id.txtHistoryTeams);
            txtHistoryWinner = itemView.findViewById(R.id.txtHistoryWinner);
        }
    }
}