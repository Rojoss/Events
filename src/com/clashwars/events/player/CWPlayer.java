package com.clashwars.events.player;

import com.clashwars.cwcore.CooldownManager;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.player.Freeze;
import com.clashwars.cwcore.player.Vanish;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.config.data.PlayerCfg;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.runnables.RelogRunnable;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.UUID;

/**
 * Custom player class to handle all player specific things like resetting etc.
 * Each player gets an instance of this class.
 * When a player logs off the CWPlayer instance stays so players can be offline/invalid.
 */
public class CWPlayer {

    private Events events;
    private PlayerCfg pcfg;

    private UUID uuid;
    private PlayerData data;

    private CooldownManager cdm = new CooldownManager();
    private RelogRunnable relogRunnable;

    private EventType selectedEvent;
    private String selectedMap;

    /** Create a new CWPlayer instance with the given player UUID and PlayerData. */
    public CWPlayer(UUID uuid, PlayerData data) {
        this.events = Events.inst();
        this.pcfg = events.playerCfg;

        this.uuid = uuid;
        this.data = data;
    }


    //==============================================
    //=============== Custom methods ===============
    //==============================================

    /** Reset the player completely. Will reset stuff like inventory, health, hunger, gamemode, potioneffects etc... */
    public void reset() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }
        CWUtil.resetPlayer(player, GameMode.SURVIVAL);
        Freeze.unfreeze(player.getUniqueId());
        Vanish.unvanish(player.getUniqueId());
    }

    /** Reset all data from this player. */
    public void resetData() {
        data.reset();
        savePlayer();
    }

    /** Returns the PlayerData from this player for all config setings/data. */
    public PlayerData getPlayerData() {
        return data;
    }

    /** Save this player his data to config */
    public void savePlayer() {
        pcfg.setPlayer(uuid, data);
    }


    /** Get the cooldown manager for this player */
    public CooldownManager getCDM() {
        return cdm;
    }


    /** Returns true if the player is in a GameSession */
    public boolean inSession() {
        return data.getSessionID() >= 0;
    }

    /** Get the game session from this users. Returns null if the player has no session */
    public GameSession getSession() {
        return events.sm.getSession(data.getSessionID());
    }

    /** Set the player his session */
    public void setSession(GameSession session) {
        data.setSessionID(session.getID());
        savePlayer();
    }

    /** Remove the player his session */
    public void removeSession() {
        data.setSessionID(-1);
        savePlayer();
    }


    /** Returns true if the player is spectating the current session. Always check first if the player is in a session! */
    public boolean isSpectating() {
        return data.isSpectating();
    }

    /** Set if the player is spectating the session or not */
    public void setSpectating(boolean spectating) {
        data.setSpectating(spectating);
        savePlayer();
    }


    /** Get the player his teleport ID. Can also be seen as a game user ID however it may not be unique if there aren't enough teleport spots. */
    public int getTeleportID() {
        return data.getTeleportID();
    }

    /** Set the player his teleport ID. */
    public void setTeleportID(int teleportID) {
        data.setTeleportID(teleportID);
        savePlayer();
    }


    public void createRelogRunnable() {
        if (!hasRelogRunnable()) {
            relogRunnable = new RelogRunnable(this);
        }
    }

    public void removeRelogRunnable() {
        if (hasRelogRunnable()) {
            relogRunnable.cancel();
            relogRunnable = null;
        }
    }

    public boolean hasRelogRunnable() {
        return relogRunnable != null;
    }


    public EventType getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(EventType selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public String getSelectedMap() {
        return selectedMap;
    }

    public void setSelectedMap(String selectedMap) {
        this.selectedMap = selectedMap;
    }






    //==============================================
    //=============== Player methods ===============
    //==============================================

    public UUID getUUID() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public void sendMessage(String msg) {
        getPlayer().sendMessage(msg);
    }

    public String getName() {
        return getOfflinePlayer().getName();
    }

    public Location getLocation() {
        return getPlayer().getLocation();
    }

    public World getWorld() {
        return getPlayer().getWorld();
    }

    public void playSound(Location loc, Sound sound, float volume, float pitch) {
        getPlayer().playSound(loc, sound, volume, pitch);
    }

    public boolean isOnline() {
        return getPlayer() == null ? false : getPlayer().isOnline();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CWPlayer) {
            CWPlayer other = (CWPlayer)obj;

            return other.getUUID().equals(getUUID());
        }
        return false;
    }

}
