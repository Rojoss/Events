package com.clashwars.events.util;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.State;
import com.clashwars.events.maps.EventMap;
import com.clashwars.events.player.CWPlayer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class Util {

    public static String formatMsg(String msg) {
        return CWUtil.integrateColor("&8[&4Events&8] &6" + msg);
    }

    /**
     * Update the status of the specified map.
     * If a session is provided (if not null) then it will update the player count and state based of the session data.
     * It will update the sign if it's set and update the game menu.
     */
    public static void updateStatus(EventMap map, GameSession session) {
        updateSign(map, session);
        Events.inst().gameMenu.updateMenu();
        Events.inst().mapMenu.updateMenu(map.getType());
    }

    private static void updateSign(EventMap map, GameSession session) {
        Block block = map.getBlock("sign");
        if (block == null || (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN)) {
            return;
        }
        Sign sign = (Sign)block.getState();
        sign.setLine(1, CWUtil.integrateColor(map.getName()));
        if (session == null) {
            sign.setLine(0, CWUtil.integrateColor("&7[" + CWUtil.capitalize(map.getType().toString().toLowerCase()) + "]"));
            sign.setLine(2, CWUtil.integrateColor("&70&8/&7" + map.getMaxPlayers()));
            if (map.isClosed() || !map.validateMap().isEmpty()) {
                sign.setLine(3, CWUtil.integrateColor(State.CLOSED.getSignText()));
            } else {
                sign.setLine(3, CWUtil.integrateColor(State.OPENED.getSignText()));
            }
        } else {
            if (session.getPlayerCount(false) <= 0) {
                sign.setLine(0, CWUtil.integrateColor("&7[" + CWUtil.capitalize(map.getType().toString().toLowerCase()) + "]"));
                sign.setLine(2, CWUtil.integrateColor("&70&8/&7" + map.getMaxPlayers()));
            } else {
                sign.setLine(0, CWUtil.integrateColor("&5[" + CWUtil.capitalize(map.getType().toString().toLowerCase()) + "]"));
                if (session.getSpecPlayerSize() > 0) {
                    sign.setLine(2, CWUtil.integrateColor("&a&l" + session.getPlayerCount(false) + "&8/&2" + map.getMaxPlayers() + " &d+" + session.getSpecPlayerSize()));
                } else {
                    sign.setLine(2, CWUtil.integrateColor("&a&l" + session.getPlayerCount(false) + "&8/&2" + map.getMaxPlayers()));
                }
            }
            sign.setLine(3, CWUtil.integrateColor(session.getState().getSignText()));
        }
        sign.update(true);
    }


    /** Equip the given list with CWItems on the player. It will set armor in the proper slots if the item is armor. */
    public static void equipItems(Player player, List<CWItem> items) {
        for (CWItem item : items) {
            if (item.getType().toString().endsWith("_HELMET")) {
                player.getInventory().setHelmet(item);
            } else if (item.getType().toString().endsWith("_CHESTPLATE")) {
                player.getInventory().setChestplate(item);
            } else if (item.getType().toString().endsWith("_LEGGINGS")) {
                player.getInventory().setLeggings(item);
            } else if (item.getType().toString().endsWith("_BOOTS")) {
                player.getInventory().setBoots(item);
            } else {
                item.giveToPlayer(player);
            }
        }
    }

    /** Teleport the specified player back to the lobby and give the lobby equipment */
    public static void teleportLobby(Player player) {
        CWPlayer cwp = Events.inst().pm.getPlayer(player);
        cwp.reset();
        cwp.resetData();

        if (cwp.getSignJoinLoc() != null) {
            player.teleport(cwp.getSignJoinLoc());
        } else {
            player.teleport(player.getWorld().getSpawnLocation());
        }

        CWUtil.resetPlayer(player, GameMode.SURVIVAL);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999999, 1));
        Equipment.LOBBY.equip(player);
    }
}
