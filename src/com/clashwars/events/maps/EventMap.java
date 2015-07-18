package com.clashwars.events.maps;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import com.clashwars.events.events.EventType;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Each map that is registered has a instance of this.
 * Most data in this class is loaded from the EventData configuration class.
 */
public class EventMap {

    private String name = "";
    private String tag = "";
    private MapData data;

    private boolean valid = false;

    /**
     * Creates a new instance of a event map with the given map name and MapData from the config.
     * A tag will be created for each map which is based on the event type and map name. 'event-mapname'
     */
    public EventMap(String name, MapData data) {
        this.name = name;
        this.data = data;

        tag = data.getEventType().toString().toLowerCase() + "-" + name;
        validateMap();
    }


    /**
     * Run map validation to check if all the required locations/cuboids etc are set up.
     * It will return a error message as String if something isn't set up and an empty string if it's set up correct.
     */
    public String validateMap() {
        if (data == null) {
            valid = false;
            return "No map data found.";
        }
        if (data.getEventType() == null) {
            valid = false;
            return "No valid event type specified.";
        }

        EventType eventType = data.getEventType();

        List<SetupOption> setupOptions = eventType.getEventClass().getSetupOptions();
        for (SetupOption option : setupOptions) {
            if (option.type == SetupType.CUBOID) {
                if (!data.getCuboids().containsKey(option.name)) {
                    valid = false;
                    return "Missing CUBOID: &4&l" + option.name + " &8'&7" + option.description + "&8'";
                }
            } else if (option.type == SetupType.LOCATION || option.type == SetupType.BLOCK_LOC) {
                if (!data.getLocs().containsKey(option.name)) {
                    valid = false;
                    return "Missing " + (option.type == SetupType.BLOCK_LOC ? "BLOCK " : "") + "LOCATION: &4&l" + option.name + " &8'&7" + option.description + "&8'";
                }
            } else if (option.type == SetupType.MULTI_LOC) {
                if (getMultiLocs(option.name).size() <= 0) {
                    valid = false;
                    return "Needs at least 1 location for MULTILOC: &4&l" + option.name + " &8'&7" + option.description + "&8'";
                }
            }
        }
        valid = true;
        return "";
    }

    public void save() {
        Events.inst().mapCfg.setMap(getTag(), data);
    }


    /** Returns the name of this map */
    public String getName() {
        return name;
    }

    /** Returns the map tag which is 'eventname-mapname' */
    public String getTag() {
        return tag;
    }

    /** Returns MapData configuration class with all data. */
    public MapData getData() {
        return data;
    }

    /** Returns the EventType of this map. */
    public EventType getType() {
        return data.getEventType();
    }

    /** Returns true if the arena is set up properly and validated. */
    public boolean isValid() {
        return valid;
    }


    /** Returns true if the arena set to closed */
    public boolean isClosed() {
        return data.isClosed();
    }

    /** Set the map to closed state. NOTE: This only modifies the map data it doesn't actually close it */
    public void setClosed(boolean closed) {
        data.setclosed(closed);
        save();
    }


    /** Get a location from a LOCATION setup option. */
    public Location getLocation(String name) {
        if (data.getLocs().containsKey(name)) {
            return data.getLocs().get(name);
        }
        return null;
    }

    /** Set a location for the map. It will only set the location if the map has a setupoption of type LOCATION with the specified name. */
    public boolean setLocation(String name, Location loc) {
        if (getType().getEventClass().hasSetupOption(SetupType.LOCATION, name)) {
            data.setLoc(name, loc);
            save();
            return true;
        }
        return false;
    }


    /** Get a list of multiple locations from a MULTI_LOC setup option. */
    public HashMap<Integer, Location> getMultiLocs(String name) {
        HashMap<Integer, Location> locs = new HashMap<Integer, Location>();
        for (Map.Entry<String, Location> entry : data.getLocs().entrySet()) {
            if (entry.getKey().startsWith(name + "_")) {
                String[] split = entry.getKey().split("_");
                if (split.length > 1 && CWUtil.getInt(split[1]) >= 0) {
                    locs.put(CWUtil.getInt(split[1]), entry.getValue());
                }
            }
        }
        return locs;
    }

    /** Get a specific location from a multi loc location. */
    public Location getMultiLoc(String name, int ID) {
        if (data.getLocs().containsKey(name + "_" + ID)) {
            return data.getLocs().get(name + "_" + ID);
        }
        return null;
    }

    /** Set a multi loc for the map. It will only set the location if the map has a setupoption of type MULTI_LOC with the specified name. */
    public boolean setMultiLoc(String name, int ID, Location loc) {
        if (getType().getEventClass().hasSetupOption(SetupType.MULTI_LOC, name)) {
            data.setLoc(name + "_" + ID, loc);
            save();
            return true;
        }
        return false;
    }



    /** Get a cuboid from a CUBOID setup option. */
    public Cuboid getCuboid(String name) {
        if (data.getCuboids().containsKey(name)) {
            return data.getCuboids().get(name);
        }
        return null;
    }

    /** Set a cuboid for the map. It will only set the cuboid if the map has a setupoption of type CUBOID with the specified name. */
    public boolean setCuboid(String name, Cuboid cuboid) {
        if (getType().getEventClass().hasSetupOption(SetupType.CUBOID, name)) {
            data.setCuboid(name, cuboid);
            save();
            return true;
        }
        return false;
    }


    /** Get a block from a BLOCK_LOC setup option. */
    public Block getBlock(String name) {
        if (data.getLocs().containsKey(name)) {
            return data.getLocs().get(name).getBlock();
        }
        return null;
    }

    /** Set a block loc for the map. It will only set the block loc if the map has a setupoption of type BLOCK_LOC with the specified name. */
    public boolean setblock(String name, Block block) {
        if (getType().getEventClass().hasSetupOption(SetupType.BLOCK_LOC, name)) {
            data.setLoc(name, block.getLocation());
            save();
            return true;
        }
        return false;
    }



    /** Returns the minimum amount of players required to play the map */
    public int getMinPlayers() {
        return data.getMinPlayers();
    }

    /** Set the minimum amount of players required to play the map */
    public void setMinPlayers(int minPlayers) {
        data.setMinPlayers(minPlayers);
        save();
    }


    /** Returns the maximum amount of players allowed to play in the map (exclusive VIP slots and staff) */
    public int getMaxPlayers() {
        return data.getMaxPlayers();
    }

    /** Set the maximum amount of players allowed to play in the map (exclusive VIP slots and staff) */
    public void setMaxPlayers(int maxPlayers) {
        data.setMaxPlayers(maxPlayers);
        save();
    }


    /** Returns the amount of additional slots above the max for VIP's */
    public int getVipSpots() {
        return data.getVipSpots();
    }

    /** Set the amount of additional slots above the max for VIP's */
    public void setVipSpots(int vipSpots) {
        data.setVipSpots(vipSpots);
        save();
    }


    /** Returns a list of authors who created the map */
    public String[] getAuthors() {
        return data.getAuthors();
    }

    /** Set the list of authors who created the map */
    public void setAuthors(String[] authors) {
        data.setAuthors(authors);
        save();
    }
}
