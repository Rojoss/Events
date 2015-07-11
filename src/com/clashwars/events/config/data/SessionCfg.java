package com.clashwars.events.config.data;

import com.clashwars.cwcore.config.internal.EasyConfig;
import com.clashwars.events.Events;
import com.clashwars.events.events.SessionData;

import java.util.HashMap;
import java.util.Map;

public class SessionCfg extends EasyConfig {

    public int SESSION_COUNT = 0;
    public HashMap<Integer, String> SESSIONS = new HashMap<Integer, String>();

    public SessionCfg(String fileName) {
        this.setFile(fileName);
    }

    public Map<Integer, SessionData> getSessions() {
        Map<Integer, SessionData> sessions = new HashMap<Integer, SessionData>();
        for (int id : SESSIONS.keySet()) {
            sessions.put(id, Events.inst().getGson().fromJson(SESSIONS.get(id), SessionData.class));
        }
        return sessions;
    }

    public SessionData getSession(int id) {
        return Events.inst().getGson().fromJson(SESSIONS.get(id), SessionData.class);
    }

    public void setSession(int id, SessionData data) {
        SESSIONS.put(id, Events.inst().getGson().toJson(data, SessionData.class));
        save();
    }

    public void removeSession(int id) {
        SESSIONS.remove(id);
        save();
    }
}