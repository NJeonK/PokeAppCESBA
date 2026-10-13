package com.cesba.pokeapp;

import java.util.List;

public class BattleRecord {
    private String id;
    private String mode;
    private String date;
    private List<String> playerTeam;
    private List<String> enemyTeam;
    private String winner;
    private boolean userWon;
    private List<String> battleLogs;

    public BattleRecord() { }

    public BattleRecord(String id, String mode, String date, List<String> playerTeam, List<String> enemyTeam, String winner, boolean userWon) {
        this.id = id;
        this.mode = mode;
        this.date = date;
        this.playerTeam = playerTeam;
        this.enemyTeam = enemyTeam;
        this.winner = winner;
        this.userWon = userWon;
    }

    public BattleRecord(String id, String mode, String date, List<String> playerTeam, List<String> enemyTeam, String winner, boolean userWon, List<String> battleLogs) {
        this.id = id;
        this.mode = mode;
        this.date = date;
        this.playerTeam = playerTeam;
        this.enemyTeam = enemyTeam;
        this.winner = winner;
        this.userWon = userWon;
        this.battleLogs = battleLogs;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<String> getPlayerTeam() { return playerTeam; }
    public void setPlayerTeam(List<String> playerTeam) { this.playerTeam = playerTeam; }

    public List<String> getEnemyTeam() { return enemyTeam; }
    public void setEnemyTeam(List<String> enemyTeam) { this.enemyTeam = enemyTeam; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public boolean isUserWon() { return userWon; }
    public void setUserWon(boolean userWon) { this.userWon = userWon; }

    public List<String> getBattleLogs() { return battleLogs; }
    public void setBattleLogs(List<String> battleLogs) { this.battleLogs = battleLogs; }
}