package com.clashwars.events.abilities.smash.weapons;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.abilities.BaseAbility;
import org.bukkit.Material;

public class StoneSword extends BaseAbility {

    public StoneSword() {
        super();
        ability = Ability.STONE_SWORD;
        displayName = "&8&lStone Sword";
        description = new String[] {"Deals &a&l5 &7&odamage when attacking."};
        castItem = new CWItem(Material.STONE_SWORD, 1, (short)125, displayName);
    }
}
