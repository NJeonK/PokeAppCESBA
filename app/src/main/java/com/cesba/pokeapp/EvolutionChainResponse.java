package com.cesba.pokeapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EvolutionChainResponse {
    private Chain chain;

    public Chain getChain() { return chain; }

    public static class Chain {
        private Species species;

        @SerializedName("evolves_to")
        private List<Chain> evolvesTo;

        public Species getSpecies() { return species; }
        public List<Chain> getEvolvesTo() { return evolvesTo; }
    }

    public static class Species {
        private String name;
        private String url;

        public String getName() { return name; }
        public String getUrl() { return url; }
    }
}