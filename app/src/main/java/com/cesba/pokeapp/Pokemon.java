package com.cesba.pokeapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Pokemon {

    private int id;
    private String name;
    private int height;
    private int weight;
    private Sprites sprites;
    private List<PokemonStat> stats;
    private List<PokemonType> types;
    private List<PokemonAbility> abilities;
    private List<PokemonMove> moves;
    private Cries cries;

    public int getId() { return id; }
    public String getName() { return name; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public Sprites getSprites() { return sprites; }
    public List<PokemonStat> getStats() { return stats; }
    public List<PokemonType> getTypes() { return types; }
    public List<PokemonAbility> getAbilities() { return abilities; }
    public List<PokemonMove> getMoves() { return moves; }
    public Cries getCries() { return cries; }

    public static class PokemonType {
        private Type type;
        public Type getType() { return type; }
    }

    public static class Type {
        private String name;
        public String getName() { return name; }
    }

    public static class PokemonMove {
        private Move move;
        @SerializedName("version_group_details")
        private List<VersionGroupDetail> versionGroupDetails;
        public Move getMove() { return move; }
        public List<VersionGroupDetail> getVersionGroupDetails() { return versionGroupDetails; }
    }

    public static class VersionGroupDetail {
        @SerializedName("level_learned_at")
        private int levelLearnedAt;
        @SerializedName("move_learn_method")
        private MoveLearnMethod moveLearnMethod;
        public int getLevelLearnedAt() { return levelLearnedAt; }
        public MoveLearnMethod getMoveLearnMethod() { return moveLearnMethod; }
    }

    public static class MoveLearnMethod {
        private String name;
        public String getName() { return name; }
    }

    public static class Move {
        private String name;
        public String getName() { return name; }
    }

    public static class PokemonAbility {
        private Ability ability;
        @SerializedName("is_hidden")
        private boolean isHidden;
        public Ability getAbility() { return ability; }
        public boolean isHidden() { return isHidden; }
    }

    public static class Ability {
        private String name;
        public String getName() { return name; }
    }

    public static class Cries {
        private String latest;
        private String legacy;
        public String getLatest() { return latest; }
        public String getLegacy() { return legacy; }
    }
}