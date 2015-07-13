package com.clashwars.events.modifiers;

import com.clashwars.cwcore.helpers.CWItem;
import org.bukkit.Material;

public class ModifierOption {

    public int ID;
    public String name;
    public String value;
    public float weight;

    public ModifierOption(int ID, String name, String value, float chance) {
        this.ID = ID;
        this.name = name;
        this.value = value;
        this.weight = chance;
    }

    public String getString() {
        return value;
    }

    public boolean getBoolean() {
        if (this instanceof BoolModifierOption) {
            return ((BoolModifierOption)this).getBool();
        }
        return false;
    }

    public int getInteger() {
        if (this instanceof IntModifierOption) {
            return ((IntModifierOption)this).getInt();
        }
        return 0;
    }

    public CWItem getCWItem() {
        if (this instanceof ItemModifierOption) {
            return ((ItemModifierOption)this).getItem();
        }
        return new CWItem(Material.STONE);
    }
}
