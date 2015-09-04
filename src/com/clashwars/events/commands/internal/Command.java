package com.clashwars.events.commands.internal;

import com.clashwars.events.commands.EventCmd;
import com.clashwars.events.commands.LeaveCmd;
import com.clashwars.events.commands.PlayCmd;
import com.clashwars.events.commands.SetupCmd;

public enum Command {
    SETUP(new SetupCmd(), "events.setup"),
    EVENT(new EventCmd(), "events.event", "events", "ev", "e"),
    LEAVE(new LeaveCmd(), "", "quit", "hub", "lobby"),
    PLAY(new PlayCmd(), "", "play", "game", "playgame");

    private BaseCmd cmd;
    private String permission;
    private String[] aliases;

    Command(BaseCmd cmd, String permission, String... aliases) {
        this.cmd = cmd;
        this.permission = permission;
        this.aliases = aliases;
    }

    public BaseCmd getCmdClass() {
        return cmd;
    }

    public String getPermission() {
        return permission;
    }

    public String[] getAliases() {
        return aliases;
    }

    public boolean isPlayerCmd() {
        return (cmd instanceof PlayerCmd);
    }
}
