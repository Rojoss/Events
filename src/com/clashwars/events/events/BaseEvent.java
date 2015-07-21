package com.clashwars.events.events;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.events.Events;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

/**
 * Base class for the event classes.
 * Each event has a class like Race, Koh etc which extends from this class.
 * All base things are defined here like each event needs a sign and map setup option.
 */
public class BaseEvent implements Listener {

    protected Events events;

    protected List<SetupOption> setupOptions = new ArrayList<SetupOption>(); /** Event specific setup options should be added in each event class. */
    protected List<Modifier> modifiers = new ArrayList<Modifier>();
    protected List<Ability> abilities = new ArrayList<Ability>();

    public BaseEvent() {
        events = Events.inst();

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


    public List<Ability> getAbilities() {
        return abilities;
    }



    //Util/helper methods

    protected CWPlayer getCWPlayer(Player player) {
        return events.pm.getPlayer(player);
    }

    protected boolean isSpectating(Player player) {
        return isSpectating(getCWPlayer(player));
    }

    protected boolean isSpectating(CWPlayer cwp) {
        return cwp.isSpectating();
    }

    protected boolean inSession(Player player) {
        return inSession(getCWPlayer(player));
    }

    protected boolean inSession(CWPlayer cwp) {
        if (!cwp.inSession()) {
            return false;
        }
        return true;
    }

    protected GameSession getSession(Player player) {
        return getSession(getCWPlayer(player));
    }

    protected GameSession getSession(CWPlayer cwp) {
        return cwp.getSession();
    }


    protected  boolean validateSession(Player player, EventType type, boolean allowSpectators) {
        return validateSession(player, type, allowSpectators, (List<State>)null);
    }

    protected  boolean validateSession(Player player, EventType type, boolean allowSpectators, State allowedState) {
        return validateSession(player, type, allowSpectators, new State[] {allowedState});
    }

    protected boolean validateSession(Player player, EventType type, boolean allowSpectators, State[] allowedStates) {
        return validateSession(player, type, allowSpectators, Arrays.asList(allowedStates));
    }

    protected boolean validateSession(Player player, EventType type, boolean allowSpectators, List<State> allowedStates) {
        CWPlayer cwp = getCWPlayer(player);
        if (!inSession(cwp)) {
            return false;
        }

        if (!allowSpectators && isSpectating(cwp)) {
            return false;
        }

        GameSession session = getSession(cwp);
        if (session == null) {
            return false;
        }

        if (type != null && type != session.getType()) {
            return false;
        }

        if (allowedStates != null && !allowedStates.contains(session.getState())) {
            return false;
        }

        return true;
    }



    //Allowed actions for all games



}
