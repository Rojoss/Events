package com.clashwars.events.maps;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.events.events.EventType;
import org.bukkit.Location;

import java.util.HashMap;

/**
 * Configuration class for all EventMap data like locations, cuboids, slots etc.
 */
public class MapData {

    private String[] authors;
    private EventType eventType;
    private boolean closed = true;

    private int minPlayers = 2;
    private int maxPlayers = 4;
    private int vipSpots = 2;

    private HashMap<String, Cuboid> cuboids = new HashMap<String, Cuboid>();
    private HashMap<String, Location> locs = new HashMap<String, Location>();

    public MapData() {
        //--
    }


    public String[] getAuthors() {
        return authors;
    }

    public EventType getEventType() {
        return eventType;
    }

    public boolean isClosed() {
        return closed;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getVipSpots() {
        return vipSpots;
    }

    public HashMap<String, Cuboid> getCuboids() {
        return cuboids;
    }

    public HashMap<String, Location> getLocs() {
        return locs;
    }

}
