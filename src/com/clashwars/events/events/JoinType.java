package com.clashwars.events.events;

/**
 * When trying to join an event one of these types will be returned.
 */
public enum JoinType {
    INVALID, /** When the session/map is invalid. */
    CLOSED, /** When the map is closed/locked. */
    FULL, /** When the session is full */
    ENDED, /** When the session has ended */
    RESETTING, /** When the session is still resetting */
    JOIN, /** When the player can join the session */
    JOIN_BACK, /** When the player joins back while a session is on hold */
    SPECTATE; /** When the session is started but the player can spectate */
}
