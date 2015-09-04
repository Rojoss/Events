package com.clashwars.events.menu;

import com.clashwars.cwcore.ItemMenu;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.events.BaseEvent;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.maps.EventMap;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;

public class GameMenu implements Listener {

    private Events events;
    public ItemMenu menu;


    public GameMenu(Events events) {
        this.events = events;
        menu = new ItemMenu("game_menu", 45, CWUtil.integrateColor("&4&lJoin A Game"));

        Integer[] dividers = new Integer[] {9,10,11,12,13,14,15,16,17};
        for (int dividerSlot : dividers) {
            menu.setSlot(new CWItem(Material.STAINED_GLASS_PANE, 1, (byte)15).hideTooltips().setName("&8-----").setLore(new String[] {
                    "&a^ ^ ^ ^ ^ ^ ^", "&a&lA&6&lR&c&lC&9&lA&5&lD&b&lE", "&7- - - - - - -", "&6&lEVENTS", "&av v v v v v v"}), dividerSlot, null);
        }

        dividers = new Integer[] {19,28,37};
        for (int dividerSlot : dividers) {
            menu.setSlot(new CWItem(Material.STAINED_GLASS_PANE, 1, (byte)15).hideTooltips().setName("&8-----").setLore(new String[] {
                    "&a< < < < < < <", "&d&lSERVERS", "&7- - - - - - -", "&6&lEVENTS", "&a> > > > > > >"}), dividerSlot, null);
        }
    }

    public void showMenu(final Player player) {
        player.closeInventory();
        menu.show(player);
        updateMenu();
    }

    public void updateMenu() {
        if (events.mm == null || events.sm == null) {
            return;
        }
        for (EventType eventType : EventType.values()) {
            BaseEvent eventClass = eventType.getEventClass();
            CWItem item = eventClass.getMenuItem().clone();
            item.setName(eventClass.getDisplayName());

            int players = 0;
            int spectators = 0;
            List<EventMap> maps = events.mm.getMaps(eventType);
            for (EventMap map : maps) {
                if (events.sm.hasSession(eventType, map.getName())) {
                    GameSession session = events.sm.getSession(eventType, map.getName());
                    players += session.getPlayerCount(false);
                    spectators += session.getSpecPlayerSize();
                }
            }

            if (players > 0) {
                item.addLore("&6&lPLAYERS&8: &a&l" + players + " &8(&5&l+&d&l" + spectators + "&8)");
                item.makeGlowing();
            } else {
                item.addLore("&cNobody is playing this right now!");
            }
            menu.setSlot(item, eventClass.getMenuSlot(), null);
        }
        //TODO: DvZ and Survival
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

        for (EventType eventType : EventType.values()) {
            if (eventType.getEventClass().getMenuSlot() == event.getRawSlot()) {
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 2);
                events.mapMenu.showMenu(player, eventType);
                return;
            }
        }
    }

}
