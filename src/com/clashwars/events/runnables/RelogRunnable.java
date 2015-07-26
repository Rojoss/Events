package com.clashwars.events.runnables;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.events.Events;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.player.CWPlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class RelogRunnable extends BukkitRunnable {

    CWPlayer cwp;

    public RelogRunnable(CWPlayer cwp) {
        this.cwp = cwp;
        runTaskLater(Events.inst(), 200);
    }


    @Override
    public void run() {
        cwp.removeRelogRunnable();
        GameSession session = cwp.getSession();
        if (session == null) {
            return;
        }
        if (!cwp.isOnline() && session.hasPlayer(cwp.getUUID(), true, true, true)) {
            session.leave(cwp.getOfflinePlayer(), false);
        }
    }
}
