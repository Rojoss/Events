package com.clashwars.events.maps;

import com.clashwars.events.Events;
import com.clashwars.events.events.EventType;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager class to access and manage all event maps.
 * All maps are loaded from config when this class is created.
 */
public class MapManager {

    private Events ev;

    public HashMap<String, EventMap> maps = new HashMap<String, EventMap>();


    public MapManager(Events ev) {
        this.ev = ev;
        loadMaps();
    }


    private void loadMaps() {
        HashMap<String, MapData> cfgMaps = ev.mapCfg.getMaps();
        for (Map.Entry<String, MapData> entry : cfgMaps.entrySet()) {
            maps.put(entry.getValue().getEventType() + "-" + entry.getKey(), new EventMap(entry.getKey(), entry.getValue()));
        }
    }

    /** Try to find a map with the given event type and map name. */
    public EventMap getMap(EventType eventType, String mapName) {
        return getMap(eventType.toString().toLowerCase() + "-" + mapName);
    }

    /**
     * Try to find a map with the given tag.
     * A map tag is the event name and then the map name. 'event-map'
     */
    public EventMap getMap(String mapTag) {
        if (!maps.containsKey(mapTag)) {
            return null;
        }
        return maps.get(mapTag);
    }

}
