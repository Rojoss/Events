package com.clashwars.events.config.data;

import com.clashwars.cwcore.config.internal.EasyConfig;
import com.clashwars.events.Events;
import com.clashwars.events.events.EventType;
import com.clashwars.events.maps.MapData;

import java.util.HashMap;
import java.util.Map;

public class MapCfg extends EasyConfig {

    public HashMap<String, String> MAPS = new HashMap<String, String>();

    public MapCfg(String fileName) {
        this.setFile(fileName);
    }

    public HashMap<String, MapData> getMaps() {
        HashMap<String, MapData> maps = new HashMap<String, MapData>();
        for (String mapTag : MAPS.keySet()) {
            maps.put(mapTag, Events.inst().getGson().fromJson(MAPS.get(mapTag), MapData.class));
        }
        return maps;
    }

    public MapData getMap(String mapTag) {
        return Events.inst().getGson().fromJson(MAPS.get(mapTag), MapData.class);
    }

    public void setMap(String mapTag, MapData mapData) {
        MAPS.put(mapTag, Events.inst().getGson().toJson(mapData, MapData.class));
        save();
    }

    public void removeMap(String mapTag) {
        MAPS.remove(mapTag);
        save();
    }

    public void renameMap(EventType event, String prevName, String newName) {
        String prevTag = event.toString().toLowerCase() + "-" + prevName;
        if (MAPS.containsKey(prevTag)) {
            MapData data = getMap(prevTag);
            removeMap(prevTag);
            setMap(event.toString().toLowerCase() + "-" + newName, data);
        }
    }
}
