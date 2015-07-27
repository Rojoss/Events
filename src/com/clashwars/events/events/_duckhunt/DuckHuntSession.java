package com.clashwars.events.events._duckhunt;

import com.clashwars.cwcore.helpers.CWEntity;
import com.clashwars.cwcore.scoreboard.Criteria;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.SessionData;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DuckHuntSession extends GameSession {

    public List<CWEntity> chickens = new ArrayList<CWEntity>();

    public DuckHuntSession(SessionData data, boolean loadedFromConfig) {
        super(data, loadedFromConfig);
        session = this;
        maxTime = 300;
    }

    @Override
    public void lock() {
        super.lock();

        Objective sidebarObj = board.addObjective("points-side", "&a&lCHICKEN SHOT", Criteria.DUMMY, DisplaySlot.SIDEBAR, true);

        List<UUID> playerList = getAllPlayers(false);
        for (UUID player : playerList) {
            String playerName = CWUtil.getName(player);
            board.setScore(DisplaySlot.SIDEBAR, playerName, 0);
        }

        setupTeams(1);
    }

    @Override
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        for (CWEntity chicken : chickens) {
            if (chicken != null && chicken.entity() != null) {
                if (chicken.entity().getPassenger() != null) {
                    chicken.entity().getPassenger().remove();
                }
                chicken.entity().remove();
            }
        }
        delete();
        return true;
    }
}
