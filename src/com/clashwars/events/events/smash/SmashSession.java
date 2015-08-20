package com.clashwars.events.events.smash;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.dependencies.CWWorldEdit;
import com.clashwars.cwcore.scoreboard.Criteria;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.SessionData;
import com.clashwars.events.modifiers.Modifier;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.world.DataException;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.primesoft.asyncworldedit.api.blockPlacer.IJobEntryListener;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;

import java.io.File;
import java.io.IOException;
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

        Cuboid mapCuboid = getMap().getCuboid("map");

        File mapsFolder = new File(events.getDataFolder(), "maps");
        mapsFolder.mkdir();
        try {
            IJobEntryListener callback = new IJobEntryListener() {
                @Override
                public void jobStateChanged(JobEntry jobEntry) {
                    if (jobEntry.isTaskDone() && jobEntry.getStatus() == JobEntry.JobStatus.Done) {
                        delete();
                    }
                }
            };
            CWWorldEdit.loadSchematicAsync(new File(mapsFolder, getMapTag()), mapCuboid.getMinLoc(), callback);
        } catch (FilenameException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (com.sk89q.worldedit.data.DataException e) {
            e.printStackTrace();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void teleportPlayer(Player player) {
        super.teleportPlayer(player);
        player.setAllowFlight(true);
        player.setExp(1);
    }
}
