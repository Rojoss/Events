package com.clashwars.events.maps;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.EventType;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for all EventMap data like locations, cuboids, slots etc.
 */
public class MapData {

    private String name;
    private String[] authors = new String[] {};
    private EventType eventType;
    private boolean closed = true;

    private int minPlayers = 2;
    private int maxPlayers = 4;
    private int vipSpots = 2;

    private HashMap<String, String> cuboids = new HashMap<String, String>();
    private HashMap<String, String> locs = new HashMap<String, String>();

    public MapData() {
        //--
    }

    public MapData(EventType eventType, String name) {
        this.eventType = eventType;
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public EventType getEventType() {
        return eventType;
    }


    public String[] getAuthors() {
        return authors;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }


    public boolean isClosed() {
        return closed;
    }

    public void setclosed(boolean closed) {
        this.closed = closed;
    }


    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }


    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }


    public int getVipSpots() {
        return vipSpots;
    }

    public void setVipSpots(int vipSpots) {
        this.vipSpots = vipSpots;
    }


    public HashMap<String, Cuboid> getCuboids() {
        HashMap<String, Cuboid> cubs = new HashMap<String, Cuboid>();
        for (Map.Entry<String,String> entry : cuboids.entrySet()) {
            cubs.put(entry.getKey(), Cuboid.deserialize(entry.getValue()));
        }
        return cubs;
    }

    public void setCuboid(String name, Cuboid cuboid) {
        cuboids.put(name, cuboid.toString());
    }


    public HashMap<String, Location> getLocs() {
        HashMap<String, Location> lo = new HashMap<String, Location>();
        for (Map.Entry<String,String> entry : locs.entrySet()) {
            lo.put(entry.getKey(), CWUtil.locFromStringSimple(entry.getValue()));
        }
        return lo;
    }

    public void setLoc(String name, Location location) {
        locs.put(name, CWUtil.locToStringSimple(location));
    }
}
