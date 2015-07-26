package com.clashwars.events.commands;


import com.clashwars.events.commands.internal.PlayerCmd;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.util.Util;
import org.bukkit.entity.Player;

public class LeaveCmd extends PlayerCmd {

    @Override
    public void onCommand(Player player, String[] args) {
        CWPlayer cwp = events.pm.getPlayer(player);
        if (!cwp.inSession()) {
            player.sendMessage(Util.formatMsg("&cYou're not in a game!"));
            return;
        }

        GameSession session = cwp.getSession();
        if (session != null) {
            session.leave(player, false);
        }

        cwp.removeSession();
        player.sendMessage(Util.formatMsg("&6You left the game!"));
    }
}
