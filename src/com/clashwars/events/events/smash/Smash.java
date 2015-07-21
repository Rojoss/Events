package com.clashwars.events.events.smash;

import com.clashwars.cwcore.damage.Iattacker;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.events.CustomDamageEvent;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.*;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.util.Vector;

import java.util.*;

public class Smash extends BaseEvent {

    private Vector[] relativeVectors = new Vector[] {new Vector(1,0,0), new Vector(-1,0,0), new Vector(0,0,1), new Vector(0,0,-1),
            new Vector(0,1,0), new Vector(1,1,0), new Vector(-1,1,0), new Vector(0,1,1), new Vector(0,1,-1)};
    List<UUID> smashedPlayers = new ArrayList<UUID>();

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
        Player player = event.getPlayer();
        CWPlayer cwp = getCWPlayer(player);
        if (!validateSession(player, EventType.SMASH, false)) {
            return;
        }

        GameSession session = cwp.getSession();
        session.setPotentialWinners(session.getAllPlayers(false));
        session.removePotentialWinner(player.getUniqueId());

        if (session.isStarted()) {
            int score = session.getBoard().getScore(DisplaySlot.SIDEBAR, cwp.getName());

            if (score <= 0) {
                session.getBoard().resetScore(player.getName());
                session.switchToSpectator(player);
            } else {
                CWUtil.resetPlayer(player, GameMode.SURVIVAL);
                Util.equipItems(player, getEquipment(session));
                player.setAllowFlight(true);
                player.setExp(1);
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

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (validateSession((Player)event.getEntity(), EventType.SMASH, false, State.STARTED)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void customDamage(CustomDamageEvent event) {
        if (!validateSession(event.getPlayer(), EventType.SMASH, false)) {
            return;
        }

        event.getPlayer().setLevel(event.getPlayer().getLevel() + (int)Math.ceil(event.getDamage()));

        if (event.getDmgClass() instanceof Iattacker) {
            if (((Iattacker)event.getDmgClass()).hasAttacker()) {
                if (!smashedPlayers.contains(event.getPlayer().getUniqueId())) {
                    smashedPlayers.add(event.getPlayer().getUniqueId());
                }

                OfflinePlayer attacker = ((Iattacker)event.getDmgClass()).getAttacker();
                if (attacker != null && attacker.isOnline()) {
                    float vxz = 0.2f;
                    float vy = 0.2f;
                    int level = event.getPlayer().getLevel();
                    if (level >= 250) {
                        vxz = 3.5f;
                        vy = 0.88f;
                    } else if (level >= 200) {
                        vxz = 3f;
                        vy = 0.8f;
                    } else if (level >= 160) {
                        vxz = 2.5f;
                        vy = 0.72f;
                    } else if (level >= 120) {
                        vxz = 2f;
                        vy = 0.64f;
                    } else if (level >= 80) {
                        vxz = 1.5f;
                        vy = 0.56f;
                    } else if (level >= 40) {
                        vxz = 1f;
                        vy = 0.48f;
                    } else if (level >= 0) {
                        vxz = 0.7f;
                        vy = 0.42f;
                    }

                    Vector dir = ((Player)attacker).getLocation().getDirection();
                    event.getPlayer().setVelocity(event.getPlayer().getVelocity().add(new Vector(dir.getX() * vxz, vy, dir.getZ() * vxz)));
                }

            }
        }
        event.setDamage(0);
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
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        if (event.getPlayer().isFlying()) {
            return;
        }

        if (!validateSession(event.getPlayer(), EventType.SMASH, false)) {
            return;
        }

        event.setCancelled(true);
        if (player.getExp() != 1) {
            return;
        }
        player.playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 0.6f, 1.1f);
        Vector dir = player.getLocation().getDirection();
        player.setVelocity(player.getVelocity().add(new Vector(dir.getX() * 1.4f, 0.9f, dir.getZ() * 1.4f)));
        player.setExp(0);
        player.setAllowFlight(false);
    }

    @EventHandler
    private void move(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        if (!validateSession(event.getPlayer(), EventType.SMASH, false)) {
            return;
        }
        GameSession session = getSession(event.getPlayer());

        //Trigger ground pound
        if (player.isSneaking()) {
            if (event.getTo().getBlockY() != event.getFrom().getBlockY() && event.getTo().getY() < event.getFrom().getY()) {
                player.setExp(Math.min(player.getExp() + 0.1f, 1));
                player.setVelocity(player.getVelocity().add(new Vector(0,-1,0)));
            }
        }

        //Smashing against walls/ceilings etc.
        if (smashedPlayers.contains(player.getUniqueId())) {
            Block block = event.getTo().getBlock();
            for (Vector offset : relativeVectors) {
                if (block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ()).getType() != Material.AIR) {
                    smashedPlayers.remove(player.getUniqueId());

                    double radius = 0;
                    int level = player.getLevel();
                    if (level >= 250) {
                        radius = 3.5f;
                    } else if (level >= 200) {
                        radius = 3f;
                    } else if (level >= 160) {
                        radius = 2.5f;
                    } else if (level >= 120) {
                        radius = 2f;
                    } else if (level >= 80) {
                        radius = 1.5f;
                    }

                    if (session.getModifierOption(Modifier.SMASH_DESTRUCTION).getInteger() == 1) {
                        radius += 1f;
                    }

                    if (radius > 0) {
                        boolean blockRegen = session.getModifierOption(Modifier.SMASH_BLOCK_REGEN).getBoolean();
                        createExplosion(block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ()).getLocation(), player, radius, blockRegen);
                    }
                }
            }
        }

        // Landing on ground
        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
            //Debug.bc(player.getVelocity().getY());
            if (smashedPlayers.contains(player.getUniqueId()) && player.getVelocity().getY() < -0.1f) {
                smashedPlayers.remove(player.getUniqueId());
            }

            if (player.getExp() != 1) {
                if (player.getExp() == 0) {
                    //Normal landing
                    player.setExp(1);
                    player.setAllowFlight(true);
                } else if (player.getExp() < 1 && player.getFoodLevel() == 20) {
                    //Land with ground pound
                    //TODO: Create ground pound effect.
                    player.setFoodLevel(19);
                    player.setExp(0.1f);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.getExp() < 1) {
                                player.setExp(Math.min(player.getExp() + 0.1f, 1));
                            } else {
                                player.setFoodLevel(20);
                                player.setExp(1);
                                player.setAllowFlight(true);
                                cancel();
                            }
                        }
                    }.runTaskTimer(events, 10, 10);
                }
            }
        }
    }


    private void createExplosion(Location location, Player player, double radius, boolean regen) {
        location = location.add(0.5f, 0.5f, 0.5f);
        for (double x = location.getBlockX() - radius; x < location.getBlockX() + radius; x++) {
            for (double y = location.getBlockY() - radius; y < location.getBlockY() + radius; y++) {
                for (double z = location.getBlockZ() - radius; z < location.getBlockZ() + radius; z++) {
                    Block block = location.getWorld().getBlockAt((int)x, (int)y, (int)z);
                    if (block.getType() == Material.AIR) {
                        continue;
                    }
                    double distance = block.getLocation().distance(location);
                    if (distance > radius) {
                        continue;
                    }
                    if (distance > radius-1  && CWUtil.randomFloat() > 0.5f) {
                        continue;
                    }

                    FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(block.getLocation(), block.getType(), block.getData());
                    Vector dir = block.getLocation().toVector().subtract(location.toVector()).normalize();
                    fallingBlock.setVelocity(dir.multiply(0.5f));
                    fallingBlock.getLocation().getWorld().playEffect(fallingBlock.getLocation(), Effect.STEP_SOUND, block.getTypeId());

                    if (regen) {
                        final Material type = block.getType();
                        final byte data = block.getData();
                        final Location loc = block.getLocation();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Block b = loc.getBlock();
                                if (b.getType() == Material.AIR) {
                                    b.setType(type);
                                    b.setData(data);
                                }
                            }
                        }.runTaskLater(events, CWUtil.random(200, 600));
                    }

                    block.setType(Material.AIR);
                }
            }
        }
    }


}
