package com.clashwars.events.events.koh;

import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;

public class KohSession extends GameSession {

    public KohSession(int ID, EventType type, String mapName) {
        super(ID, type, mapName);
        session = this;
        maxTime = 600;
    }

}
