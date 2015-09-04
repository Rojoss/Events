package com.clashwars.events.util;

import com.clashwars.cwcore.helpers.CWItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public enum Equipment {
    SPECTATOR(new CWItem[] {
            new CWItem(Material.SKULL_ITEM).setSlot(0).hideTooltips().setSkullOwner("cy1337").setName("&6&lCycle").setLore(new String[] {
                    "&bRight click &7to cycle between online players.", "&aLeft click &7to teleport to the selected player."
            }),
            new CWItem(Material.SKULL_ITEM).setSlot(1).hideTooltips().setSkullOwner("cy1337").setName("&6&lSelect A Player").setLore(new String[] {
                    "&bRight click &7to open the player selection menu.", "&aLeft click &7to teleport to the selected player."
            }),
            new CWItem(Material.REDSTONE_BLOCK).setSlot(8).hideTooltips().setName("&4&l/LEAVE").setLore(new String[] {
                    "&7Stop spectating the current game.", "&7You will be teleported back to the lobby!"
            })
    }),
    LOBBY(new CWItem[]{
            new CWItem(Material.EYE_OF_ENDER).setSlot(0).hideTooltips().setName("&6&l/PLAY").setLore(new String[] {
                    "&7Select a game and map to play!", "&7You can also join games by using the signs in the lobby!"
            }),
            new CWItem(Material.HOPPER).setSlot(2).hideTooltips().setName("&5&l/MODIFIERS").setLore(new String[] {"&7Customize your favourite game modifiers!"}),
            new CWItem(Material.DIODE).setSlot(4).hideTooltips().setName("&d&l/SETTINGS").setLore(new String[] {"&7Modify your game settings!"}),
            new CWItem(Material.NETHER_STAR).setSlot(6).hideTooltips().setName("&a&l/STATS").setLore(new String[] {"&7Display statistics!"}),
            new CWItem(Material.NAME_TAG).setSlot(8).hideTooltips().setName("&b&l/VOTESHOP").setLore(new String[] {"&7Purchase rewards with your vote tokens!"}),
    });

    private CWItem[] items;

    Equipment(CWItem[] items) {
        this.items = items;
    }

    /** Get the array of all CWItems */
    public CWItem[] getItems() {
        return items;
    }

    /** Get the list of all CWItems */
    public List<CWItem> getItemList() {
        return Arrays.asList(items);
    }

    /** Equip the items for the specified player. It will put armor in the armor slots */
    public void equip(Player player) {
        Util.equipItems(player, getItemList());
    }
}
