package com.clashwars.events.events;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for the event classes.
 * Each event has a class like Spleef, Koh etc which extends from this class.
 * All base things are defined here like each event needs a sign and map setup option.
 */
public class BaseEvent implements Listener {

    protected List<SetupOption> setupOptions = new ArrayList<SetupOption>(); /** Event specific setup options should be added in each event class. */
    protected List<Modifier> modifiers = new ArrayList<Modifier>();

    public BaseEvent() {
        setupOptions.add(new SetupOption(SetupType.CUBOID, "map", "A cuboid that contains the entire map."));
        setupOptions.add(new SetupOption(SetupType.BLOCK_LOC, "sign", "The sign to join the map."));
        setupOptions.add(new SetupOption(SetupType.MULTI_LOC, "spawn", "Location(s) where players (re)spawn."));
    }


    /** Get a list of SetupOption(s) that need to be specified for map validation */
    public List<SetupOption> getSetupOptions() {
        return setupOptions;
    }

    /** Check if the event needs the specified setup option. */
    public boolean hasSetupOption(SetupType type, String name) {
        for (SetupOption option : setupOptions) {
            if (option.type == type && option.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /** Try and get a SetupOption from the given name. If it doesn't have this option it will return null. */
    public SetupOption getSetupOption(String name) {
        for (SetupOption option : setupOptions) {
            if (option.name.equalsIgnoreCase(name)) {
                return option;
            }
        }
        return null;
    }



    /**
     * Get a list of CWItem's with all equipment for the specified session.
     * If no session is specified should return all items.
     * If the session is specified the equipment can be based on the game modifiers.
     */
    public List<CWItem> getEquipment(GameSession session) {
        return new ArrayList<CWItem>();
    }



    /** Get a list of all modifiers for the event */
    public List<Modifier> getModifiers() {
        return modifiers;
    }

    /** Returns true if the event has the specified modifier. */
    public boolean hasModifier(Modifier modifier) {
        if (modifiers.contains(modifier)) {
            return true;
        }
        return false;
    }

    /** Add all modifiers starting with the specified prefix like SPLEEF_ or KOH_ */
    protected void setupModifiers(String prefix) {
        for (Modifier modifier : Modifier.values()) {
            if (modifier.toString().startsWith(prefix)) {
                modifiers.add(modifier);
            }
        }
    }

}
