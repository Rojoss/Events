package com.clashwars.events.abilities;

import com.clashwars.cwcore.CooldownManager;
import com.clashwars.cwcore.events.DelayedPlayerInteractEvent;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.State;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseAbility {

    protected Events events = Events.inst();
    protected Ability ability;

    protected String displayName = "&7Unknown";
    protected CWItem castItem = null;
    protected List<State> allowedStates = new ArrayList<State>(Arrays.asList(new State[] {State.STARTED}));
    protected List<Action> castActions = new ArrayList<Action>(Arrays.asList(new Action[]{Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}));
    protected int cooldown = 0;

    protected String[] description = new String[] {};
    protected String[] usage = new String[] {};


    public BaseAbility() {
        //--
    }

    /** Set the Ability enum value that the ability belongs too. */
    public void setAbility(Ability ability) {
        this.ability = ability;
    }

    /** Get a list of all events that use this ability */
    public List<EventType> getEvents() {
        if (ability == null) {
            return null;
        }
        List<EventType> events = new ArrayList<EventType>();
        for (EventType event : EventType.values()) {
            if (event.getEventClass().getAbilities().contains(ability)) {
                events.add(event);
            }
        }
        return events;
    }

    /** Get the cast item for the ability if it has one. With the description and usage as lore. */
    public CWItem getCastItem() {
        if (castItem != null) {
            castItem.setLore(new String[]{}).setLore(CWUtil.concat(formatDesc(getDesc(), "&aDesc&8: ", "&7&o"), formatDesc(getUsage(), "&aUsage&8: ", "&7&o")));
            castItem.setName(getDisplayName());
            castItem.replaceLoreNewLines();
        }
        return castItem;
    }

    /** Get the cooldown time the ability has in total */
    public int getCooldown() {
        return cooldown;
    }

    /** Set the cooldown time the ability has in total */
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    /** Returns true if the total cooldown time is set and greater than 0 */
    public boolean hasCooldown() {
        return cooldown > 0;
    }

    /** Get the displayname for the ability */
    public String getDisplayName() {
        return displayName;
    }

    /** Set the displayname for the ability */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** Get the description for the ability if it has one. If not it will return a no description string. */
    public String[] getDesc() {
        if (description == null || description.length <= 0) {
            return new String[] {"&8&oNo description available."};
        } else {
            return description;
        }
    }

    /** Set the description for the ability.*/
    public void setDesc(String[] desc) {
        this.description = desc;
    }

    /** Get the usage for the ability if it has one.
     * If not it will return a default string with 'Click while holding this item to use it'.
     * And if it has no cast item the usage will be 'No info available.'
     */
    public String[] getUsage() {
        if (usage == null || usage.length <= 0) {
            if (castItem != null) {
                return new String[] {"&7&oClick while holding this item to use it."};
            } else {
                return new String[] {"&8&oNo info available."};
            }
        }
        return usage;
    }

    /** Set the usage description for the ability. */
    public void setUsage(String[] usage) {
        this.usage = usage;
    }


    /** Format the description and usage lines by adding a prefix like Desc: and Usage: And by coloring all lines. */
    public String[] formatDesc(String[] desc, String prefix, String lineColor) {
        for (int i = 0; i < desc.length; i++) {
            if (i == 0 && !prefix.isEmpty()) {
                desc[i] = prefix + lineColor + desc[i];
            } else {
                desc[i] = lineColor + desc[i];
            }
        }
        return desc;
    }


    /** This method should be overwritten by each ability to do the custom ability implementation.
     * If the ability doesn't have a cast item then this won't be called.
     */
    public void castAbility(Player player, Location triggerLoc) {
        player.sendMessage(Util.formatMsg("&4Error&8: &cMissing ability implementation."));
    }

    /** Check if the given player can cast this ability.
     *  It will check if the player is inside a GameSession and if the session has this ability.
     *  It will also make sure that the current session game State is specified in the allowedStates list.
     */
    public boolean canCast(Player player) {
        CWPlayer cwp = events.pm.getPlayer(player);

        if (cwp.getSession() == null) {
            return false;
        }
        GameSession session = cwp.getSession();

        if (allowedStates.contains(session.getState())) {
            if (session.getState() == State.COUNTDOWN || session.getState() == State.OPENED) {
                CWUtil.sendActionBar(player, "&4&l", "&cThe game hasn't started yet!");
            } else if (session.getState() == State.STARTED) {
                CWUtil.sendActionBar(player, "&4&l", "&cThe game has already started!");
            } else if (session.getState() == State.ON_HOLD) {
                CWUtil.sendActionBar(player, "&4&l", "&cThe game is currently on hold!");
            } else if (session.getState() == State.ENDED || session.getState() == State.RESETTING ) {
                CWUtil.sendActionBar(player, "&4&l", "&cThe game has ended!");
            } else if (session.getState() == State.CLOSED) {
                CWUtil.sendActionBar(player, "&4&l", "&cThe game is closed!");
            }
        }

        if (!getEvents().contains(cwp.getSession().getType())) {
            return false;
        }
        return true;
    }

    /** Check if the ability is on cooldown for the specified player */
    public boolean onCooldown(Player player) {
        return onCooldown(player, "", getCooldown(), 0);
    }

    /** Check if the ability is on cooldown for the specified player. */
    public boolean onCooldown(Player player, String extraPrefix) {
        return onCooldown(player, extraPrefix, getCooldown(), 0);
    }

    /** Check if the ability is on cooldown for the specified player. */
    public boolean onCooldown(Player player, String extraPrefix, int cooldownTime) {
        return onCooldown(player, extraPrefix, cooldownTime, 0);
    }

    /** Check if the ability is on cooldown for the specified player. */
    public boolean onCooldown(Player player, float cooldownReduction) {
        return onCooldown(player, "", getCooldown(), cooldownReduction);
    }

    /** Check if the ability is on cooldown for the specified player. */
    public boolean onCooldown(Player player, String extraPrefix, int cooldownTime, float cooldownReduction) {
        CWPlayer cwp = events.pm.getPlayer(player);
        GameSession session = cwp.getSession();

        cooldownReduction = 1 - Math.max(Math.min(cooldownReduction, 1), 0);

        if (hasCooldown() && session != null) {
            String tag = session.getType().toString().toLowerCase() +  "-" + ability.toString().toLowerCase();
            if (extraPrefix != null && !extraPrefix.isEmpty()) {
                tag += "-" + extraPrefix;
            }
            CooldownManager.Cooldown cd = cwp.getCDM().getCooldown(tag);
            if (cd == null) {
                cwp.getCDM().createCooldown(tag, Math.round(cooldownTime * cooldownReduction));
                return false;
            }
            if (!cd.onCooldown()) {
                cd.setTime(Math.round(cooldownTime * cooldownReduction));
                return false;
            }

            if (cd.getTimeLeft() >= 60000) {
                CWUtil.sendActionBar(player, CWUtil.integrateColor(getDisplayName() + " &4&l> &7" + CWUtil.formatTime(cd.getTimeLeft(), "&c%M&4:&c%S&4m")));
            } else {
                CWUtil.sendActionBar(player, CWUtil.integrateColor(getDisplayName() + " &4&l> &7" + CWUtil.formatTime(cd.getTimeLeft(), "&c%S&4.&c%%%&4s")));
            }
            return true;
        }
        return false;
    }

    /** Check if the given item is the same as the cast item of this ability */
    public boolean isCastItem(ItemStack item) {
        CWItem castI = getCastItem();
        if (item == null || castI == null) {
            return false;
        }
        if (item.getType() != castI.getType()) {
            return false;
        }
        if (castI.hasItemMeta() && !item.hasItemMeta()) {
            return false;
        }
        if (castI.hasItemMeta() && item.hasItemMeta() && castItem.getItemMeta().hasDisplayName() && item.getItemMeta().hasDisplayName()) {
            if (!castI.getItemMeta().getDisplayName().equalsIgnoreCase(item.getItemMeta().getDisplayName())) {
                return false;
            }
        }
        return true;
    }

    /** This method should be called from the ability class with a DelayedPlayerInteractEvent handler
     * When the ability can be cased and it's the proper cast item etc the castAbility method will be called.
     */
    protected void interact(DelayedPlayerInteractEvent event) {
        //This is only for abilities with cast items.
        if (castItem == null) {
            return;
        }

        //Compare items.
        if (!isCastItem(event.getItem())) {
            return;
        }

        //Compare the click action with allowed actions.
        if (castActions == null || !castActions.contains(event.getAction())) {
            return;
        }

        //Make sure we can cast it. (In a event and propper event state)
        Player player = event.getPlayer();
        if (!canCast(player)) {
            return;
        }

        event.setCancelled(true);
        player.updateInventory();

        //CAST! (we need to get the actual ability class when casting and not this BaseAbility cast method)
        Location loc = player.getLocation();
        if (event.getClickedBlock() != null) {
            loc = event.getClickedBlock().getLocation();
        }

        ability.getAbilityClass().castAbility(player, loc);
    }

    /** This method can be overwritten to do something when the cast item is given */
    public void onCastItemGiven(Player player) {
        //--
    }
}
