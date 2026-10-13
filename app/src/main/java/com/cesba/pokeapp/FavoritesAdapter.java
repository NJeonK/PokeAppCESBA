package com.cesba.pokeapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private List<FavoritePokemon> favoriteList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FavoritePokemon pokemon);
    }

    public FavoritesAdapter(List<FavoritePokemon> favoriteList, OnItemClickListener listener) {
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_pokemon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoritePokemon pokemon = favoriteList.get(position);
        holder.txtFavName.setText(pokemon.getName().substring(0, 1).toUpperCase(Locale.ROOT) + pokemon.getName().substring(1));
        holder.txtFavId.setText(String.format(Locale.ROOT, "#%03d", pokemon.getId()));
        
        if (pokemon.getImage() != null) {
            Glide.with(holder.itemView.getContext())
                 .load(pokemon.getImage())
                 .into(holder.imgFavPokemon);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(pokemon);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFavPokemon;
        TextView txtFavId;
        TextView txtFavName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFavPokemon = itemView.findViewById(R.id.imgFavPokemon);
            txtFavId = itemView.findViewById(R.id.txtFavId);
            txtFavName = itemView.findViewById(R.id.txtFavName);
        }
    }
}