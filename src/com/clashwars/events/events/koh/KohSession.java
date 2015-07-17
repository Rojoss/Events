package com.clashwars.events.events.koh;

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

public class KohSession extends GameSession {

    public KohSession(SessionData data, boolean loadedFromConfig) {
        super(data, loadedFromConfig);
        session = this;
        maxTime = 600;
    }

    @Override
    public void lock() {
        super.lock();
        HashMap<Modifier, ModifierOption> modifierOptions = getModifierOptions();

        Objective sidebarObj = board.addObjective("lives-side", "&4&lLIVES", Criteria.DUMMY, DisplaySlot.SIDEBAR, true);

        List<UUID> playerList = getAllPlayers(false);
        for (UUID player : playerList) {
            String playerName = CWUtil.getName(player);
            board.setScore(DisplaySlot.SIDEBAR, playerName, modifierOptions.get(Modifier.KOH_LIVES).getInteger());
        }

        if (modifierOptions.get(Modifier.KOH_TEAMS).getBoolean()) {
            int playerSize = getAllPlayers(false).size();
            if (playerSize > 3) {
                List<Integer> playerCounts = Arrays.asList(new Integer[] {2,3,4,5,6,7,8,9,10});
                Collections.shuffle(playerCounts);
                for (int i = 0; i < 9; i++) {
                    if (playerSize % playerCounts.get(i) == 0) {
                        setupTeams(playerCounts.get(i));
                        return;
                    }
                }
            }
            setupTeams(1);
            return;
        }
    }
    
    @Override
    public void start() {
        super.start();
        List<UUID> playerList = getAllPlayers(false);
        for (UUID player : playerList) {
            Freeze.unfreeze(player);
        }
    }

    @Override
    public void reset() {
        super.reset();
        delete();
    }

    @Override
    public void teleportPlayer(Player player) {
        super.teleportPlayer(player);
        CWPlayer cwp = events.pm.getPlayer(player);
        if (!isStarted() && !cwp.isSpectating()) {
            Freeze.freeze(player.getUniqueId(), player.getLocation());
        }
    }

    private void setupTeams(int playersPerTeam) {
        List<UUID> allPlayers = getAllPlayers(false);

        int count = 0;
        int id = 0;
        String team = "";

        for (UUID uuid : allPlayers) {
            if (team.isEmpty() || count % playersPerTeam == 0) {
                board.addTeam("team-" + id, CWUtil.getPrefix(true, id), "", false, false);
                team = "team-" + id;
                id++;
            }
            board.joinTeam(team, events.getServer().getOfflinePlayer(uuid));
            count++;
        }
    }
}
