package com.cesba.pokeapp;

import com.google.gson.annotations.SerializedName;

public class Sprites {

    @SerializedName("front_default")
    private String frontDefault;

    @SerializedName("back_default")
    private String backDefault;

    @SerializedName("front_shiny")
    private String frontShiny;

    @SerializedName("back_shiny")
    private String backShiny;

    @SerializedName("other")
    private Other other;

    public Other getOther() { return other; }

    public String getShowdownFront(boolean shiny) {
        if (other != null && other.getShowdown() != null) {
            String url = shiny ? other.getShowdown().getFrontShiny() : other.getShowdown().getFrontDefault();
            if (url != null && !url.isEmpty()) return url;
        }
        return shiny ? getFrontShiny() : getFrontDefault();
    }

    public String getShowdownBack(boolean shiny) {
        if (other != null && other.getShowdown() != null) {
            String url = shiny ? other.getShowdown().getBackShiny() : other.getShowdown().getBackDefault();
            if (url != null && !url.isEmpty()) return url;
        }
        return shiny ? getBackShiny() : getBackDefault();
    }

    public String getFrontDefault() {
        return frontDefault;
    }

    public String getBackDefault() {
        return backDefault;
    }

    public String getFrontShiny() {
        return frontShiny;
    }

    public String getBackShiny() {
        return backShiny;
    }

    public static class Other {
        @SerializedName("showdown")
        private Showdown showdown;

        @SerializedName("official-artwork")
        private OfficialArtwork officialArtwork;

        @SerializedName("home")
        private Home home;

        public Showdown getShowdown() { return showdown; }
        public OfficialArtwork getOfficialArtwork() { return officialArtwork; }
        public Home getHome() { return home; }
    }

    public static class Showdown {
        @SerializedName("front_default")
        private String frontDefault;

        @SerializedName("back_default")
        private String backDefault;

        @SerializedName("front_shiny")
        private String frontShiny;

        @SerializedName("back_shiny")
        private String backShiny;

        public String getFrontDefault() { return frontDefault; }
        public String getBackDefault() { return backDefault; }
        public String getFrontShiny() { return frontShiny; }
        public String getBackShiny() { return backShiny; }
    }

    public static class OfficialArtwork {
        @SerializedName("front_default")
        private String frontDefault;

        public String getFrontDefault() { return frontDefault; }
    }

    public static class Home {
        @SerializedName("front_default")
        private String frontDefault;

        public String getFrontDefault() { return frontDefault; }
    }
}