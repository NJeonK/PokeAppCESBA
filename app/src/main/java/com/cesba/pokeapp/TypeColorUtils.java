package com.cesba.pokeapp;

import android.graphics.Color;

public class TypeColorUtils {

    public static int getTypeColor(String typeName) {
        if (typeName == null) return Color.parseColor("#A8A878");
        switch (typeName.toLowerCase().trim()) {
            case "fire": case "fuego": return Color.parseColor("#F08030");
            case "water": case "agua": return Color.parseColor("#6890F0");
            case "grass": case "planta": return Color.parseColor("#78C850");
            case "electric": case "electrico": case "eléctrico": return Color.parseColor("#F8D030");
            case "ice": case "hielo": return Color.parseColor("#98D8D8");
            case "fighting": case "lucha": return Color.parseColor("#C03028");
            case "poison": case "veneno": return Color.parseColor("#A040A0");
            case "ground": case "tierra": return Color.parseColor("#E0C068");
            case "flying": case "volador": return Color.parseColor("#A890F0");
            case "psychic": case "psiquico": case "psíquico": return Color.parseColor("#F85888");
            case "bug": case "bicho": return Color.parseColor("#A8B820");
            case "rock": case "roca": return Color.parseColor("#B8A038");
            case "ghost": case "fantasma": return Color.parseColor("#705898");
            case "dragon": case "dragón": return Color.parseColor("#7038F8");
            case "dark": case "siniestro": return Color.parseColor("#705848");
            case "steel": case "acero": return Color.parseColor("#B8B8D0");
            case "fairy": case "hada": return Color.parseColor("#EE99AC");
            default: return Color.parseColor("#A8A878");
        }
    }
}