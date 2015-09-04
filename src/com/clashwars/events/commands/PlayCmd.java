package com.clashwars.events.commands;


import com.clashwars.events.commands.internal.PlayerCmd;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.util.Util;
import org.bukkit.entity.Player;

public class PlayCmd extends PlayerCmd {

    @Override
    public void onCommand(Player player, String[] args) {
        CWPlayer cwp = events.pm.getPlayer(player);
        if (cwp.inSession()) {
            player.sendMessage(Util.formatMsg("&cYou're already in a game!"));
            return;
        }

        events.gameMenu.showMenu(player);
    }
}
