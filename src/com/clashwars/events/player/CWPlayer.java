package com.clashwars.events.player;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.events.Events;
import com.clashwars.events.config.data.PlayerCfg;
import com.clashwars.events.events.GameSession;
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

    private GameSession session;

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

        player.getInventory().clear();
        player.getInventory().setHelmet(new CWItem(Material.AIR));
        player.getInventory().setChestplate(new CWItem(Material.AIR));
        player.getInventory().setLeggings(new CWItem(Material.AIR));
        player.getInventory().setBoots(new CWItem(Material.AIR));
        player.updateInventory();

        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setFireTicks(0);
        player.setGameMode(GameMode.SURVIVAL);
        player.setSaturation(10);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0);

        Collection<PotionEffect> effects = player.getActivePotionEffects();
        for (PotionEffect pe : effects) {
            player.removePotionEffect(pe.getType());
        }

        player.updateInventory();
    }

    /** Reset all data from this player. */
    public void resetData() {
        data.reset();
    }

    /** Returns the PlayerData from this player for all config setings/data. */
    public PlayerData getPlayerData() {
        return data;
    }

    /** Save this player his data to config */
    public void savePlayer() {
        pcfg.setPlayer(uuid, data);
    }

    /** Get a suffix for teams based on permissions. If staff it will return '_s' and if VIP it will return '_v' */
    public String getTeamSuffix() {
        if (getPlayer().hasPermission("team.staff")) {
            return "_s";
        }
        if (getPlayer().hasPermission("team.vip")) {
            return "_v";
        }
        return "";
    }


    /** Returns true if the player is in a GameSession */
    public boolean inSession() {
        return session != null;
    }

    /** Get the game session from this users. Returns null if the player has no session */
    public GameSession getSession() {
        return session;
    }

    /** Set the player his session */
    public void setSession(GameSession session) {
        this.session = session;
        data.setSessionID(session.getID());
    }

    /** Remove the player his session */
    public void removeSession() {
        session = null;
        data.setSessionID(-1);
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
