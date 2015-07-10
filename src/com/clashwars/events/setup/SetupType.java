package com.clashwars.events.setup;

/**
 * A type of (location) needed for setting up a map.
 */
public enum SetupType {
    LOCATION, /** A single location with a yaw/pitch for player teleports and such */
    BLOCK_LOC, /** A single location with just a x,y,z for a block location like the event sign */
    MULTI_LOC, /** Multiple locations under one name. For example 'spawn' and then it could have spawn_0, spawn_1, spawn_2 etc. */
    CUBOID; /** A cuboid with two locations to set up areas */
}
