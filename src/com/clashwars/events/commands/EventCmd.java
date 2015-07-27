package com.clashwars.events.commands;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.cuboid.Selection;
import com.clashwars.cwcore.cuboid.SelectionStatus;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.commands.internal.PlayerCmd;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.maps.EventMap;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import com.clashwars.events.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class EventCmd extends PlayerCmd {

    @Override
    public void onCommand(Player player, String[] args) {
        if (args.length < 1) {
            showHelp(player);
            return;
        }

        CWPlayer cwp = events.pm.getPlayer(player);
        if (cwp.inSession() && cwp.getSession() != null) {
            cwp.setSelectedEvent(cwp.getSession().getType());
            cwp.setSelectedMap(cwp.getSession().getMapName());
        }

        if (args[0].equalsIgnoreCase("select")) {
            if (args.length < 3) {
                player.sendMessage(Util.formatMsg("&cInvalid usage! &7/event select {event} {mapName}"));
                return;
            }

            EventType eventType = EventType.fromString(args[1]);
            if (eventType == null) {
                player.sendMessage(Util.formatMsg("&cInvalid event specified!"));
                player.sendMessage(CWUtil.integrateColor("&4Events&8: &7" + CWUtil.implode(EventType.getEventNames(), "&8, &7")));
                return;
            }

            EventMap map = events.mm.getMap(eventType, args[2]);
            if (map == null) {
                player.sendMessage(Util.formatMsg("&cInvalid map specified!"));
                player.sendMessage(CWUtil.integrateColor("&4Maps&8: &7" + CWUtil.implode(events.mm.getMapNames(eventType), "&8, &7")));
                return;
            }

            cwp.setSelectedEvent(eventType);
            cwp.setSelectedMap(map.getName());
            player.sendMessage(Util.formatMsg("&6&lMap selected!" + " &6Event&8: &a" + eventType.toString().toLowerCase() + " &6Map&8: &a" + map.getName()));
            return;
        }


        if (args[0].equalsIgnoreCase("list")) {
            player.sendMessage(CWUtil.integrateColor("&8===== &4&lAll event maps &8====="));
            for (EventType eventType : EventType.values()) {
                player.sendMessage(CWUtil.integrateColor("&6&l" + CWUtil.capitalize(eventType.toString().toLowerCase().replace("_", "")) + "&8&l: &7" + CWUtil.implode(events.mm.getMapNames(eventType), "&8, &7")));
            }
            return;
        }


        if (args[0].equalsIgnoreCase("info")) {

            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (!events.sm.hasSession(map.getType(), map.getName())) {
                player.sendMessage(Util.formatMsg("&cNo active session! &7This map currently has no active session!"));
                return;
            }
            GameSession session = events.sm.getSession(map.getType(), map.getName());

            player.sendMessage(CWUtil.integrateColor("&8===== &4&lGame Information &8====="));
            player.sendMessage(CWUtil.integrateColor("&6Event&8: &7" + map.getType().toString().toLowerCase().replace("_"," ")));
            player.sendMessage(CWUtil.integrateColor("&6Map&8: &7" + map.getName()));
            player.sendMessage(CWUtil.integrateColor("&6Session ID&8: &7" + session.getID()));
            player.sendMessage(CWUtil.integrateColor("&6State&8: &7" + session.getState().getColor() + session.getState()));
            player.sendMessage(CWUtil.integrateColor("&8--- &7&lPlayers &8[&a" + session.getAllPlayers(false).size() + "&7/&2" + map.getMaxPlayers() + " &d+" + session.getSpecPlayerSize() + " &8] &7---"));
            List<String> players = new ArrayList<String>();
            List<String> spectators = new ArrayList<String>();
            for (UUID uuid : session.getAllPlayers(false)) {
                OfflinePlayer oplayer = events.getServer().getOfflinePlayer(uuid);
                String name = oplayer.getName();
                if (oplayer.isOnline()) {
                    name = ((Player)oplayer).getDisplayName();
                }
                if (oplayer.isOnline()) {
                    players.add("&8[&a&lON&8] &7" + name);
                } else {
                    players.add("&8[&4&lOFF&8] &7" + name);
                }
            }

            if (players.size() > 0) {
                player.sendMessage(CWUtil.integrateColor("&6Players&8: &7" + CWUtil.implode(players, "&8, &7")));
            }
            if (spectators.size() > 0) {
                player.sendMessage(CWUtil.integrateColor("&6Spectators&8: &7" + CWUtil.implode(spectators, "&8, &7")));
            }
            return;
        }


        if (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("close")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (events.sm.hasSession(map.getType(), map.getName())) {
                player.sendMessage(Util.formatMsg("&cActive session! &7This map currently has an active session so it can't be closed!"));
                //TODO: Allow closing active sessions but end it first etc.
                return;
            }

            if (map.isClosed()) {
                player.sendMessage(Util.formatMsg("&6&lMap &a&lopened&6&l!"));
                map.setClosed(false);
            } else {
                player.sendMessage(Util.formatMsg("&6&lMap &4&lclosed&6&l!"));
                map.setClosed(true);
            }
            Util.updateSign(map, null);
            return;
        }


        if (args[0].equalsIgnoreCase("reset")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (!events.sm.hasSession(map.getType(), map.getName())) {
                player.sendMessage(Util.formatMsg("&cNo active session! &7This map has no action session that can be reset!"));
                return;
            }
            GameSession session = events.sm.getSession(map.getType(), map.getName());

            player.sendMessage(Util.formatMsg("&6&lSession reset!"));
            session.broadcast("&cThe game has been reset by a staff member!", true);
            session.reset();
            return;
        }


        if (args[0].equalsIgnoreCase("start")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (!events.sm.hasSession(map.getType(), map.getName())) {
                player.sendMessage(Util.formatMsg("&cNo active session! &7This map has no action session that can be started!"));
                return;
            }
            GameSession session = events.sm.getSession(map.getType(), map.getName());

            player.sendMessage(Util.formatMsg("&6&lSession started!"));
            session.broadcast("&cThe game has been started by a staff member!", true);
            session.getTimer().setCountdownTimeRemaining(10);
            return;
        }


        if (args[0].equalsIgnoreCase("end")) {
            if (cwp.getSelectedEvent() == null || cwp.getSelectedMap() == null || events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap()) == null) {
                player.sendMessage(Util.formatMsg("&cNo map selected! &7Select one using &c/setup select&7!"));
                return;
            }
            EventMap map = events.mm.getMap(cwp.getSelectedEvent(), cwp.getSelectedMap());

            if (!events.sm.hasSession(map.getType(), map.getName())) {
                player.sendMessage(Util.formatMsg("&cNo active session! &7This map has no action session that can be ended!"));
                return;
            }
            GameSession session = events.sm.getSession(map.getType(), map.getName());

            player.sendMessage(Util.formatMsg("&6&lSession ended!"));
            session.broadcast("&cThe game has been ended by a staff member!", true);
            session.forceEnd();
            return;
        }


        showHelp(player);
    }


    private void showHelp(Player player) {
        player.sendMessage(CWUtil.integrateColor("&8===== &4&lEvent Commands &8====="));
        player.sendMessage(CWUtil.integrateColor("&6/event select {event} {mapname} &8- &7Select the given event/map."));
        player.sendMessage(CWUtil.integrateColor("&6/event list &8- &7List all maps per event."));
        player.sendMessage(CWUtil.integrateColor("&6/event info &8- &7Show all info about the event."));
        player.sendMessage(CWUtil.integrateColor("&8--- &7&lState Management &8---"));
        player.sendMessage(CWUtil.integrateColor("&6/event open/close &8- &7Open and close the event/map."));
        player.sendMessage(CWUtil.integrateColor("&6/event start &8- &7Force start the 10s countdown"));
        player.sendMessage(CWUtil.integrateColor("&6/event end &8- &7Force end the game."));
        player.sendMessage(CWUtil.integrateColor("&6/event reset &8- &7Reset/remove the game."));
        player.sendMessage(CWUtil.integrateColor("&7&oMost of the above commands work for the selected map!"));
        player.sendMessage(CWUtil.integrateColor("&7&oIf you use a command during a game it will select that session!"));
    }
}
