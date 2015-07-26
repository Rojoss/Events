package com.clashwars.events.abilities.race;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.abilities.BaseAbility;
import org.bukkit.Material;

public class Rush extends BaseAbility {

    public Rush() {
        super();
        ability = Ability.RUSH;
        displayName = "&c&lRush";
        description = new String[] {"Receive a speed boost!"};
        usage = new String[] {"Right click the item!"};
        castItem = new CWItem(Material.SUGAR, 1, (short)0, displayName);
    }
}
