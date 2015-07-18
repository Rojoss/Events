package com.clashwars.events.events.koh;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.*;
import com.clashwars.events.modifiers.IntModifierOption;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import com.clashwars.events.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Koh extends BaseEvent {

    public Koh() {
        super();
        setupOptions.add(new SetupOption(SetupType.CUBOID, "hill", "Area where capturing is triggered."));
        setupModifiers("KOH_");

        new BukkitRunnable() {
            @Override
            public void run(){
                HashMap<Integer, GameSession> sessions = events.sm.getSessions();
                for (GameSession session : sessions.values()) {
                    if (session.getType() != EventType.KOH) {
                        continue;
                    }
                    if (!session.isStarted()) {
                        continue;
                    }

                    SessionData data =  session.getData();
                    boolean teams = session.getModifierOption(Modifier.KOH_TEAMS).getBoolean();

                    int defaultCaptureTime = 10;
                    if (teams) {
                        defaultCaptureTime = 20;
                        if (session.getBoard().getTeams().iterator().hasNext()) {
                            defaultCaptureTime = Math.min(session.getBoard().getTeams().iterator().next().getPlayers().size() * 10, 30);
                        }
                    }

                    int captureTime = CWUtil.getInt(data.getEventData("capture-time"));
                    if (captureTime == -1) {
                        captureTime = defaultCaptureTime;
                    }

                    List<String> prevPlayersOnHill = new ArrayList<String>();
                    if (data.hasEventData("on-hill") && !data.getEventData("on-hill").isEmpty()) {
                        prevPlayersOnHill = Arrays.asList(data.getEventData("on-hill").split(","));
                    }

                    List<String> playersOnHill = new ArrayList<String>();
                    List<Player> onlinePlayers = session.getAllOnlinePlayers(false);
                    for (Player player : onlinePlayers) {
                        if (session.getMap().getCuboid("hill").contains(player)) {
                            playersOnHill.add(player.getName());
                        }
                    }

                    if (playersOnHill.size() > 0) {
                        data.setEventData("on-hill", CWUtil.implode(playersOnHill, ","));
                        if (teams) {
                            Team capturingTeam = null;
                            int teamCount = 0;
                            int playerCount = 0;
                            for (String player : playersOnHill) {
                                Team team = session.getBoard().getBukkitBoard().getPlayerTeam(Bukkit.getOfflinePlayer(player));
                                if (capturingTeam == null) {
                                    capturingTeam = team;
                                    teamCount++;
                                    playerCount++;
                                } else if (!capturingTeam.getName().equalsIgnoreCase(team.getName())) {
                                    teamCount++;
                                    break;
                                } else {
                                    playerCount++;
                                }
                            }
                            if (teamCount > 1) {
                                session.broadcastBar("&2&l", "&a&lTimer has been reset!", true);
                                captureTime = defaultCaptureTime;
                            } else {
                                session.broadcastBar("&6&l", "&5&l" + captureTime + " &dseconds till team &d&l" + capturingTeam.getDisplayName() + " &dcaptures the hill!", true);
                                captureTime = Math.max(0, captureTime - playerCount);
                                if (captureTime == 0) {
                                    List<UUID> winners = new ArrayList<UUID>();
                                    for (OfflinePlayer player : capturingTeam.getPlayers()) {
                                        winners.add(player.getUniqueId());
                                    }
                                    session.end(winners);
                                    return;
                                }
                            }
                        } else {
                            if (playersOnHill.size() > 1 || (prevPlayersOnHill.size() == 1 && !playersOnHill.get(0).equalsIgnoreCase(prevPlayersOnHill.get(0)))) {
                                session.broadcastBar("&2&l", "&a&lTimer has been reset!", true);
                                captureTime = defaultCaptureTime;
                            } else {
                                if (captureTime == 0) {
                                    session.end(Bukkit.getOfflinePlayer(playersOnHill.get(0)).getUniqueId());
                                    return;
                                } else if (captureTime > 0) {
                                    session.broadcastBar("&6&l", "&5&l" + captureTime + " &dseconds till &d&l" + playersOnHill.get(0) + " &dcaptures the hill!", true);
                                    captureTime--;
                                }
                            }
                        }
                    } else {
                        data.setEventData("on-hill", "");
                        if (prevPlayersOnHill != null && prevPlayersOnHill.size() > 0) {
                            session.broadcastBar("&2&l", "&a&lNo more players on the hill!", true);
                            captureTime = defaultCaptureTime;
                        }
                    }

                    data.setEventData("capture-time", Integer.toString(captureTime));
                    session.setData(data);
                }
            }
        }.runTaskTimer(events, 0, 20);
    }

    public List<CWItem> getEquipment(GameSession session) {
        HashMap<Modifier, ModifierOption> modifierOptions = session.getModifierOptions();
        List<CWItem> equipment = new ArrayList<CWItem>();
        equipment.add(new CWItem(Material.DIAMOND_HELMET));
        equipment.add(new CWItem(Material.DIAMOND_CHESTPLATE));
        equipment.add(new CWItem(Material.DIAMOND_LEGGINGS));
        equipment.add(new CWItem(Material.DIAMOND_BOOTS));

        int knockback = modifierOptions.get(Modifier.KOH_KNOCKBACK).getInteger();
        if (knockback > 0) {
            equipment.add(new CWItem(Material.DIAMOND_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 3).addEnchant(Enchantment.KNOCKBACK, knockback));
            equipment.add(new CWItem(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 3).addEnchant(Enchantment.ARROW_KNOCKBACK, knockback).addEnchant(Enchantment.ARROW_INFINITE, 1));
        } else {
            equipment.add(new CWItem(Material.DIAMOND_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 3));
            equipment.add(new CWItem(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 3).addEnchant(Enchantment.ARROW_INFINITE, 1));
        }

        equipment.add(new CWItem(PotionType.INSTANT_HEAL, true, modifierOptions.get(Modifier.KOH_HEALTH_POTIONS).getInteger()));
        equipment.add(new CWItem(PotionType.INSTANT_DAMAGE, true, modifierOptions.get(Modifier.KOH_DAMAGE_POTIONS).getInteger()));
        equipment.add(new CWItem(Material.ARROW, 1).setSlot(9));
        return equipment;
    }


    @EventHandler
    private void death(PlayerDeathEvent event) {
        CWPlayer cwp = getCWPlayer(event.getEntity());
        if (!validateSession(event.getEntity(), EventType.KOH, false, State.STARTED)) {
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
        if (!validateSession(event.getPlayer(), EventType.KOH, false)) {
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
    }


    @EventHandler
    private void entityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE &&
                event.getCause() != EntityDamageEvent.DamageCause.FALL && event.getCause() != EntityDamageEvent.DamageCause.MAGIC) {
            return;
        }

        if (validateSession((Player)event.getEntity(), EventType.KOH, false, State.STARTED)) {
            event.setCancelled(false);
        }
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
    private void interact(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.POTION) {
            return;
        }
        if (validateSession(event.getPlayer(), EventType.KOH, false, State.STARTED)) {
            event.setUseItemInHand(Event.Result.ALLOW);
            event.setCancelled(false);
        }
    }

}
