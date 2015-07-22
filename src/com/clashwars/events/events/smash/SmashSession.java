package com.clashwars.events.events.smash;

import com.clashwars.cwcore.player.Freeze;
import com.clashwars.cwcore.scoreboard.Criteria;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.SessionData;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.player.CWPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.*;

public class SmashSession extends GameSession {

    public SmashSession(SessionData data, boolean loadedFromConfig) {
        super(data, loadedFromConfig);
        session = this;
        maxTime = 600;
    }

    @Override
    public void lock() {
        super.lock();
        int lives = getModifierOption(Modifier.SMASH_LIVES).getInteger();

        Objective sidebarObj = board.addObjective("lives-side", "&4&lLIVES", Criteria.DUMMY, DisplaySlot.SIDEBAR, true);
        List<UUID> playerList = getAllPlayers(false);
        for (UUID player : playerList) {
            String playerName = CWUtil.getName(player);
            board.setScore(DisplaySlot.SIDEBAR, playerName, lives);
        }
    }

    @Override
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        delete();
        return true;
    }

    @Override
    public void teleportPlayer(Player player) {
        super.teleportPlayer(player);
        player.setAllowFlight(true);
        player.setExp(1);
    }
}
