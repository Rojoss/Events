package com.clashwars.events.abilities.smash.weapons;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.abilities.BaseAbility;
import org.bukkit.Material;

public class WoodSword extends BaseAbility {

    public WoodSword() {
        super();
        ability = Ability.WOOD_SWORD;
        displayName = "&e&lWooden Sword";
        description = new String[] {"Deals &a&l4 &7&odamage when attacking."};
        castItem = new CWItem(Material.WOOD_SWORD, 1, (short)54, displayName);
    }
}
