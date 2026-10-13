package com.cesba.pokeapp;

import java.util.List;

public class PokemonListResponse {
    private List<PokemonItem> results;

    public List<PokemonItem> getResults() {
        return results;
    }

    public static class PokemonItem {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}