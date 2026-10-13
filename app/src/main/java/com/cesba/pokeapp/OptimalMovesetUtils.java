package com.cesba.pokeapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OptimalMovesetUtils {

    // Smogon Tier S/A Core STAB & High Power Moves
    private static final Set<String> SMOGON_STAB_MOVES = new HashSet<>();
    // Smogon Setup, Recovery & Utility Moves
    private static final Set<String> SMOGON_SETUP_MOVES = new HashSet<>();
    // Smogon Priority, Momentum & Hazards
    private static final Set<String> SMOGON_UTILITY_MOVES = new HashSet<>();

    static {
        // Core Attacks (Smogon OU/VGC)
        String[] stabMoves = {
            "earthquake", "flamethrower", "fire-blast", "surf", "hydro-pump", "thunderbolt",
            "thunder", "ice-beam", "blizzard", "close-combat", "superpower", "sludge-bomb",
            "psychic", "shadow-ball", "outrage", "dragon-claw", "dragon-pulse", "moonblast",
            "stone-edge", "rock-slide", "iron-head", "flash-cannon", "bug-buzz", "megahorn",
            "giga-drain", "energy-ball", "leaf-storm", "air-slash", "brave-bird", "dark-pulse",
            "foul-play", "play-rough", "water-shuriken", "aura-sphere", "shadow-claw",
            "knock-off", "drain-punch", "draco-meteor", "fire-punch", "ice-punch", "thunder-punch",
            "flare-blitz", "liquidation", "head-smash", "iron-tail", "hurricane", "high-jump-kick"
        };
        Collections.addAll(SMOGON_STAB_MOVES, stabMoves);

        // Setup & Recovery (Smogon OU/VGC)
        String[] setupMoves = {
            "swords-dance", "dragon-dance", "calm-mind", "nasty-plot", "shell-smash",
            "quiver-dance", "bulk-up", "agility", "rock-polish", "shift-gear",
            "recover", "roost", "synthesis", "slack-off", "soft-boiled", "wish", "morning-sun"
        };
        Collections.addAll(SMOGON_SETUP_MOVES, setupMoves);

        // Priority, Hazards & Disrupt (Smogon OU/VGC)
        String[] utilityMoves = {
            "extreme-speed", "sucker-punch", "mach-punch", "bullet-punch", "aqua-jet",
            "ice-shard", "quick-attack", "fake-out", "u-turn", "volt-switch", "flip-turn",
            "stealth-rock", "spikes", "toxic-spikes", "taunt", "will-o-wisp", "toxic",
            "thunder-wave", "yawn", "haze", "defog", "rapid-spin", "protect"
        };
        Collections.addAll(SMOGON_UTILITY_MOVES, utilityMoves);
    }

    public static List<Pokemon.PokemonMove> getOptimalMoveset(Pokemon pokemon) {
        if (pokemon == null || pokemon.getMoves() == null || pokemon.getMoves().isEmpty()) {
            return new ArrayList<>();
        }

        List<Pokemon.PokemonMove> allMoves = pokemon.getMoves();
        if (allMoves.size() <= 4) {
            return new ArrayList<>(allMoves);
        }

        // Extract Pokemon types
        List<String> pTypes = new ArrayList<>();
        if (pokemon.getTypes() != null) {
            for (Pokemon.PokemonType pt : pokemon.getTypes()) {
                if (pt.getType() != null && pt.getType().getName() != null) {
                    pTypes.add(pt.getType().getName().toLowerCase(Locale.ROOT));
                }
            }
        }

        // Score each move available for this Pokemon using Smogon competitive criteria
        List<ScoredMove> scoredList = new ArrayList<>();

        for (Pokemon.PokemonMove pm : allMoves) {
            if (pm.getMove() == null || pm.getMove().getName() == null) continue;

            String moveName = pm.getMove().getName().toLowerCase(Locale.ROOT);
            int score = 10; // Base score

            // 1. Smogon Core STAB Attack
            if (SMOGON_STAB_MOVES.contains(moveName)) {
                score += 80;
            }

            // 2. Smogon Setup / Recovery Move
            if (SMOGON_SETUP_MOVES.contains(moveName)) {
                score += 85;
            }

            // 3. Smogon Utility / Priority / Hazards
            if (SMOGON_UTILITY_MOVES.contains(moveName)) {
                score += 70;
            }

            // 4. STAB Bonus (Matches Pokemon's Type)
            for (String t : pTypes) {
                if (moveName.contains(t) || isMoveMatchingType(moveName, t)) {
                    score += 45;
                }
            }

            scoredList.add(new ScoredMove(pm, score));
        }

        // Sort moves by score in descending order
        Collections.sort(scoredList, (a, b) -> Integer.compare(b.score, a.score));

        // Pick top 4 optimal moves ensuring non-duplicates
        List<Pokemon.PokemonMove> result = new ArrayList<>();
        Set<String> chosenNames = new HashSet<>();

        for (ScoredMove sm : scoredList) {
            if (result.size() >= 4) break;
            String name = sm.move.getMove().getName();
            if (!chosenNames.contains(name)) {
                chosenNames.add(name);
                result.add(sm.move);
            }
        }

        return result;
    }

    private static boolean isMoveMatchingType(String moveName, String pokemonType) {
        switch (pokemonType) {
            case "fire": return moveName.contains("fire") || moveName.contains("flame") || moveName.contains("flare") || moveName.contains("heat");
            case "water": return moveName.contains("water") || moveName.contains("surf") || moveName.contains("hydro") || moveName.contains("aqua");
            case "grass": return moveName.contains("grass") || moveName.contains("leaf") || moveName.contains("drain") || moveName.contains("seed");
            case "electric": return moveName.contains("thunder") || moveName.contains("bolt") || moveName.contains("volt") || moveName.contains("spark");
            case "ice": return moveName.contains("ice") || moveName.contains("blizzard") || moveName.contains("freeze") || moveName.contains("frost");
            case "fighting": case "lucha": return moveName.contains("combat") || moveName.contains("punch") || moveName.contains("kick") || moveName.contains("aura");
            case "dragon": return moveName.contains("dragon") || moveName.contains("draco") || moveName.contains("outrage");
            case "ghost": return moveName.contains("shadow") || moveName.contains("ghost") || moveName.contains("spook");
            case "psychic": return moveName.contains("psychic") || moveName.contains("psyshock") || moveName.contains("mind") || moveName.contains("zen");
            case "dark": return moveName.contains("dark") || moveName.contains("foul") || moveName.contains("night") || moveName.contains("sucker");
            case "fairy": return moveName.contains("moon") || moveName.contains("dazzl") || moveName.contains("play") || moveName.contains("charm");
            default: return false;
        }
    }

    private static class ScoredMove {
        Pokemon.PokemonMove move;
        int score;

        ScoredMove(Pokemon.PokemonMove move, int score) {
            this.move = move;
            this.score = score;
        }
    }
}