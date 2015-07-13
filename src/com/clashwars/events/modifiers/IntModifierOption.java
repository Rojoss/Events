package com.clashwars.events.modifiers;

public class IntModifierOption extends ModifierOption {

    public IntModifierOption(int ID, String name, int value, float chance) {
        super(ID, name, Integer.toString(value), chance);
    }

    public int getInt() {
        return Integer.valueOf(value);
    }
}
