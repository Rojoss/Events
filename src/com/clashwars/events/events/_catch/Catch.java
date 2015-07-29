package com.clashwars.events.events._catch;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.helpers.CWEntity;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.helpers.EntityTag;
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
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Catch extends BaseEvent {



    public Catch() {
        super();
        setupOptions.add(new SetupOption(SetupType.CUBOID, "chickenSpawn", "Area where chickens will spawn in."));
        setupOptions.add(new SetupOption(SetupType.MULTI_CUBOID, "deathBox", "Area where it will reward points when chicken dies. Needs an area per player with the same ID as the spawn point."));
        setupModifiers("CATCH_");

        new BukkitRunnable() {
            @Override
            public void run(){
                HashMap<Integer, GameSession> sessions = events.sm.getSessions();
                for (GameSession session : sessions.values()) {
                    if (session.getType() != EventType.CATCH) {
                        continue;
                    }
                    if (!session.isStarted()) {
                        continue;
                    }
                    CatchSession catchSession = (CatchSession)session;

                    Cuboid cuboid = session.getMap().getCuboid("chickenSpawn");
                    Location location = cuboid.getMinLoc();
                    location.add(CWUtil.random(0, cuboid.getWidth()), CWUtil.random(0, cuboid.getHeight()), CWUtil.random(0, cuboid.getLength()));

                    final CWEntity chicken = CWEntity.create(EntityType.CHICKEN, location);
                    chicken.setBaby(false);

                    CWEntity armorStand = CWEntity.create(EntityType.ARMOR_STAND, location);
                    armorStand.setArmorstandVisibility(false);
                    armorStand.setArmorstandGravity(false);
                    armorStand.setTag(EntityTag.MARKER, 1);
                    if (CWUtil.randomFloat() < 0.08f) {
                        armorStand.setName(CWUtil.integrateColor("&6&l3"));
                        chicken.setName(CWUtil.integrateColor("&6&l3"));
                    } else if (CWUtil.randomFloat() < 0.15f) {
                        armorStand.setName(CWUtil.integrateColor("&c&l-1"));
                        chicken.setName(CWUtil.integrateColor("&c&l-1"));
                    } else if (CWUtil.randomFloat() < 0.2f) {
                        armorStand.setName(CWUtil.integrateColor("&e&l2"));
                        chicken.setName(CWUtil.integrateColor("&e&l2"));
                    } else {
                        armorStand.setName(CWUtil.integrateColor("&a&l1"));
                        chicken.setName(CWUtil.integrateColor("&a&l1"));
                    }
                    armorStand.setNameVisible(true);

                    chicken.entity().setPassenger(armorStand.entity());
                    catchSession.chickens.add(chicken);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (chicken != null && chicken.entity() != null) {
                                if (chicken.entity().getPassenger() != null) {
                                    chicken.entity().getPassenger().remove();
                                }
                                chicken.entity().remove();
                            }
                        }
                    }.runTaskLater(events, 200);
                }
            }
        }.runTaskTimer(events, 0, 60);
    }

    public List<CWItem> getEquipment(GameSession session) {
        List<CWItem> equipment = new ArrayList<CWItem>();
        equipment.add(new CWItem(Material.FISHING_ROD, 1).addEnchant(Enchantment.DURABILITY, 10));
        return equipment;
    }


    @EventHandler
    private void projectileShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        if (validateSession((Player)event.getEntity().getShooter(), EventType.CATCH, false, State.STARTED)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void interact(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.FISHING_ROD) {
            return;
        }
        if (validateSession(event.getPlayer(), EventType.CATCH, false, State.STARTED)) {
            event.setUseItemInHand(Event.Result.ALLOW);
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void entityDmg(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.CHICKEN) {
            return;
        }
        GameSession session = events.sm.getSession(event.getEntity().getLocation());
        if (session == null || !(session instanceof CatchSession)) {
            return;
        }
        if (!session.isStarted()) {
            return;
        }
        event.setCancelled(false);
    }

    @EventHandler
    private void entityDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.CHICKEN) {
            return;
        }
        GameSession session = events.sm.getSession(event.getEntity().getLocation());
        if (session == null || !(session instanceof CatchSession)) {
            return;
        }
        if (!session.isStarted()) {
            return;
        }

        HashMap<Integer, Cuboid> deathBoxes = session.getMap().getMultiCuboids("deathBox");
        for (Map.Entry<Integer, Cuboid> entry : deathBoxes.entrySet()) {
            if (entry.getValue().contains(event.getEntity())) {
                List<Player> players = session.getAllOnlinePlayers(false);
                for (Player player : players) {
                    CWPlayer cwp = getCWPlayer(player);
                    if (cwp.getTeleportID() == entry.getKey()) {
                        session.getBoard().setScore(DisplaySlot.SIDEBAR, cwp.getName(),
                                Math.max(session.getBoard().getScore(DisplaySlot.SIDEBAR, cwp.getName()) + CWUtil.getInt(CWUtil.stripAllColor(event.getEntity().getName())), 0));
                        if (session.getBoard().getScore(DisplaySlot.SIDEBAR, cwp.getName()) >= 20) {
                            session.end(cwp.getUUID());
                        }
                        return;
                    }
                }
            }
        }
    }

}
