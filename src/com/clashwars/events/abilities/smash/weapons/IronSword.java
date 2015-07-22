package com.clashwars.events.abilities.smash.weapons;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.abilities.BaseAbility;
import org.bukkit.Material;

public class IronSword extends BaseAbility {

    public IronSword() {
        super();
        ability = Ability.IRON_SWORD;
        displayName = "&7&lIron Sword";
        description = new String[] {"Deals &a&l6 &7&odamage when attacking."};
        castItem = new CWItem(Material.IRON_SWORD, 1, (short)243, displayName);
    }
}
