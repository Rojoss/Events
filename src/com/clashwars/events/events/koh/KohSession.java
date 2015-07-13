package com.clashwars.events.events.koh;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.SessionData;
import org.bukkit.entity.Player;

public class KohSession extends GameSession {

    public KohSession(SessionData data, boolean loadedFromConfig) {
        super(data, loadedFromConfig);
        session = this;
        maxTime = 600;
    }

    @Override
    public void teleportPlayer(Player player) {
        player.teleport(getMap().getMultiLoc("spawn", 1));
    }
}
