package com.cesba.pokeapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonSpecies {
    @SerializedName("flavor_text_entries")
    private List<FlavorTextEntry> flavorTextEntries;

    @SerializedName("evolution_chain")
    private EvolutionChainUrl evolutionChain;

    @SerializedName("varieties")
    private List<Variety> varieties;

    public List<FlavorTextEntry> getFlavorTextEntries() {
        return flavorTextEntries;
    }

    public EvolutionChainUrl getEvolutionChain() {
        return evolutionChain;
    }

    public List<Variety> getVarieties() {
        return varieties;
    }

    public static class Variety {
        @SerializedName("is_default")
        private boolean isDefault;

        private PokemonRef pokemon;

        public boolean isDefault() { return isDefault; }
        public PokemonRef getPokemon() { return pokemon; }
    }

    public static class PokemonRef {
        private String name;
        private String url;

        public String getName() { return name; }
        public String getUrl() { return url; }
    }

    public static class EvolutionChainUrl {
        private String url;
        public String getUrl() { return url; }
    }

    public static class FlavorTextEntry {
        @SerializedName("flavor_text")
        private String flavorText;
        private Language language;

        public String getFlavorText() {
            return flavorText;
        }

        public Language getLanguage() {
            return language;
        }
    }

    public static class Language {
        private String name;

        public String getName() {
            return name;
        }
    }
}