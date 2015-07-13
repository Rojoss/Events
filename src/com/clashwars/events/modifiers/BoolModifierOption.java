package com.clashwars.events.modifiers;

public class BoolModifierOption extends ModifierOption {

    public BoolModifierOption(int ID, String name, boolean value, float chance) {
        super(ID, name, Boolean.toString(value), chance);
    }

    public boolean getBool() {
        return Boolean.valueOf(value);
    }
}
