package com.clashwars.events.events.spleef;

import com.clashwars.cwcore.config.aliases.PotionEffects;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.SessionData;
import com.clashwars.events.modifiers.Modifier;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SpleefSession extends GameSession {

    public SpleefSession(SessionData data, boolean loadedFromConfig) {
        super(data, loadedFromConfig);
        session = this;
        maxTime = 600;
    }

    @Override
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        for (Block b : getMap().getCuboid("floor").getBlocks()) {
            if (b.getType() == Material.AIR) {
                b.setType(Material.SNOW_BLOCK);
            }
        }
        delete();
        return true;
    }

    @Override
    public void teleportPlayer(Player player) {
        super.teleportPlayer(player);
        int speed = getModifierOption(Modifier.SPLEEF_SPEED).getInteger();
        if (speed == 0) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999999, speed));

    }


    @Override
    public void start() {
        super.start();
        boolean blindness = getModifierOption(Modifier.SPLEEF_BLINDNESS).getBoolean();
        if (!(blindness)) {
            return;
        }
        List<Player> onlinePlayers = session.getAllOnlinePlayers(false);
        for (Player player : onlinePlayers) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 99999, 1));
        }
    }
}
