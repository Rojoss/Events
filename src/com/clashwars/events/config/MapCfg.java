package com.clashwars.events.config;

import com.clashwars.cwcore.config.internal.EasyConfig;
import com.clashwars.events.Events;
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
        for (String key : MAPS.keySet()) {
            maps.put(key, Events.inst().getGson().fromJson(MAPS.get(key), MapData.class));
        }
        return maps;
    }

    public MapData getMap(String mapName) {
        return Events.inst().getGson().fromJson(MAPS.get(mapName), MapData.class);
    }

    public void setMap(String mapName, MapData mapData) {
        MAPS.put(mapName, Events.inst().getGson().toJson(mapData, MapData.class));
        save();
    }

    public void removeMap(String mapName) {
        MAPS.remove(mapName);
        save();
    }
}
