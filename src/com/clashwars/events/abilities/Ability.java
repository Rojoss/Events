package com.clashwars.events.abilities;

import com.clashwars.events.abilities.smash.*;
import com.clashwars.events.abilities.smash.food.*;
import com.clashwars.events.abilities.smash.weapons.DiamondSword;
import com.clashwars.events.abilities.smash.weapons.IronSword;
import com.clashwars.events.abilities.smash.weapons.StoneSword;
import com.clashwars.events.abilities.smash.weapons.WoodSword;

public enum Ability {
    COOKIE(new Cookie(), ""),
    BREAD(new Bread(), ""),
    STEAK(new Steak(), ""),
    GOLDEN_CARROT(new GoldenCarrot(), ""),
    GOLDEN_APPLE(new GoldenApple(), ""),
    WOOD_SWORD(new WoodSword(), ""),
    STONE_SWORD(new StoneSword(), ""),
    IRON_SWORD(new IronSword(), ""),
    DIAMOND_SWORD(new DiamondSword(), ""),
    TOSS(new Toss(), "{0} was tossed by {1}");

    private BaseAbility abilityClass;
    private String deathMessage;

    Ability(BaseAbility abilityClass, String deathMessage) {
        this.abilityClass = abilityClass;
        abilityClass.setAbility(this);
        this.deathMessage = deathMessage;
    }

    public BaseAbility getAbilityClass() {
        return abilityClass;
    }

    public String getDeathMsg() {
        return deathMessage;
    }

    public static Ability fromString(String name) {
        name = name.toLowerCase().replace("_","");
        for (Ability c : values()) {
            if (c.toString().toLowerCase().replace("_", "").equals(name)) {
                return c;
            }
        }
        return null;
    }
}
