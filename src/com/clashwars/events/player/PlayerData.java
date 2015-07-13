package com.clashwars.events.player;

public class PlayerData {

    private int sessionID = -1;
    private boolean spectating = false;
    private int teleportID = -1;

    public PlayerData() {
        //--
    }

    /** Reset all the data values back to default. */
    public void reset() {
        sessionID = -1;
        teleportID = -1;
    }


    public int getSessionID() {
        return sessionID;
    }
    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public boolean isSpectating() {
        return spectating;
    }
    public void setSpectating(boolean spectating) {
        this.spectating = spectating;
    }


    public int getTeleportID() {
        return teleportID;
    }
    public void setTeleportID(int teleportID) {
        this.teleportID = teleportID;
    }
}
