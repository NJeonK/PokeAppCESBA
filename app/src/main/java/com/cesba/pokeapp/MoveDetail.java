package com.cesba.pokeapp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MoveDetail {
    private String name;
    private Integer power;
    private Integer accuracy;
    private Integer priority;
    private NamedAPIResource type;
    private NamedAPIResource target;

    @SerializedName("stat_changes")
    private List<StatChange> statChanges;

    @SerializedName("damage_class")
    private NamedAPIResource damageClass;
    
    private Meta meta;

    public String getName() { return name; }
    public Integer getPower() { return power; }
    public Integer getAccuracy() { return accuracy; }
    public Integer getPriority() { return priority != null ? priority : 0; }
    public NamedAPIResource getType() { return type; }
    public NamedAPIResource getTarget() { return target; }
    public List<StatChange> getStatChanges() { return statChanges; }
    public NamedAPIResource getDamageClass() { return damageClass; }
    public Meta getMeta() { return meta; }

    public static class Meta {
        private NamedAPIResource ailment;

        @SerializedName("min_hits")
        private Integer minHits;

        @SerializedName("max_hits")
        private Integer maxHits;

        @SerializedName("flinch_chance")
        private Integer flinchChance;

        private Integer drain;
        private NamedAPIResource category;

        public NamedAPIResource getAilment() { return ailment; }
        public Integer getMinHits() { return minHits != null ? minHits : 1; }
        public Integer getMaxHits() { return maxHits != null ? maxHits : 1; }
        public Integer getFlinchChance() { return flinchChance != null ? flinchChance : 0; }
        public Integer getDrain() { return drain != null ? drain : 0; }
        public NamedAPIResource getCategory() { return category; }
    }

    public static class NamedAPIResource {
        private String name;
        public String getName() { return name; }
    }

    public static class StatChange {
        private int change;
        private NamedAPIResource stat;
        public int getChange() { return change; }
        public NamedAPIResource getStat() { return stat; }
    }
}