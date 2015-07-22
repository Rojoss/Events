package com.clashwars.events.events.race;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.*;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Race extends BaseEvent {

    public Race() {
        super();
        setupOptions.add(new SetupOption(SetupType.CUBOID, "finish", "Where the race ends. "));
        setupOptions.add(new SetupOption(SetupType.CUBOID, "beginWall", "The wall that blocks the players. "));
        setupModifiers("RACE_");


        new BukkitRunnable() {
            @Override
            public void run() {
                HashMap<Integer, GameSession> sessions = events.sm.getSessions();
                for (final GameSession session : sessions.values()) {
                    if (session.getType() != EventType.RACE) {
                        continue;
                    }
                    if (!session.isStarted()) {
                        continue;
                    }
                    List<Player> onlinePlayers = session.getAllOnlinePlayers(false);
                    for (Player p : onlinePlayers) {
                        final Location playerLoc = p.getLocation();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                SessionData data = session.getData();
                                data.setEventData("player-loc", CWUtil.locToStringSimple(playerLoc));
                                session.setData(data);
                            }
                        }.runTaskLater(events, 100);
                    }
                }
            }
        }.runTaskTimer(events, 20, 20);
    }


    public List<CWItem> getEquipment(GameSession session) {
        HashMap<Modifier, ModifierOption> modifierOptions = session.getModifierOptions();
        List<CWItem> equipment = new ArrayList<CWItem>();
        equipment.add(new CWItem(Material.GOLD_BOOTS).setName("&3&lRacing Booties!").setLore(new String[]{"&7Masterfully crafted for top speeds!"}));
        return equipment;
    }


    @EventHandler(priority = EventPriority.HIGH)
    private void respawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!validateSession(player, EventType.RACE, false, State.STARTED)) {
            return;
        }
        final GameSession session = getSession(player);

        if (session.getData().hasEventData("player-loc") && !session.getData().getEventData("player-loc").isEmpty()) {
            event.setRespawnLocation(CWUtil.locFromStringSimple(session.getData().getEventData("player-loc")));
        } else {
            event.setRespawnLocation(session.getTeleportLocation(getCWPlayer(player)));
        }
    }

    @EventHandler
    private void playerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!validateSession(player, EventType.RACE, false, State.STARTED)) {
            return;
        }
        final GameSession session = getSession(player);

        if (session.hasPotentialWinner(player.getUniqueId())) {
            return;
        }

        Cuboid finish = session.getMap().getCuboid("finish");
        if (finish != null && finish.contains(event.getTo())) {
            session.addPotentialWinner(player.getUniqueId());

            //TODO: If all players finished end game and cancel the countdown timer.
            int finishedPlayers = session.getPotentialWinners().size();

            if (finishedPlayers == 1) {
                //TODO: Change to 1 second timer and show countdown message.
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        session.end(session.getPotentialWinners());
                    }
                }.runTaskLater(events, 200);
            }
            session.broadcast("&a&l" + player.getDisplayName() + " &6&lhas finished in &a&l" + finishedPlayers + CWUtil.getNumberSufix(finishedPlayers) + " &6&lplace!", true);
        }
    }


    @EventHandler
    private void entityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player p = (Player)event.getEntity();
        if (!validateSession(p, EventType.RACE, false, State.STARTED)) {
            return;
        }
        GameSession session = getSession((Player)event.getEntity());
        HashMap<Modifier, ModifierOption> modifiers = session.getModifierOptions();
        boolean pvp = modifiers.get(Modifier.RACE_PVP).getBoolean();
        if (pvp == true && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            event.setCancelled(false);
        } else if (pvp == false && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ) {
            return;
        }
        event.setCancelled(false);

    }
}


/*  @EventHandler
    private void interact(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.SNOW_BALL) {
            return;
        }
        if (validateSession((Player) event.getPlayer(), EventType.SPLEEF, false, State.STARTED)) {
            event.setCancelled(false);
        }
    } */
