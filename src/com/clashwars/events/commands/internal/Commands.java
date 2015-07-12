package com.clashwars.events.commands.internal;

import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.Events;
import com.clashwars.events.commands.internal.Command;
import com.clashwars.events.commands.internal.PlayerCmd;
import com.clashwars.events.util.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands {

    private Events ev;

    public Commands(Events ev) {
        this.ev = ev;
    }

    public boolean onCommand(CommandSender sender, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player)sender;
        }

        for (Command cmd : Command.values()) {
            if (isCmd(cmd, label)) {
                if (!hasPermissions(cmd, sender)) {
                    sender.sendMessage(Util.formatMsg("&cInsufficient permissions! &8'&7" + cmd.getPermission() + "&8'"));
                    return true;
                }
                if (cmd.isPlayerCmd()) {
                    if (player == null) {
                        sender.sendMessage(CWUtil.formatCWMsg("&cPlayer command only."));
                        return true;
                    } else {
                        ((PlayerCmd)cmd.getCmdClass()).onCommand(player, args);
                        return true;
                    }
                } else {
                    cmd.getCmdClass().onCommand(sender, args);
                    return true;
                }
            }
        }
        return false;
    }


    public boolean isCmd(Command cmd, String label) {
        if (cmd.toString().equalsIgnoreCase(label)) {
            return true;
        }
        for (String alias : cmd.getAliases()) {
            if (alias.equalsIgnoreCase(label)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPermissions(Command cmd, CommandSender sender) {
        if (sender.isOp()) {
            return true;
        }
        if (cmd.getPermission() == null || cmd.getPermission().isEmpty()) {
            return true;
        }
        if (sender.hasPermission("events.*") || sender.hasPermission(cmd.getPermission())) {
            return true;
        }
        return false;
    }
}
