package com.clashwars.events.events;

import org.bukkit.ChatColor;

/**
 * The state of a GameSession.
 */
public enum State {
    CLOSED(ChatColor.DARK_RED), /** When the map is closed/locked */
    OPENED(ChatColor.GREEN), /** When the session is opened. (players can join during this state if there are slots left) */
    STARTED(ChatColor.GRAY), /** When the session is started. (players can spectate during this state) */
    ON_HOLD(ChatColor.DARK_AQUA), /** When the session is on hold. (After plugin loads all existing sessions will be put on hold till players join back) */
    ENDED(ChatColor.RED), /** When the session has ended. (nobody can join during this state) */
    RESETTING(ChatColor.GOLD); /** When the session is resetting. (nobody can join during this state) */

    private ChatColor color;

    State(org.bukkit.ChatColor color) {
        this.color = color;
    }

    /** Get the color that represents the state for display purposes */
    public ChatColor getColor() {
        return color;
    }
}
