package com.clashwars.events.events.koh;

import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.SessionData;

public class KohSession extends GameSession {

    public KohSession(SessionData data, boolean loadedFromConfig) {
        super(data, loadedFromConfig);
        session = this;
        maxTime = 600;
    }

}
