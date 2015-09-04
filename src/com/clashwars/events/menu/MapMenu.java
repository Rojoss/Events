package com.clashwars.events.menu;

import com.clashwars.cwcore.ItemMenu;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.State;
import com.clashwars.events.maps.EventMap;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MapMenu implements Listener {

    private Events events;
    public ItemMenu menu;

    public HashMap<UUID, EventType> openMenus = new HashMap<UUID, EventType>();

    public MapMenu(Events events) {
        this.events = events;
        menu = new ItemMenu("map_menu", 18, CWUtil.integrateColor("&4&lJoin A Game"));
    }

    public void showMenu(final Player player, EventType eventType) {
        openMenus.put(player.getUniqueId(), eventType);
        player.closeInventory();
        menu.show(player);
        updateMenu(player.getUniqueId());
    }

    public void updateMenu(EventType eventType) {
        if (events.mm == null || events.sm == null) {
            return;
        }
        for (UUID uuid : openMenus.keySet()) {
            updateMenu(uuid);
        }
    }

    private void updateMenu(UUID uuid) {
        Player player = events.getServer().getPlayer(uuid);
        if (player == null) {
            return;
        }
        if (!openMenus.containsKey(uuid)) {
            player.closeInventory();
            return;
        }
        EventType eventType = openMenus.get(uuid);

        int slot = 0;
        List<EventMap> maps = events.mm.getMaps(eventType);
        for (EventMap map : maps) {
            GameSession session = null;
            if (events.sm.hasSession(eventType, map.getName())) {
                session = events.sm.getSession(eventType, map.getName());
            }

            CWItem item = new CWItem(Material.INK_SACK);
            if (session == null) {
                item.setName("&7&l" + map.getName());
                if (map.isClosed() || !map.validateMap().isEmpty()) {
                    item.setDurability((byte)1);
                    item.addLore("&6&lSTATE&8: " + State.CLOSED.getSignText());
                } else {
                    item.setDurability((byte)10);
                    item.addLore("&6&lSTATE&8: " + State.OPENED.getSignText());
                }
                item.addLore("&6&lPLAYERS&8: &70&8/&7" + map.getMaxPlayers());
            } else {
                item.setName("&a&l" + map.getName());
                item.setDurability(session.getState().getDyeColor());
                item.setAmount(Math.max(session.getPlayerCount(false), 1));
                item.addLore("&6&lSTATE&8: " + session.getState().getSignText());
                if (session.getSpecPlayerSize() > 0) {
                    item.addLore("&6&lPLAYERS&8: &a&l" + session.getPlayerCount(false) + "&8/&2" + map.getMaxPlayers() + " &d+" + session.getSpecPlayerSize());
                } else {
                    item.addLore("&6&lPLAYERS&8: &a&l" + session.getPlayerCount(false) + "&8/&2" + map.getMaxPlayers());
                }

                List<UUID> allPlayers = session.getAllPlayers(true);
                List<String> players = new ArrayList<String>();
                for (UUID p : allPlayers) {
                    if (events.pm.getPlayer(p).isSpectating()) {
                        players.add("&d&o" + CWUtil.getName(p));
                    } else {
                        players.add("&7&o" + CWUtil.getName(p));
                    }
                }
                item.addLore(CWUtil.implode(players, "&8&o, ", " &8&o& "));
                item.makeGlowing();
            }

            item.hideTooltips();
            menu.setSlot(item, slot, player);
            slot++;
        }
        for (;slot < menu.getSize(); slot++) {
            menu.setSlot(new CWItem(Material.AIR), slot, player);
        }
    }

    @EventHandler
    private void menuClick(final ItemMenu.ItemMenuClickEvent event) {
        if (menu == null) {
            return;
        }
        if (!event.getItemMenu().getName().equals(menu.getName())) {
            return;
        }
        if (event.getItemMenu().getID() != menu.getID()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        CWItem item = new CWItem(event.getCurrentItem());

        event.setCancelled(true);
        if (event.getRawSlot() >= menu.getSize()) {
            return;
        }


    }

    @EventHandler
    private void menuClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        final Player player = (Player)event.getPlayer();
        if (!inv.getTitle().equals(menu.getTitle()) || inv.getSize() != menu.getSize() || !inv.getHolder().equals(player)) {
            return;
        }
        if (!openMenus.containsKey(player.getUniqueId())) {
            return;
        }
        openMenus.remove(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                events.gameMenu.showMenu(player);
            }
        }.runTaskLater(events, 1);
    }

}
