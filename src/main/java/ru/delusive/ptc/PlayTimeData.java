package ru.delusive.ptc;

public class PlayTimeData {
    private String username;
    private int playTime;

    public PlayTimeData(String username, int playTime) {
        this.username = username;
        this.playTime = playTime;
    }

    public String getUsername() {
        return username;
    }

    public int getPlayTime() {
        return playTime;
    }
}
