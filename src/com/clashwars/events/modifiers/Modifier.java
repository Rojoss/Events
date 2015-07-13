package com.clashwars.events.modifiers;

import com.clashwars.events.events.EventType;

public enum Modifier {
    KOH_LIVES(new String[] {"&7The amount of lives you get.","&7When you're out of lives you're out of the game!"},
            new ModifierOption[] {new ModifierOption(0, "1 life", 0.1f), new ModifierOption(1, "3 lives", 0.4f), new ModifierOption(2, "5 lives", 0.3f), new ModifierOption(3, "8 lives", 0.2f)}),
    KOH_TEAMS(new String[] {"&7Solo or random generated teams.","&7The more people of a team stand on a hill...", "&7the faster it will be captured!"},
            new ModifierOption[] {new ModifierOption(0, "Teams", 0.3f), new ModifierOption(1, "No teams", 0.7f)}),
    KOH_HEALTH_POTIONS(new String[] {"&7The amount of health potions you get."},
            new ModifierOption[] {new ModifierOption(0, "No health pots", 0.1f), new ModifierOption(1, "3 Health pots", 0.4f), new ModifierOption(2, "5 Health pots", 0.3f), new ModifierOption(3, "8 Health pots", 0.2f)}),
    KOH_DAMAGE_POTIONS(new String[] {"&7The amount of damage potions you get."},
            new ModifierOption[] {new ModifierOption(0, "No damage pots", 0.2f), new ModifierOption(1, "1 Damage pot", 0.4f), new ModifierOption(2, "3 Damage pots", 0.3f), new ModifierOption(3, "5 Damage pots", 0.1f)}),
    KOH_KNOCKBACK(new String[] {"&7The amount of knockback on your sword and bow."},
            new ModifierOption[] {new ModifierOption(0, "No knockback", 0.2f), new ModifierOption(1, "Knockback 1", 0.4f), new ModifierOption(2, "Knockback 2", 0.3f), new ModifierOption(3, "Knockback 3", 0.1f)}),


    SMASH_LIVES(new String[] {"&7The amount of lives you get."},
            new ModifierOption[] {new ModifierOption(0, "1 life", 0.2f), new ModifierOption(1, "3 lives", 0.5f), new ModifierOption(2, "5 lives", 0.3f)}),
    SMASH_TEAMS(new String[] {"&7Solo or random generated teams."},
            new ModifierOption[] {new ModifierOption(0, "Teams", 0.3f), new ModifierOption(1, "No teams", 0.7f)}),
    SMASH_POWERUPS(new String[] {"&7The amount of powerups that will drop in the map."},
            new ModifierOption[] {new ModifierOption(0, "No powerups", 0.1f), new ModifierOption(1, "Few powerups", 0.5f), new ModifierOption(2, "A lot powerups", 0.4f)}),
    SMASH_DESTRUCTION(new String[] {"&7The amount of destruction that will happen", "&7when you smash players in walls and such."},
            new ModifierOption[] {new ModifierOption(0, "Little destruction", 0.5f), new ModifierOption(1, "A lot destruction", 0.5f)}),


    SPLEEF_TOOL(new String[] {"&7The tool you get to spleef others down.", "&7Arrows from bows will create holes.","&7And TnT can be thrown to create holes."},
            new ModifierOption[] {new ModifierOption(0, "Spade", 0.6f), new ModifierOption(1, "Bow", 0.2f), new ModifierOption(2, "TnT", 0.2f)}),
    SPLEEF_DECAY(new String[] {"&7When there is decay blocks will slowly decay.", "&7This means the blocks will start to disappear!"},
            new ModifierOption[] {new ModifierOption(0, "No decay", 0.6f), new ModifierOption(1, "Slow decay", 0.3f), new ModifierOption(2, "Fast decay", 0.1f)}),
    SPLEEF_TRAIL(new String[] {"&7When you have a trail blocks will", "&7disappear as you walk over them."},
            new ModifierOption[] {new ModifierOption(0, "No trail", 0.7f), new ModifierOption(1, "Full trail", 0.1f), new ModifierOption(2, "Half trail", 0.2f)}),
    SPLEEF_FLOOR(new String[] {"&7Blocks will randomly change to ice.", "&7Ice can't be destroyed but it can decay."},
            new ModifierOption[] {new ModifierOption(0, "Solid", 0.6f), new ModifierOption(1, "Few changes", 0.3f), new ModifierOption(2, "A lot changes", 0.1f)}),
    SPLEEF_SPEED(new String[] {"&7You'll speed or slowness."},
            new ModifierOption[] {new ModifierOption(0, "Normal speed", 0.6f), new ModifierOption(1, "Slow speed", 0.1f), new ModifierOption(2, "Fast speed", 0.2f), new ModifierOption(3, "Super fast speed", 0.1f)}),
    SPLEEF_BLINDNESS(new String[] {"&7When enabled you will be blinded", "&7when you get close to another player."},
            new ModifierOption[] {new ModifierOption(0, "No blindness", 0.9f), new ModifierOption(1, "Blindness", 0.1f)});


    private String[] description;
    private ModifierOption[] options;

    Modifier(String[] description, ModifierOption[] options) {
        this.description = description;
        this.options = options;
    }


    public String[] getDescription() {
        return description;
    }

    public ModifierOption[] getOptions() {
        return options;
    }
}
