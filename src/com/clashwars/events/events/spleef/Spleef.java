package com.clashwars.events.events.spleef;

import com.clashwars.cwcore.damage.types.CustomDmg;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.events.ProjectileHitBlockEvent;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.packet.ParticleEffect;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.*;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Spleef extends BaseEvent {

    public Spleef() {
        super();
        setupOptions.add(new SetupOption(SetupType.CUBOID, "floor", "Area that can be broken. "));
        setupModifiers("SPLEEF_");
        new BukkitRunnable() {
            @Override
            public void run(){
                HashMap<Integer, GameSession> sessions = events.sm.getSessions();
                for (GameSession session : sessions.values()) {
                    if (session.getType() != EventType.SPLEEF) {
                        continue;
                    }
                    if (!session.isStarted()) {
                        continue;
                    }


                    HashMap<Modifier, ModifierOption> modifiers = session.getModifierOptions();
                    int decay = modifiers.get(Modifier.SPLEEF_DECAY).getInteger();
                    int change = modifiers.get(Modifier.SPLEEF_FLOOR).getInteger();

                    if (decay > 0 || change > 0) {
                        List<Block> blocks = session.getMap().getCuboid("floor").getBlocks(new Material[]{Material.SNOW_BLOCK});

                        if (decay > 0) {
                            Block block = CWUtil.random(blocks);
                            destroyBlock(null, block, false);
                        }

                        if (change > 0) {
                            final Block block = CWUtil.random(blocks);
                            block.setType(Material.PACKED_ICE);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    block.setType(Material.SNOW_BLOCK);
                                }
                            }.runTaskLater(events, 100);
                        }
                    }
                }
            }
        }.runTaskTimer(events, 0, 2);
    }


    public List<CWItem> getEquipment(GameSession session) {
        HashMap<Modifier, ModifierOption> modifierOptions = session.getModifierOptions();
        List<CWItem> equipment = new ArrayList<CWItem>();

        int toolID = modifierOptions.get(Modifier.SPLEEF_TOOL).getInteger();
        if (toolID == 0) {
            equipment.add(new CWItem(Material.DIAMOND_SPADE).setName("&3&lSpade").setLore(new String[] {"&7Break blocks with this!"}));
        } else if (toolID == 1) {
            equipment.add(new CWItem(Material.BOW).addEnchant(Enchantment.ARROW_INFINITE, 1).setName("&e&lBow").setLore(new String[]{"&7Arrows shot from this bow will destroy snow."}));
            equipment.add(new CWItem(Material.ARROW));
        } else if (toolID == 2) {
            equipment.add(new CWItem(Material.TNT).setName("&c&lTnT").setLore(new String[] {"&7Throw it or place it to break blocks!"}));
        }
        return equipment;
    }

    @EventHandler
    private void projectileShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        if (validateSession((Player) event.getEntity().getShooter(), EventType.SPLEEF, false, State.STARTED)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void inventoryClick(InventoryClickEvent event) {
        if (validateSession((Player)event.getWhoClicked(), EventType.SPLEEF, false)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void playerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        //Get the player that broke the block when player is falling.
        if (event.getFrom().getBlockY() != event.getTo().getBlockY() && event.getTo().getY() < event.getFrom().getY()) {
            if (validateSession(player, EventType.SPLEEF, false, State.STARTED)) {
                GameSession session = getSession(player);

                if (event.getTo().getY() < session.getMap().getCuboid("floor").getMinY() + 1) {
                    Block block = event.getTo().getBlock();
                    if (block.hasMetadata("spleefer")) {
                        OfflinePlayer spleefer = events.getServer().getOfflinePlayer(block.getMetadata("spleefer").get(0).asString());
                        if (spleefer != null) {
                            new CustomDmg(event.getPlayer(), 0, "{0} got spleefed by {1}!", "", spleefer);
                            session.broadcast("&3" + player.getDisplayName() + " &bgot spleefed by &3" + spleefer.getName() + "!", true);
                        } else {
                            new CustomDmg(player, 0, "{0} has fallen!", "");
                            session.broadcast("&3" + player.getDisplayName() + " &bhas fallen!", true);
                        }
                    } else {
                        new CustomDmg(player, 0, "{0} has fallen!", "");
                        session.broadcast("&3" + player.getDisplayName() + " &bhas fallen!", true);
                    }

                    session.setPotentialWinners(session.getAllPlayers(false));
                    session.removePotentialWinner(player.getUniqueId());
                    session.switchToSpectator(player);
                    event.setCancelled(true);
                }
            }
        }

        //Trail stuff
        final Block blockBelow = event.getFrom().getBlock().getRelative(BlockFace.DOWN);
        if (blockBelow.getType() != Material.SNOW_BLOCK) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        if (!validateSession(player, EventType.SPLEEF, false, State.STARTED)) {
            return;
        }
        GameSession session = getSession(player);

        int trailOption = session.getModifierOption(Modifier.SPLEEF_TRAIL).getInteger();
        if (trailOption == 0) {
            return;
        }
        if (trailOption == 2 || (trailOption == 1 && CWUtil.randomFloat() <= 0.5f)) {
            destroyBlock(player, blockBelow, false);
            new BukkitRunnable() {
                @Override
                public void run() {
                    blockBelow.setType(Material.SNOW_BLOCK);
                }
            }.runTaskLater(events, 40);
        }
    }

    @EventHandler
    private void breakBlock(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SNOW_BLOCK) {
            return;
        }
        if (!validateSession((Player) event.getPlayer(), EventType.SPLEEF, false, State.STARTED)) {
            return;
        }
        event.setCancelled(false);
        destroyBlock(event.getPlayer(), block, true);
    }

    @EventHandler
    private void blockPlace(final BlockPlaceEvent event) {
        final Block block = event.getBlock();
        if (block.getType() != Material.TNT) {
            return;
        }

        if (!validateSession((Player) event.getPlayer(), EventType.SPLEEF, false, State.STARTED)) {
            return;
        }

        if (block.getRelative(BlockFace.DOWN).getType() != Material.SNOW_BLOCK) {
            CWUtil.sendActionBar(event.getPlayer(), "&4&l", "&cYou must place TNT on Snow!");
            return;
        }
        event.setCancelled(false);
        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(Material.AIR);
                GameSession session = getSession(event.getPlayer());
                if (session != null && session.isStarted()) {
                    destroySnowInRadius(event.getPlayer(), block.getRelative(BlockFace.DOWN).getLocation(), 3);
                    ParticleEffect.EXPLOSION_LARGE.display(0.5f, 1f, 0.5f, 0, 1, event.getBlockPlaced().getLocation().add(0, 1, 0));
                    block.getWorld().playSound(block.getLocation(), Sound.EXPLODE, 1, 2);
                    new CWItem(new CWItem(Material.TNT).setName("&c&lTnT").setLore(new String[] {"&7Throw it or place it to break blocks!"})).giveToPlayer(event.getPlayer());
                }
            }
        }.runTaskLater(events, 10);
    }

    @EventHandler
    private void projectileHitBlock(ProjectileHitBlockEvent event) {
        if (!(event.getProjectile().getShooter() instanceof Player)) {
            return;
        }
        if (validateSession((Player)event.getProjectile().getShooter(), EventType.SPLEEF, false, State.STARTED)) {
            Location blockCenter = event.getBlock().getLocation().getBlock().getLocation().add(0.5f, 0.5f, 0.5f);
            destroySnowInRadius((Player)event.getProjectile().getShooter(), blockCenter, 2);
        }
    }

    @EventHandler
    private void interact(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.SNOW_BALL) {
            return;
        }
        if (validateSession((Player) event.getPlayer(), EventType.SPLEEF, false, State.STARTED)) {
            event.setCancelled(false);
        }
    }

    private void destroySnowInRadius(Player player, Location blockCenter, int radius) {
        for (double x = blockCenter.getBlockX() - radius; x < blockCenter.getBlockX() + radius; x++) {
            for (double z = blockCenter.getBlockZ() - radius; z < blockCenter.getBlockZ() + radius; z++) {
                Block block = blockCenter.getWorld().getBlockAt((int) x, blockCenter.getBlockY(), (int) z);
                if (block.getType() != Material.SNOW_BLOCK) {
                    continue;
                }
                double distance = block.getLocation().distance(blockCenter);
                if (distance > radius) {
                    continue;
                }
                if (distance > radius-1  && CWUtil.randomFloat() > 0.5f) {
                    continue;
                }

                destroyBlock(player, block, false);
            }
        }
    }

    private void destroyBlock(Player player, Block block, boolean giveSnowball) {
        block.setTypeIdAndData(9, (byte) 7, false);

        if (giveSnowball && player != null && CWUtil.randomFloat() <= 0.1f) {
            new CWItem(Material.SNOW_BALL, 1).giveToPlayer(player);
        }
    }

}
