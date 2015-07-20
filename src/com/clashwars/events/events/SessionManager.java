package com.clashwars.events.events;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.events.Events;
import com.clashwars.events.config.data.SessionCfg;
import com.clashwars.events.events.koh.KohSession;
import com.clashwars.events.events.race.RaceSession;
import com.clashwars.events.events.smash.SmashSession;
import com.clashwars.events.events.spleef.SpleefSession;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class to access and manage all GameSessions.
 * When getting a session by EventType and MapName it will create a new session if it doesn't exists.
 */
public class SessionManager {

    private Events events;
    private SessionCfg sessionCfg;

    private HashMap<Integer, GameSession> sessions = new HashMap<Integer, GameSession>();

    public SessionManager(Events events) {
        this.events = events;
        events.sm = this;
        sessionCfg = events.sessionCfg;

        //Load in sessions from config
        Map<Integer, SessionData> cfgSessions = sessionCfg.getSessions();
        for (SessionData data : cfgSessions.values()) {
            createSession(data);
        }
    }

    public void unload() {
        for (GameSession session : sessions.values()) {
            session.unload();
        }
    }




    //Create a new session.
    private GameSession createSession(EventType type, String mapName) {
        int sessionID = sessionCfg.SESSION_COUNT;
        sessionCfg.SESSION_COUNT++;

        SessionData data = new SessionData(type, mapName);
        data.setSessionID(sessionID);
        sessionCfg.setSession(sessionID, data);

        GameSession session = null;
        if (type == EventType.KOH) {
            session = new KohSession(data, false);
        } else if (type == EventType.SPLEEF) {
            session = new SpleefSession(data, false);
        } else if (type == EventType.SMASH) {
            session = new SmashSession(data, false);
        } else if (type == EventType.RACE) {
            session = new RaceSession(data, false);
        }
        if (session != null) {
            if (session.getMap() != null && session.getMap().isValid() && !session.getMap().isClosed()) {
                session.setState(State.OPENED);
            } else {
                session.setState(State.CLOSED);
            }
            sessions.put(sessionID, session);
        }
        return session;
    }

    private GameSession createSession(SessionData data) {
        GameSession session = null;
        if (data.getEventType() == EventType.KOH) {
            session = new KohSession(data, true);
        } else if (data.getEventType() == EventType.SPLEEF) {
            session = new SpleefSession(data, true);
        } else if (data.getEventType() == EventType.SMASH) {
            session = new SmashSession(data, true);
        } else if (data.getEventType() == EventType.RACE) {
            session = new RaceSession(data, true);
        }

        if (session == null || session.getID() < 0) {
            return null;
        }

        sessions.put(session.getID(), session);
        return session;
    }

    /** Delete the session with the specified ID so that a new session can be started. */
    public void deleteSession(int sessionID) {
        if (sessions.containsKey(sessionID)) {
            sessions.remove(sessionID);
            sessionCfg.removeSession(sessionID);
        }
    }


    /**
     * Get/create a GameSession from the specified EventType and mapName.
     * If there is no session for this event-map then it will create a new session and return this.
     */
    public GameSession getSession(EventType eventType, String mapName) {
        for (GameSession session : sessions.values()) {
            if (session.getType() == eventType && session.getMapName().equals(mapName)) {
                return session;
            }
        }
        return createSession(eventType, mapName);
    }

    /** Get the hashmap with all the sessions by ID */
    public HashMap<Integer, GameSession> getSessions() {
        return sessions;
    }

    /** Returns true if there is a session for this event/map and false if not. */
    public boolean hasSession(EventType eventType, String mapName) {
        for (GameSession session : sessions.values()) {
            if (session.getType() == eventType && session.getMapName().equals(mapName)) {
                return true;
            }
        }
        return false;
    }

    /** Get a GameSession by ID. If the session doesn't exist it will return null! */
    public GameSession getSession(Integer id) {
        if (sessions.containsKey(id)) {
            return sessions.get(id);
        }
        return null;
    }


    /** Get a session at the given entity location. Returns null if no valid session was found */
    public GameSession getSession(Entity entity) {
        return getSession(entity.getLocation());
    }
    /** Get a session at the given block location. Returns null if no valid session was found */
    public GameSession getSession(Block block) {
        return getSession(block.getLocation());
    }
    /** Get a session at the given location. Returns null if no valid session was found. */
    public GameSession getSession(Location location) {
        for (GameSession session : sessions.values()) {
            if (session.getMap() != null && session.getMap().isValid()) {
                Cuboid mapCub = session.getMap().getCuboid("arena");
                if (mapCub != null && mapCub.contains(location)) {
                    return session;
                }
            }
        }
        return null;
    }

}
