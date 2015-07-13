package com.clashwars.events.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SessionData {

    private int sessionID = -1;
    private EventType eventType;
    private String mapName;
    private State state = State.CLOSED;
    private Long startTime;
    private int teleportID = 0;

    private List<String> players = new ArrayList<String>();
    private List<String> vipPlayers = new ArrayList<String>();
    private List<String> spectators = new ArrayList<String>();

    private HashMap<String, String> eventData = new HashMap<String, String>();

    public SessionData() {
        //-
    }

    public SessionData(EventType type, String mapName) {
        this.eventType = type;
        this.mapName = mapName;
    }


    public int getSessionID() {
        return sessionID;
    }
    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public EventType getEventType() {
        return eventType;
    }
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getMapName() {
        return mapName;
    }
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }

    public Long getStartTime() {
        return startTime;
    }
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public int getTeleportID() {
        return teleportID;
    }
    public void setTeleportID(int teleportID) {
        this.teleportID = teleportID;
    }

    public HashMap<String, String> getEventData() {
        return eventData;
    }
    public void setEventData(HashMap<String, String> eventData) {
        this.eventData = eventData;
    }
    public void addEventData(String key, Object value) {
        this.eventData.put(key, value.toString());
    }
    public String getEventData(String key) {
        if (!eventData.containsKey(key)) {
            return null;
        }
        return eventData.get(key);
    }

    public List<UUID> getPlayers() {
        return stringListToUUID(players);
    }
    public void setPlayers(List<UUID> players) {
        this.players = uuidListToString(players);
    }
    public void addPlayer(UUID player) {
        players.add(player.toString());
    }
    public void removePlayer(UUID player) {
        players.remove(player.toString());
    }

    public List<UUID> getVipPlayers() {
        return stringListToUUID(vipPlayers);
    }
    public void setVipPlayers(List<UUID> vipPlayers) {
        this.vipPlayers = uuidListToString(vipPlayers);
    }
    public void addVip(UUID player) {
        vipPlayers.add(player.toString());
    }
    public void removeVip(UUID player) {
        vipPlayers.remove(player.toString());
    }

    public List<UUID> getSpectators() {
        return stringListToUUID(spectators);
    }
    public void setSpectators(List<UUID> spectators) {
        this.spectators = uuidListToString(spectators);
    }
    public void addSpectator(UUID player) {
        spectators.add(player.toString());
    }
    public void removeSpectator(UUID player) {
        spectators.remove(player.toString());
    }



    private List<UUID> stringListToUUID(List<String> stringList) {
        List<UUID> uuidList = new ArrayList<UUID>();
        for (String uuid : stringList) {
            uuidList.add(UUID.fromString(uuid));
        }
        return uuidList;
    }

    private List<String> uuidListToString(List<UUID> uuidList) {
        List<String> stringList = new ArrayList<String>();
        for (UUID uuid : uuidList) {
            stringList.add(uuid.toString());
        }
        return stringList;
    }
}
