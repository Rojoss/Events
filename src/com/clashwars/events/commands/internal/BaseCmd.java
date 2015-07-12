package com.clashwars.events.commands.internal;

import com.clashwars.events.Events;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BaseCmd {

    protected Events events = Events.inst();

    public void onCommand(CommandSender sender, String[] args) {
        //-
    }
}
