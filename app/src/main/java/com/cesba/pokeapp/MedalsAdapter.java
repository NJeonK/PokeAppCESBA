package com.cesba.pokeapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedalsAdapter extends RecyclerView.Adapter<MedalsAdapter.ViewHolder> {

    private List<Medal> medalList;

    public MedalsAdapter(List<Medal> medalList) {
        this.medalList = medalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medal medal = medalList.get(position);

        holder.txtMedalName.setText("Medalla Torre " + medal.getId());

        if (medal.isUnlocked()) {
            holder.imgMedalIcon.setAlpha(1.0f);
            holder.txtMedalStatus.setText("DESBLOQUEADA");
            holder.txtMedalStatus.setTextColor(holder.itemView.getContext().getColor(R.color.pokedex_led_green));
        } else {
            holder.imgMedalIcon.setAlpha(0.25f);
            holder.txtMedalStatus.setText("BLOQUEADA");
            holder.txtMedalStatus.setTextColor(holder.itemView.getContext().getColor(R.color.poke_text_sub));
        }
    }

    @Override
    public int getItemCount() {
        return medalList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMedalIcon;
        TextView txtMedalName, txtMedalStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMedalIcon = itemView.findViewById(R.id.imgMedalIcon);
            txtMedalName = itemView.findViewById(R.id.txtMedalName);
            txtMedalStatus = itemView.findViewById(R.id.txtMedalStatus);
        }
    }
}