package com.cesba.pokeapp;

public class Medal {
    private int id;
    private String name;
    private boolean unlocked;
    private String unlockedDate;

    public Medal() { }

    public Medal(int id, String name, boolean unlocked, String unlockedDate) {
        this.id = id;
        this.name = name;
        this.unlocked = unlocked;
        this.unlockedDate = unlockedDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public String getUnlockedDate() { return unlockedDate; }
    public void setUnlockedDate(String unlockedDate) { this.unlockedDate = unlockedDate; }
}