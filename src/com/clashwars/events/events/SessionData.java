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

    private List<UUID> players = new ArrayList<UUID>();
    private List<UUID> vipPlayers = new ArrayList<UUID>();
    private List<UUID> spectators = new ArrayList<UUID>();

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
        return players;
    }
    public void setPlayers(List<UUID> players) {
        this.players = players;
    }
    public void addPlayer(UUID player) {
        players.add(player);
    }
    public void removePlayer(UUID player) {
        players.remove(player);
    }

    public List<UUID> getVipPlayers() {
        return vipPlayers;
    }
    public void setVipPlayers(List<UUID> vipPlayers) {
        this.vipPlayers = vipPlayers;
    }
    public void addVip(UUID player) {
        players.add(player);
    }
    public void removeVip(UUID player) {
        players.remove(player);
    }

    public List<UUID> getSpectators() {
        return spectators;
    }
    public void setSpectators(List<UUID> spectators) {
        this.spectators = spectators;
    }
    public void addSpectator(UUID player) {
        players.add(player);
    }
    public void removeSpectator(UUID player) {
        players.remove(player);
    }
}
