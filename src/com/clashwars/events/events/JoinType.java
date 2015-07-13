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
    IN_GAME, /** When the player is already in a session. (Most likely queued but doesn't have to be) */
    QUEUE, /** When the player can join the session but the 10 second timer hasn't started */
    JOIN, /** When the player can join the session */
    JOIN_BACK, /** When the player joins back while a session is on hold */
    SPECTATE; /** When the session is started but the player can spectate */
}
