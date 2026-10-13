package com.cesba.pokeapp;

import java.util.HashMap;
import java.util.Map;

public class TypeUtils {
    private static final Map<String, Map<String, Double>> typeChart = new HashMap<>();

    static {
        // Inicializar tabla de efectividades básicas.
        // Si no está, el multiplicador es 1.0
        addEffectiveness("normal", "rock", 0.5);
        addEffectiveness("normal", "ghost", 0.0);
        addEffectiveness("normal", "steel", 0.5);

        addEffectiveness("fire", "fire", 0.5);
        addEffectiveness("fire", "water", 0.5);
        addEffectiveness("fire", "grass", 2.0);
        addEffectiveness("fire", "ice", 2.0);
        addEffectiveness("fire", "bug", 2.0);
        addEffectiveness("fire", "rock", 0.5);
        addEffectiveness("fire", "dragon", 0.5);
        addEffectiveness("fire", "steel", 2.0);

        addEffectiveness("water", "fire", 2.0);
        addEffectiveness("water", "water", 0.5);
        addEffectiveness("water", "grass", 0.5);
        addEffectiveness("water", "ground", 2.0);
        addEffectiveness("water", "rock", 2.0);
        addEffectiveness("water", "dragon", 0.5);

        addEffectiveness("electric", "water", 2.0);
        addEffectiveness("electric", "electric", 0.5);
        addEffectiveness("electric", "grass", 0.5);
        addEffectiveness("electric", "ground", 0.0);
        addEffectiveness("electric", "flying", 2.0);
        addEffectiveness("electric", "dragon", 0.5);

        addEffectiveness("grass", "fire", 0.5);
        addEffectiveness("grass", "water", 2.0);
        addEffectiveness("grass", "grass", 0.5);
        addEffectiveness("grass", "poison", 0.5);
        addEffectiveness("grass", "ground", 2.0);
        addEffectiveness("grass", "flying", 0.5);
        addEffectiveness("grass", "bug", 0.5);
        addEffectiveness("grass", "rock", 2.0);
        addEffectiveness("grass", "dragon", 0.5);
        addEffectiveness("grass", "steel", 0.5);

        addEffectiveness("ice", "fire", 0.5);
        addEffectiveness("ice", "water", 0.5);
        addEffectiveness("ice", "grass", 2.0);
        addEffectiveness("ice", "ice", 0.5);
        addEffectiveness("ice", "ground", 2.0);
        addEffectiveness("ice", "flying", 2.0);
        addEffectiveness("ice", "dragon", 2.0);
        addEffectiveness("ice", "steel", 0.5);

        addEffectiveness("fighting", "normal", 2.0);
        addEffectiveness("fighting", "ice", 2.0);
        addEffectiveness("fighting", "poison", 0.5);
        addEffectiveness("fighting", "flying", 0.5);
        addEffectiveness("fighting", "psychic", 0.5);
        addEffectiveness("fighting", "bug", 0.5);
        addEffectiveness("fighting", "rock", 2.0);
        addEffectiveness("fighting", "ghost", 0.0);
        addEffectiveness("fighting", "dark", 2.0);
        addEffectiveness("fighting", "steel", 2.0);
        addEffectiveness("fighting", "fairy", 0.5);

        addEffectiveness("poison", "grass", 2.0);
        addEffectiveness("poison", "poison", 0.5);
        addEffectiveness("poison", "ground", 0.5);
        addEffectiveness("poison", "rock", 0.5);
        addEffectiveness("poison", "ghost", 0.5);
        addEffectiveness("poison", "steel", 0.0);
        addEffectiveness("poison", "fairy", 2.0);

        addEffectiveness("ground", "fire", 2.0);
        addEffectiveness("ground", "electric", 2.0);
        addEffectiveness("ground", "grass", 0.5);
        addEffectiveness("ground", "poison", 2.0);
        addEffectiveness("ground", "flying", 0.0);
        addEffectiveness("ground", "bug", 0.5);
        addEffectiveness("ground", "rock", 2.0);
        addEffectiveness("ground", "steel", 2.0);

        addEffectiveness("flying", "electric", 0.5);
        addEffectiveness("flying", "grass", 2.0);
        addEffectiveness("flying", "fighting", 2.0);
        addEffectiveness("flying", "bug", 2.0);
        addEffectiveness("flying", "rock", 0.5);
        addEffectiveness("flying", "steel", 0.5);

        addEffectiveness("psychic", "fighting", 2.0);
        addEffectiveness("psychic", "poison", 2.0);
        addEffectiveness("psychic", "psychic", 0.5);
        addEffectiveness("psychic", "dark", 0.0);
        addEffectiveness("psychic", "steel", 0.5);

        addEffectiveness("bug", "fire", 0.5);
        addEffectiveness("bug", "grass", 2.0);
        addEffectiveness("bug", "fighting", 0.5);
        addEffectiveness("bug", "poison", 0.5);
        addEffectiveness("bug", "flying", 0.5);
        addEffectiveness("bug", "psychic", 2.0);
        addEffectiveness("bug", "ghost", 0.5);
        addEffectiveness("bug", "dark", 2.0);
        addEffectiveness("bug", "steel", 0.5);
        addEffectiveness("bug", "fairy", 0.5);

        addEffectiveness("rock", "fire", 2.0);
        addEffectiveness("rock", "ice", 2.0);
        addEffectiveness("rock", "fighting", 0.5);
        addEffectiveness("rock", "ground", 0.5);
        addEffectiveness("rock", "flying", 2.0);
        addEffectiveness("rock", "bug", 2.0);
        addEffectiveness("rock", "steel", 0.5);

        addEffectiveness("ghost", "normal", 0.0);
        addEffectiveness("ghost", "psychic", 2.0);
        addEffectiveness("ghost", "ghost", 2.0);
        addEffectiveness("ghost", "dark", 0.5);

        addEffectiveness("dragon", "dragon", 2.0);
        addEffectiveness("dragon", "steel", 0.5);
        addEffectiveness("dragon", "fairy", 0.0);

        addEffectiveness("dark", "fighting", 0.5);
        addEffectiveness("dark", "psychic", 2.0);
        addEffectiveness("dark", "ghost", 2.0);
        addEffectiveness("dark", "dark", 0.5);
        addEffectiveness("dark", "fairy", 0.5);

        addEffectiveness("steel", "fire", 0.5);
        addEffectiveness("steel", "water", 0.5);
        addEffectiveness("steel", "electric", 0.5);
        addEffectiveness("steel", "ice", 2.0);
        addEffectiveness("steel", "rock", 2.0);
        addEffectiveness("steel", "steel", 0.5);
        addEffectiveness("steel", "fairy", 2.0);

        addEffectiveness("fairy", "fire", 0.5);
        addEffectiveness("fairy", "fighting", 2.0);
        addEffectiveness("fairy", "poison", 0.5);
        addEffectiveness("fairy", "dragon", 2.0);
        addEffectiveness("fairy", "dark", 2.0);
        addEffectiveness("fairy", "steel", 0.5);
    }

    private static void addEffectiveness(String atk, String def, double mult) {
        if (!typeChart.containsKey(atk)) {
            typeChart.put(atk, new HashMap<>());
        }
        typeChart.get(atk).put(def, mult);
    }

    public static double getMultiplier(String attackType, String defenseType) {
        if (attackType == null || defenseType == null) return 1.0;
        String atk = attackType.toLowerCase().trim();
        String def = defenseType.toLowerCase().trim();
        if (typeChart.containsKey(atk)) {
            Map<String, Double> defenseMap = typeChart.get(atk);
            if (defenseMap.containsKey(def)) {
                return defenseMap.get(def);
            }
        }
        return 1.0;
    }

    public static double getTypeMultiplier(String attackType, String defenderType1, String defenderType2) {
        double m1 = getMultiplier(attackType, defenderType1);
        double m2 = (defenderType2 != null && !defenderType2.isEmpty()) ? getMultiplier(attackType, defenderType2) : 1.0;
        return m1 * m2;
    }
}