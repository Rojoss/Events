package com.clashwars.events.events._duckhunt;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.helpers.CWEntity;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.helpers.EntityTag;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.BaseEvent;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.State;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import com.clashwars.events.util.Util;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuckHunt extends BaseEvent {



    public DuckHunt() {
        super();
        setupOptions.add(new SetupOption(SetupType.CUBOID, "chickenSpawn", "Area where chickens will spawn in."));
        setupModifiers("DUCKHUNT_");

        new BukkitRunnable() {
            @Override
            public void run(){
                HashMap<Integer, GameSession> sessions = events.sm.getSessions();
                for (GameSession session : sessions.values()) {
                    if (session.getType() != EventType.DUCKHUNT) {
                        continue;
                    }
                    if (!session.isStarted()) {
                        continue;
                    }
                    DuckHuntSession duckHuntSession = (DuckHuntSession)session;

                    Cuboid cuboid = session.getMap().getCuboid("chickenSpawn");
                    Location location = cuboid.getMinLoc();
                    location.add(CWUtil.random(0, cuboid.getWidth()), CWUtil.random(0, cuboid.getHeight()) - 1, CWUtil.random(0, cuboid.getLength()));

                    for (CWEntity chicken : duckHuntSession.chickens) {
                        if (chicken.entity() != null && chicken.entity().isValid()) {
                            if (cuboid.getWidth() > cuboid.getLength()) {
                                chicken.entity().setVelocity(new Vector(CWUtil.randomFloat(-0.25f, 0.25f), CWUtil.randomFloat(-0.05f, 0.2f), 0));
                            } else {
                                chicken.entity().setVelocity(new Vector(0, CWUtil.randomFloat(-0.05f, 0.2f), CWUtil.randomFloat(-0.25f, 0.25f)));
                            }
                        }
                    }

                    final CWEntity chicken = CWEntity.create(EntityType.CHICKEN, location);
                    chicken.setBaby(false);

                    CWEntity armorStand = CWEntity.create(EntityType.ARMOR_STAND, location);
                    armorStand.setArmorstandVisibility(false);
                    armorStand.setArmorstandGravity(false);
                    armorStand.setTag(EntityTag.MARKER, 1);
                    if (CWUtil.randomFloat() < 0.08f) {
                        armorStand.setName(CWUtil.integrateColor("&6&l3"));
                        chicken.setName(CWUtil.integrateColor("&6&l3"));
                        chicken.setBaby(true);
                    } else if (CWUtil.randomFloat() < 0.25f) {
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
                    duckHuntSession.chickens.add(chicken);

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
                    }.runTaskLater(events, 400);
                }
            }
        }.runTaskTimer(events, 0, 25);
    }

    public List<CWItem> getEquipment(GameSession session) {
        List<CWItem> equipment = new ArrayList<CWItem>();
        equipment.add(new CWItem(Material.BOW, 1).addEnchant(Enchantment.DURABILITY, 10).addEnchant(Enchantment.ARROW_INFINITE, 1).addEnchant(Enchantment.ARROW_DAMAGE, 10));
        equipment.add(new CWItem(Material.ARROW));
        return equipment;
    }


    @EventHandler
    private void projectileShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        if (validateSession((Player)event.getEntity().getShooter(), EventType.DUCKHUNT, false, State.STARTED)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void entityDmg(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.CHICKEN) {
            return;
        }
        GameSession session = events.sm.getSession(event.getEntity().getLocation());
        if (session == null || !(session instanceof DuckHuntSession)) {
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
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        if (!validateSession(killer, EventType.DUCKHUNT, false, State.STARTED)) {
            return;
        }
        GameSession session = getSession(killer);

        if (event.getEntity().getPassenger() != null) {
            event.getEntity().getPassenger().remove();
        }

        session.getBoard().setScore(DisplaySlot.SIDEBAR, killer.getName(),
                Math.max(session.getBoard().getScore(DisplaySlot.SIDEBAR, killer.getName()) + CWUtil.getInt(CWUtil.stripAllColor(event.getEntity().getName())), 0));
        if (session.getBoard().getScore(DisplaySlot.SIDEBAR, killer.getName()) >= 25) {
            session.end(killer.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void respawn(PlayerRespawnEvent event) {
        CWPlayer cwp = getCWPlayer(event.getPlayer());
        if (!validateSession(event.getPlayer(), EventType.DUCKHUNT, false)) {
            return;
        }

        Location loc = getSession(cwp).getTeleportLocation(cwp);
        event.setRespawnLocation(loc);
    }

}
