package com.clashwars.events.player;

public class PlayerData {

    private int sessionID = -1;

    public PlayerData() {
        //--
    }

    /** Reset all the data values back to default. */
    public void reset() {
        sessionID = -1;
    }


    public int getSessionID() {
        return sessionID;
    }
    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }
}
