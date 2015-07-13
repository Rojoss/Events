package com.clashwars.events.events;

import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;

import java.util.*;

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

    private HashMap<String, String> modifierValues = new HashMap<String, String>();
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

    public HashMap<Modifier, ModifierOption> getModifierValues() {
        HashMap<Modifier, ModifierOption> map = new HashMap<Modifier, ModifierOption>();
        for (Map.Entry<String, String> entry : modifierValues.entrySet()) {
            Modifier modifier = Modifier.valueOf(entry.getKey());
            map.put(modifier, modifier.getOptions()[CWUtil.getInt(entry.getValue())]);
        }
        return map;
    }

    public void setModifierValues(HashMap<Modifier, ModifierOption> options) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (Map.Entry<Modifier, ModifierOption> entry : options.entrySet()) {
            map.put(entry.getKey().toString(), Integer.toString(entry.getValue().ID));
        }
        modifierValues = map;
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
