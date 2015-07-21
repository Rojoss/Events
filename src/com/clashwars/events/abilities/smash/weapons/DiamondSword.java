package com.clashwars.events.abilities.smash.weapons;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.abilities.BaseAbility;
import org.bukkit.Material;

public class DiamondSword extends BaseAbility {

    public DiamondSword() {
        super();
        ability = Ability.DIAMOND_SWORD;
        displayName = "&b&lDiamond Sword";
        description = new String[] {"Deals &a&l7 &7&odamage when attacking."};
        castItem = new CWItem(Material.DIAMOND_SWORD, 1, (short)1551, displayName);
    }
}
