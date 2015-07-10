package com.clashwars.events.events;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.events.Events;
import com.clashwars.events.events.koh.KohSession;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager class to access and manage all GameSessions.
 * When getting a session by EventType and MapName it will create a new session if it doesn't exists.
 */
public class SessionManager {

    private Events ev;

    private int sessionCount = 0;
    private List<GameSession> sessions = new ArrayList<GameSession>();

    public SessionManager(Events ev) {
        this.ev = ev;
    }




    //Create a new session.
    private GameSession createSession(EventType type, String mapName) {
        GameSession session = null;
        if (type == EventType.KOH) {
            session = new KohSession(sessionCount, type, mapName);
        }
        if (session != null) {
            sessions.add(session);
            sessionCount++;
        }
        return session;
    }


    /**
     * Get/create a GameSession from the specified EventType and mapName.
     * If there is no session for this event-map then it will create a new session and return this.
     */
    public GameSession getSession(EventType eventType, String mapName) {
        for (GameSession session : sessions) {
            if (session.getType() == eventType && session.getMapName().equals(mapName)) {
                return session;
            }
        }
        return createSession(eventType, mapName);
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
        for (GameSession session : sessions) {
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
