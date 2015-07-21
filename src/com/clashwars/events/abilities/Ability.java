package com.clashwars.events.abilities;

public enum Ability {
    NULL(null, "");

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
