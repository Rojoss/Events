package com.clashwars.events.modifiers;

import com.clashwars.cwcore.helpers.CWItem;

public class ItemModifierOption extends ModifierOption {

    public ItemModifierOption(int ID, String name, String itemString, float chance) {
        super(ID, name, itemString, chance);
    }

    public CWItem getItem() {
        return new CWItem(value);
    }
}
