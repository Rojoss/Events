package com.clashwars.events.modifiers;

public class ModifierOption {

    public int ID;
    public String name;
    public float weight;

    public ModifierOption(int ID, String name, float chance) {
        this.ID = ID;
        this.name = name;
        this.weight = chance;
    }

}
