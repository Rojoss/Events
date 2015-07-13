package com.clashwars.events.config.data;

import com.clashwars.cwcore.config.internal.EasyConfig;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.events.SessionData;

import java.util.HashMap;
import java.util.Map;

public class SessionCfg extends EasyConfig {

    public int SESSION_COUNT = 0;
    public HashMap<String, String> SESSIONS = new HashMap<String, String>();

    public SessionCfg(String fileName) {
        this.setFile(fileName);
    }

    public Map<Integer, SessionData> getSessions() {
        Map<Integer, SessionData> sessions = new HashMap<Integer, SessionData>();
        for (String id : SESSIONS.keySet()) {
            sessions.put(CWUtil.getInt(id), Events.inst().getGson().fromJson(SESSIONS.get(id), SessionData.class));
        }
        return sessions;
    }

    public SessionData getSession(int id) {
        return Events.inst().getGson().fromJson(SESSIONS.get(Integer.toString(id)), SessionData.class);
    }

    public void setSession(int id, SessionData data) {
        SESSIONS.put(Integer.toString(id), Events.inst().getGson().toJson(data, SessionData.class));
        save();
    }

    public void removeSession(int id) {
        SESSIONS.remove(id);
        save();
    }
}