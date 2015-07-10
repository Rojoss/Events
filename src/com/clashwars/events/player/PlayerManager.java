package com.clashwars.events.player;

import com.clashwars.events.Events;
import com.clashwars.events.config.data.PlayerCfg;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;


/**
 * Manager class to access and manage all CWPlayers.
 * When this class is created all players will be loaded in from config.
 */
public class PlayerManager {

    private Events events;
    private PlayerCfg pcfg;

    private HashMap<UUID, CWPlayer> players = new HashMap<UUID, CWPlayer>();

    public PlayerManager(Events events) {
        this.events = events;
        this.pcfg = events.playerCfg;
        populate();
    }

    /** Load in all players from config and create a CWPlayer instance for each player */
    private void populate() {
        Long t = System.currentTimeMillis();
        Map<UUID, PlayerData> cfgPlayers = pcfg.getPlayers();
        for (UUID uuid : cfgPlayers.keySet()) {
            players.put(uuid, new CWPlayer(uuid, cfgPlayers.get(uuid)));
        }
    }


    /** Get a CWPlayer from a OfflinePlayer. It will create a new CWPlayer if it doesn't exist */
    public CWPlayer getPlayer(OfflinePlayer p) {
        return getPlayer(p.getUniqueId());
    }

    /** Get a CWPlayer from a player name. It will create a new CWPlayer if it doesn't exist */
    public CWPlayer getPlayer(String name) {
        return getPlayer(events.getServer().getOfflinePlayer(name));
    }

    /** Get a CWPlayer from a player UUID. It will create a new CWPlayer if it doesn't exist */
    public CWPlayer getPlayer(UUID uuid) {
        if (players.containsKey(uuid)) {
            return players.get(uuid);
        } else if (pcfg.PLAYERS.containsKey(uuid.toString())) {;
            CWPlayer cwp = new CWPlayer(uuid, pcfg.getPlayer(uuid));
            players.put(uuid, cwp);
            return cwp;
        } else {
            CWPlayer cwp = new CWPlayer(uuid, new PlayerData());
            players.put(uuid, cwp);
            return cwp;
        }
    }

    /** Get a hashmap with all CWPlayers by their UUID. */
    public HashMap<UUID, CWPlayer> getPlayers() {
        return players;
    }

    /**
     * Get a list of all CWPlayers.
     * If onlineOnly is set to true it will only return players that are online.
     */
    public List<CWPlayer> getPlayers(boolean onlineOnly) {
        List<CWPlayer> playerList = new ArrayList<CWPlayer>();
        for (CWPlayer cwp : players.values()) {
            if (cwp.isOnline()) {
                playerList.add(cwp);
            }
        }
        return playerList;
    }


    /**
     * Save all player's their data.
     */
    public void savePlayers() {
        for (CWPlayer cwp : players.values()) {
            cwp.savePlayer();
        }
    }


    /**
     * Remove the given CWPlayer.
     * If fromConfig is set to to true it will also remove the PlayerData.
     */
    public void removePlayer(CWPlayer cwp, boolean fromConfig) {
        removePlayer(cwp.getUUID(), fromConfig);
    }

    /**
     * Remove a CWPlayer from the specified Player.
     * If fromConfig is set to to true it will also remove the PlayerData.
     */
    public void removePlayer(Player p, boolean fromConfig) {
        removePlayer(p.getPlayer(), fromConfig);
    }

    /**
     * Remove a CWPlayer from the specified UUID.
     * If fromConfig is set to to true it will also remove the PlayerData.
     */
    public void removePlayer(UUID uuid, boolean fromConfig) {
        players.remove(uuid);
        if (fromConfig == true) {
            pcfg.removePlayer(uuid);
            pcfg.save();
        }
    }


    /**
     * Remove all CWPlayers.
     * If fromConfig is set to true it will also delete everyone their PlayerData.
     */
    public void removePlayers(boolean fromConfig) {
        players.clear();
        if (fromConfig == true) {
            pcfg.PLAYERS.clear();
            pcfg.save();
        }
    }
}
