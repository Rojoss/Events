package com.clashwars.events.setup;

/**
 * A setup option with a SetupType, name and description.
 * Each event can have their own custom SetupOptions.
 * When validating the map all setup options have to be set up properly.
 */
public class SetupOption {

    public SetupType type;
    public String name;
    public String description;

    /** Create a new SetupOption with the specified SetupType, name to identify it and a description what this setup option is for */
    public SetupOption(SetupType type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }
}
