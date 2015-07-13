package com.clashwars.events.listeners;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.events.DelayedPlayerInteractEvent;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.JoinType;
import com.clashwars.events.maps.EventMap;
import com.clashwars.events.util.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import java.util.ArrayList;
import java.util.List;

public class MainListener implements Listener {

    Events events;

    public MainListener(Events events) {
        this.events = events;
    }

    @EventHandler
    private void interact(DelayedPlayerInteractEvent event) {
        Player player = event.getPlayer();

        //Clicking on signs
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if ((block.getType() == Material.SIGN || block.getType() == Material.WALL_SIGN) && block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                List<String> lines = new ArrayList<String>();
                lines.add(CWUtil.removeColour(sign.getLine(0)));
                lines.add(CWUtil.removeColour(sign.getLine(1)));
                lines.add(CWUtil.removeColour(sign.getLine(2)));
                lines.add(CWUtil.removeColour(sign.getLine(3)));


                //Event signs
                for (EventType eventType : EventType.values()) {
                    if (lines.get(0).equalsIgnoreCase("&5[" + eventType.toString() + "]")) {
                        EventMap map = events.mm.getMap(eventType, CWUtil.stripAllColor(lines.get(1)));
                        if (map == null) {
                            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                            player.sendMessage(Util.formatMsg("&cThis map no longer exists. &4&l:("));
                            return;
                        }

                        if (map.getBlock("sign") == null || !map.getBlock("sign").getLocation().equals(block.getLocation())) {
                            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                            player.sendMessage(Util.formatMsg("&cThis sign has been moved to another location!"));
                            block.setType(Material.AIR);
                            return;
                        }

                        if (map.isClosed()) {
                            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                            player.sendMessage(Util.formatMsg("&cThis map is not playable right now."));
                            return;
                        }

                        GameSession session = events.sm.getSession(eventType, map.getName());
                        if (session == null) {
                            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                            player.sendMessage(Util.formatMsg("&cFailed to create a session for this map."));
                            player.sendMessage(Util.formatMsg("&cPlease try again later..."));
                            return;
                        }

                        JoinType joinType = session.canJoin(player);
                        if (joinType == JoinType.QUEUE) {
                            player.playSound(player.getLocation(), Sound.WOOD_CLICK, 1, 2);
                            player.sendMessage(Util.formatMsg("&6&lYou have &a&lqueued up &6&lfor the game!"));
                            player.sendMessage(CWUtil.integrateColor("&7You will be teleported to the game when it's about to start!"));
                            session.join(player);
                            return;
                        }

                        if (joinType == JoinType.JOIN) {
                            player.playSound(player.getLocation(), Sound.WOOD_CLICK, 1, 2);
                            player.sendMessage(Util.formatMsg("&6&lYou have &a&ljoined &6&lthe game!"));
                            //TODO: Send game modifiers to player.
                            session.join(player);
                            return;
                        }

                        if (joinType == JoinType.JOIN_BACK) {
                            player.playSound(player.getLocation(), Sound.WOOD_CLICK, 1, 2);
                            player.sendMessage(Util.formatMsg("&6&lYou have &a&ljoined &6&lyour &a&lprevious game &6&lagain!"));
                            session.join(player);
                            return;
                        }

                        if (joinType == JoinType.SPECTATE) {
                            player.playSound(player.getLocation(), Sound.WOOD_CLICK, 1, 2);
                            player.sendMessage(Util.formatMsg("&6&lYou are now &a&lspectating &6&lthe game!"));
                            player.sendMessage(CWUtil.integrateColor("&7Use the items in your hotbar to navigate around and to go back!"));
                            session.join(player);
                            return;
                        }

                        player.playSound(player.getLocation(), Sound.ITEM_BREAK, 0.5f, 2);
                        if (joinType == JoinType.CLOSED) {
                            player.sendMessage(Util.formatMsg("&cThis map is not playable right now."));
                        } else if (joinType == JoinType.ENDED) {
                            player.sendMessage(Util.formatMsg("&cThis game just ended. &7It will reset soon!"));
                        } else if (joinType == JoinType.INVALID) {
                            player.sendMessage(Util.formatMsg("&cThis map no longer exists. &4&l:("));
                        } else if (joinType == JoinType.RESETTING) {
                            player.sendMessage(Util.formatMsg("&cThis map is resetting. &7You'll be able to join soon!"));
                        } else if (joinType == JoinType.FULL) {
                            player.sendMessage(Util.formatMsg("&cThis map is full!"));
                        } else if (joinType == JoinType.IN_GAME) {
                            player.sendMessage(Util.formatMsg("&cYou're already in a game! &7If you'd like to leave your current game use &c/leave&7!"));
                        }
                        return;
                    }
                }

            }
        }
    }

}
