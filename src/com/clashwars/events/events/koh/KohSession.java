package com.clashwars.events.events.koh;

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
        super.teleportPlayer(player);
        //TODO: Freeze player
    }
}
