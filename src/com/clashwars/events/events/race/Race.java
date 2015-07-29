package com.clashwars.events.events.race;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.packet.ParticleEffect;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.events.*;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Race extends BaseEvent {

    public Race() {
        super();
        setupOptions.add(new SetupOption(SetupType.CUBOID, "finish", "Where the race ends. "));
        setupOptions.add(new SetupOption(SetupType.CUBOID, "beginWall", "The wall that blocks the players. "));
        setupOptions.add(new SetupOption(SetupType.MULTI_CUBOID, "powerUps", "Cuboid where powerups will spawn"));
        setupModifiers("RACE_");
        abilities.add(Ability.FISHINGROD);
        abilities.add(Ability.SWAP);
        abilities.add(Ability.RUSH);


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
                    RaceSession raceSession = (RaceSession)session;

                    List<Item> powerupList = new ArrayList<Item>(raceSession.powerups);
                    for (Item powerup : powerupList) {
                        if (powerup != null && powerup.isValid()) {
                            ParticleEffect.FIREWORKS_SPARK.display(0.2f, 0.2f, 0.2f, 0, 20, powerup.getLocation().add(0,1,0), 600);
                        } else {
                            raceSession.powerups.remove(powerup);
                        }
                    }

                    List<Player> onlinePlayers = session.getAllOnlinePlayers(false);
                    for (final Player p : onlinePlayers) {
                        final Location playerLoc = p.getLocation();
                        Block block = playerLoc.getBlock();
                        Block below = playerLoc.getBlock().getRelative(BlockFace.DOWN);
                        if (block.getType() == Material.FIRE || block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA ||
                                below.getType() == Material.AIR || below.getType() == Material.FIRE || below.getType() == Material.LAVA || below.getType() == Material.STATIONARY_LAVA) {
                            continue;
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                SessionData data = session.getData();
                                data.setEventData("player-loc-" + p.getName(), CWUtil.locToStringSimple(playerLoc));
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

        if (session.getData().hasEventData("player-loc-" + player.getName()) && !session.getData().getEventData("player-loc-" + player.getName()).isEmpty()) {
            event.setRespawnLocation(CWUtil.locFromStringSimple(session.getData().getEventData("player-loc-" + player.getName())));
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


    @EventHandler
    private void interact(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.FISHING_ROD) {
            return;
        }
        if (validateSession(event.getPlayer(), EventType.RACE, false, State.STARTED)) {
            event.setCancelled(false);
            event.setUseItemInHand(Event.Result.ALLOW);
        }
    }

    @EventHandler
    private void projLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() == null || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        if (validateSession((Player)event.getEntity().getShooter(), EventType.RACE, false, State.STARTED)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void itemPickup(PlayerPickupItemEvent event) {
        final Player player = event.getPlayer();
        final Location itemLoc = event.getItem().getLocation();
        if (!validateSession(player, EventType.RACE, false, State.STARTED)) {
            return;
        }
        ItemStack firstItem = player.getInventory().getItem(0);
        if (firstItem != null && firstItem.getType() != Material.AIR) {
            CWUtil.sendActionBar(player, "&4&l", "&cYou can only hold one powerup!");
            return;
        }
        event.setCancelled(false);
        
        final RaceSession session = (RaceSession)getSession(player);
        final List<CWItem> castItems = new ArrayList<CWItem>();
        for (Ability ability : getAbilities()) {
            castItems.add(ability.getAbilityClass().getCastItem());
        }
        
        new BukkitRunnable() {
            int count = 0;
            int itemID = 0;
            String msg = "";

            @Override
            public void run() {
                if (count >= 10) {
                    player.getInventory().setItem(0, CWUtil.random(getAbilities()).getAbilityClass().getCastItem());
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 2);
                    ParticleEffect.VILLAGER_HAPPY.display(0.3f, 0.1f, 0.3f, 0, 10, player.getLocation().add(0, 2f, 0));
                    cancel();
                    return;
                }
                msg += ".";
                player.getInventory().setItem(0, castItems.get(itemID).setName("&5&l" + msg));
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.3f, 2);
                ParticleEffect.FIREWORKS_SPARK.display(0.3f, 0.5f, 0.3f, 0, 1, player.getLocation().add(0,1,0));
                count++;
                itemID++;
                if (itemID >= castItems.size()) {
                    itemID = 0;
                }
            }
        }.runTaskTimer(events, 3, 3);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                Item drop = CWUtil.dropItemStack(itemLoc.add(0,0.5f,0), new CWItem(Material.WOOD_BUTTON).setName("&0Powerup"));
                drop.setVelocity(new Vector(0,0,0));
                drop.setMetadata("permanent", new FixedMetadataValue(events, true));
                session.powerups.add(drop);
            }
        }.runTaskLater(events, 100);
    }

    @EventHandler
    private void itemDrop(PlayerDropItemEvent event) {
        if (!validateSession(event.getPlayer(), EventType.RACE, false, State.STARTED)) {
            return;
        }
        if (event.getPlayer().getInventory().getHeldItemSlot() != 0) {
            return;
        }
        event.setCancelled(false);
        event.getItemDrop().remove();
    }

}