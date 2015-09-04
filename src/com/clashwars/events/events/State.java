package com.clashwars.events.events;

import org.bukkit.ChatColor;

/**
 * The state of a GameSession.
 */
public enum State {
    CLOSED(ChatColor.DARK_RED, "&4&lCLOSED", (byte)1), /** When the map is closed/locked */
    OPENED(ChatColor.GREEN, "&a&lOPEN", (byte)10), /** When the session is opened. (players can join during this state if there are slots left (put in queue)) */
    COUNTDOWN(ChatColor.GREEN, "&a&lOPEN", (byte)10), /** When the sessions 10 second countdown has started. (players can still join during this state. (direct tp)) */
    STARTED(ChatColor.GRAY, "&7&lSTARTED", (byte)8), /** When the session is started. (players can spectate during this state) */
    ON_HOLD(ChatColor.DARK_AQUA, "&7&lSTARTED", (byte)8), /** When the session is on hold. (After plugin loads all existing sessions will be put on hold till players join back) */
    ENDED(ChatColor.RED, "&c&lENDED", (byte)5), /** When the session has ended. (nobody can join during this state) */
    RESETTING(ChatColor.GOLD, "&6&lRESETTING", (byte)14); /** When the session is resetting. (nobody can join during this state) */

    private ChatColor color;
    private String signText;
    private byte dyeColor;

    State(org.bukkit.ChatColor color, String signText, byte dyeColor) {
        this.color = color;
        this.signText = signText;
        this.dyeColor = dyeColor;
    }

    /** Get the color that represents the state for display purposes */
    public ChatColor getColor() {
        return color;
    }

    /** Get the string that represents the state to be put on signs. */
    public String getSignText() {
        return signText;
    }

    public byte getDyeColor() {
        return dyeColor;
    }
}
