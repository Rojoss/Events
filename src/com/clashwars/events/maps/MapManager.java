package com.clashwars.events.maps;

import com.clashwars.events.Events;
import com.clashwars.events.events.EventType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            maps.put(entry.getKey(), new EventMap(entry.getValue().getName(), entry.getValue()));
        }
    }

    /** Create a new map for the specified event with the specified map name. If the map already exisits it will return that map. */
    public EventMap createMap(EventType eventType, String mapName) {
        if (maps.containsKey(eventType.toString().toLowerCase() + "-" + mapName)) {
            return maps.get(eventType.toString().toLowerCase() + "-" + mapName);
        }
        EventMap map = new EventMap(mapName, new MapData(eventType, mapName));
        map.save();
        maps.put(eventType.toString().toLowerCase() + "-" + mapName, map);
        return map;
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

    /** Get a list of all maps from the specified event. If EventType is null it will return ALL maps. */
    public List<EventMap> getMaps(EventType eventType) {
        List<EventMap> list = new ArrayList<EventMap>();
        for (EventMap map : maps.values()) {
            if (eventType == null || map.getType() == eventType) {
                list.add(map);
            }
        }
        return list;
    }

    /** Get a list of all map names from the specified event. If EventType is null it will return ALL map names. */
    public List<String> getMapNames(EventType eventType) {
        List<String> list = new ArrayList<String>();
        for (EventMap map : maps.values()) {
            if (eventType == null || map.getType() == eventType) {
                list.add(map.getName());
            }
        }
        return list;
    }

}
