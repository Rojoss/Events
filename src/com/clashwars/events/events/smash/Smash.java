package com.clashwars.events.events.smash;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.*;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import com.clashwars.events.util.Util;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Smash extends BaseEvent {

    public Smash() {
        super();
        setupModifiers("SMASH_");
    }

    public List<CWItem> getEquipment(GameSession session) {
        HashMap<Modifier, ModifierOption> modifierOptions = session.getModifierOptions();
        List<CWItem> equipment = new ArrayList<CWItem>();

        return equipment;
    }


    @EventHandler
    private void death(PlayerDeathEvent event) {
        CWPlayer cwp = getCWPlayer(event.getEntity());
        if (!validateSession(event.getEntity(), EventType.SMASH, false, State.STARTED)) {
            return;
        }

        GameSession session = cwp.getSession();
        int score = session.getBoard().getScore(DisplaySlot.SIDEBAR, cwp.getName());

        if (score > 0) {
            session.getBoard().setScore(DisplaySlot.SIDEBAR, event.getEntity().getName(), score - 1);
        }
        event.getEntity().spigot().respawn();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void respawn(PlayerRespawnEvent event) {
        CWPlayer cwp = getCWPlayer(event.getPlayer());
        if (!validateSession(event.getPlayer(), EventType.SMASH, false)) {
            return;
        }

        GameSession session = cwp.getSession();
        session.setPotentialWinners(session.getAllPlayers(false));
        session.removePotentialWinner(event.getPlayer().getUniqueId());

        if (session.isStarted()) {
            int score = session.getBoard().getScore(DisplaySlot.SIDEBAR, cwp.getName());

            if (score <= 0) {
                session.getBoard().resetScore(event.getPlayer().getName());
                session.switchToSpectator(event.getPlayer());
            } else {
                CWUtil.resetPlayer(event.getPlayer(), GameMode.SURVIVAL);
                Util.equipItems(event.getPlayer(), getEquipment(session));
            }
        }

        Location loc = session.getTeleportLocation(cwp);
        event.setRespawnLocation(loc);
        //TODO: Spawn on save location since terrain can be destroyed.
        //TODO: 5 second invincible time.
    }


    @EventHandler
    private void entityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!validateSession((Player)event.getEntity(), EventType.SMASH, false, State.STARTED)) {
            return;
        }

        event.setCancelled(false);

        //TODO: Knockback

        //TODO: Increase smash damage percentage
    }

    @EventHandler
    private void projectileShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        if (validateSession((Player)event.getEntity().getShooter(), EventType.KOH, false, State.STARTED)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void toggleFlight(PlayerToggleFlightEvent event) {
        if (event.getPlayer().isFlying()) {
            return;
        }
        event.setCancelled(true);

        //TODO: Double jumping...
    }

    //TODO: Destroy terrain when smashed against blocks.

}
