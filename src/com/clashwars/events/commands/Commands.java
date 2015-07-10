package com.clashwars.events.commands;

import com.clashwars.events.Events;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Commands {

    private Events ev;

    public Commands(Events ev) {
        this.ev = ev;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return false;
    }
}
