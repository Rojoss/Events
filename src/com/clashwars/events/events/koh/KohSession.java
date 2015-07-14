package com.clashwars.events.events.koh;

import com.clashwars.cwcore.scoreboard.Criteria;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.SessionData;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

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

        board.addObjective("lives-side", Criteria.DUMMY, DisplaySlot.SIDEBAR, true);
        Objective sidebarObj = board.getObjective("lives-side");
        sidebarObj.setDisplayName(CWUtil.integrateColor("&4&lLIVES"));

        List<UUID> playerList = getAllPlayers(false);
        for (UUID player : playerList) {
            String playerName = CWUtil.getName(player);
            Score score = sidebarObj.getScore(playerName);
            score.setScore(modifierOptions.get(Modifier.KOH_LIVES).getInteger());
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
    public void teleportPlayer(Player player) {
        super.teleportPlayer(player);
        //TODO: Freeze player
    }

    private void setupTeams(int playersPerTeam) {
        List<UUID> allPlayers = getAllPlayers(false);

        int count = 0;
        int id = 0;
        Team team = null;

        for (UUID uuid : allPlayers) {
            if (team == null || count % playersPerTeam == 0) {
                board.addTeam("team-" + id, CWUtil.getPrefix(true, id), "", false, false);
                team = board.getTeam("team-" + id);
                id++;
            }
            team.addPlayer(events.getServer().getOfflinePlayer(uuid));
            count++;
        }
    }
}
