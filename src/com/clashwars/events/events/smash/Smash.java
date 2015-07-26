package com.clashwars.events.events.smash;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.damage.Iattacker;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.events.CustomDamageEvent;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.packet.ParticleEffect;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.events.*;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.util.Vector;

import java.util.*;

public class Smash extends BaseEvent {

    private Vector[] relativeVectors = new Vector[] {new Vector(1,0,0), new Vector(-1,0,0), new Vector(0,0,1), new Vector(0,0,-1),
            new Vector(0,1,0), new Vector(1,1,0), new Vector(-1,1,0), new Vector(0,1,1), new Vector(0,1,-1)};
    List<UUID> smashedPlayers = new ArrayList<UUID>();
    List<Item> powerups = new ArrayList<Item>();

    public Smash() {
        super();
        setupModifiers("SMASH_");

        abilities.add(Ability.COOKIE);
        abilities.add(Ability.BREAD);
        abilities.add(Ability.STEAK);
        abilities.add(Ability.GOLDEN_CARROT);
        abilities.add(Ability.GOLDEN_APPLE);
        abilities.add(Ability.WOOD_SWORD);
        abilities.add(Ability.STONE_SWORD);
        abilities.add(Ability.IRON_SWORD);
        abilities.add(Ability.DIAMOND_SWORD);
        abilities.add(Ability.TOSS);

        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run(){
                List<Item> powerupList = new ArrayList<Item>(powerups);
                for (Item powerup : powerupList) {
                    if (powerup != null && powerup.isValid()) {
                        ParticleEffect.FIREWORKS_SPARK.display(0.2f, 0.2f, 0.2f, 0, 20, powerup.getLocation().add(0,1,0), 600);
                    } else {
                        powerups.remove(powerup);
                    }
                }

                HashMap<Integer, GameSession> sessions = events.sm.getSessions();
                for (GameSession session : sessions.values()) {
                    if (session.getType() != EventType.SMASH) {
                        continue;
                    }
                    if (!session.isStarted()) {
                        continue;
                    }

                    int powerupModifer = session.getModifierOption(Modifier.SMASH_POWERUPS).getInteger();
                    if (powerupModifer == 0 && i == 0) {
                        continue;
                    }

                    Cuboid map = session.getMap().getCuboid("map");
                    Block block = CWUtil.random(map.getBlocks());
                    for (int y = map.getMinY(); y < map.getMaxY(); y++) {
                        Block b = block.getWorld().getBlockAt(block.getX(), y, block.getZ());
                        if (b.getType() == Material.AIR) {
                            continue;
                        }

                        int attempts = map.getMaxY() - map.getMinY();
                        int count = 0;
                        while (b.getType() != Material.AIR && count < attempts) {
                            b = b.getRelative(BlockFace.UP);
                            count++;
                        }

                        Ability ability = CWUtil.random(abilities);
                        powerups.add(CWUtil.dropItemStack(b.getLocation().add(0.5f, 0.5f, 0.5f), ability.getAbilityClass().getCastItem()));
                        ParticleEffect.FIREWORKS_SPARK.display(0.1f, 10, 0.1f, 0, 100, b.getLocation().add(0.5f, 5, 0.5f), 600);
                        b.getWorld().playSound(b.getLocation(), Sound.CHICKEN_EGG_POP, 1, 2);
                        break;
                    }
                }
                if (i >= 1) {
                    i = 0;
                }
                i++;
            }
        }.runTaskTimer(events, 40, 40);
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

        if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            Location loc = event.getEntity().getWorld().getHighestBlockAt(event.getEntity().getLocation()).getLocation().add(0,1,0);
            loc.setYaw(event.getEntity().getLocation().getYaw());
            loc.setPitch(event.getEntity().getLocation().getPitch());
            event.getEntity().teleport(loc);
        }
    }

    @EventHandler
    private void customDamage(CustomDamageEvent event) {
        if (!validateSession(event.getPlayer(), EventType.SMASH, false)) {
            return;
        }

        int dmg = (int)Math.ceil(event.getDamage());
        if (event.getDmgClass() instanceof Iattacker) {
            Player attacker = ((Iattacker)event.getDmgClass()).getAttacker().getPlayer();
            if (attacker.getItemInHand() == null || attacker.getItemInHand().getType() == Material.AIR) {
                dmg += 1;
            }
        }

        event.getPlayer().setLevel(event.getPlayer().getLevel() + dmg);

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
        if (validateSession((Player)event.getEntity().getShooter(), EventType.SMASH, false, State.STARTED)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void itemPickup(PlayerPickupItemEvent event) {
        if (!validateSession(event.getPlayer(), EventType.SMASH, false, State.STARTED)) {
            return;
        }
        ItemStack firstItem = event.getPlayer().getInventory().getItem(0);
        if (firstItem != null && firstItem.getType() != Material.AIR) {
            CWUtil.sendActionBar(event.getPlayer(), "&4&l", "&cYou can only hold one powerup!");
            return;
        }
        event.setCancelled(false);
    }

    @EventHandler
    private void itemDrop(PlayerDropItemEvent event) {
        if (!validateSession(event.getPlayer(), EventType.SMASH, false, State.STARTED)) {
            return;
        }
        if (event.getPlayer().getInventory().getHeldItemSlot() != 0) {
            return;
        }
        event.setCancelled(false);
        event.getItemDrop().remove();
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
        player.setVelocity(player.getVelocity().add(new Vector(dir.getX() * 1.2f, 0.8f, dir.getZ() * 1.2f)));
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
                        radius = 4f;
                    } else if (level >= 200) {
                        radius = 3.5f;
                    } else if (level >= 150) {
                        radius = 3f;
                    } else if (level >= 100) {
                        radius = 2.5f;
                    } else if (level >= 50) {
                        radius = 2f;
                    } else {
                        radius = 1f;
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
                        }.runTaskLater(events, CWUtil.random(600, 1000));
                    }

                    block.setType(Material.AIR);
                }
            }
        }
    }


}
