package com.clashwars.events.events._snake;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.packet.ParticleEffect;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.cwcore.utils.Prefix;
import com.clashwars.events.events.BaseEvent;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.State;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Snake extends BaseEvent {

    public Snake() {
        super();
        displayName = "&b&lSnake";
        menuSlot = 2;
        menuItem = new CWItem(Material.SKULL_ITEM).setSkullOwner("KylexDavis");
        setupOptions.add(new SetupOption(SetupType.CUBOID, "floor", "Area where apples will spawn. Should be one block above the floor"));
        setupModifiers("SNAKE_");

        new BukkitRunnable() {
            @Override
            public void run(){
                HashMap<Integer, GameSession> sessions = events.sm.getSessions();
                for (GameSession session : sessions.values()) {
                    if (session.getType() != EventType.SNAKE) {
                        continue;
                    }
                    if (!session.isStarted()) {
                        continue;
                    }
                    SnakeSession snakeSession = (SnakeSession)session;

                    Cuboid cuboid = session.getMap().getCuboid("floor");
                    Location location = cuboid.getMinLoc();
                    location.add(CWUtil.random(0, cuboid.getWidth()), CWUtil.random(0, cuboid.getHeight()) - 1, CWUtil.random(0, cuboid.getLength()));

                    List<Item> apples = new ArrayList<Item>(snakeSession.apples);
                    for (Item apple : apples) {
                        if (apple != null && apple.isValid()) {
                            ParticleEffect.REDSTONE.display(0.2f, 0.2f, 0.2f, 1, 10, apple.getLocation().add(0,1,0), 600);
                        } else {
                            snakeSession.apples.remove(apple);
                        }
                    }
                }
            }
        }.runTaskTimer(events, 0, 10);

        new BukkitRunnable() {
            @Override
            public void run(){
                HashMap<Integer, GameSession> sessions = events.sm.getSessions();
                for (GameSession session : sessions.values()) {
                    if (session.getType() != EventType.SNAKE) {
                        continue;
                    }
                    if (!session.isStarted()) {
                        continue;
                    }
                    SnakeSession snakeSession = (SnakeSession)session;

                    List<Player> players = session.getAllOnlinePlayers(false);
                    for (Player player : players) {
                        if (!snakeSession.moveLocations.containsKey(player.getUniqueId())) {
                            snakeSession.moveLocations.put(player.getUniqueId(), player.getLocation().toVector());
                            continue;
                        }
                        Vector prevLoc = snakeSession.moveLocations.get(player.getUniqueId());
                        Vector playerLoc = player.getLocation().getBlock().getLocation().toVector();

                        if (prevLoc.getBlockX() == playerLoc.getBlockX() && prevLoc.getBlockZ() == playerLoc.getBlockZ()) {
                            if (snakeSession.penalityPoints.containsKey(player.getUniqueId())) {
                                snakeSession.penalityPoints.put(player.getUniqueId(), snakeSession.penalityPoints.get(player.getUniqueId()) + 1);
                                if (snakeSession.penalityPoints.get(player.getUniqueId()) >= 5) {
                                    session.broadcast("&3&l" + player.getName() + " &bstood still too long!", true);
                                    removePlayer(snakeSession, player);
                                    continue;
                                }
                            } else {
                                snakeSession.penalityPoints.put(player.getUniqueId(), 1);
                            }
                            CWUtil.sendActionBar(player, "&4&l", "&cDon't stand still!!! &c&l" + snakeSession.penalityPoints.get(player.getUniqueId()) + "&8&l/&4&l5 &cpenalty points!");
                        }
                        snakeSession.moveLocations.put(player.getUniqueId(), player.getLocation().toVector());
                    }
                }
            }
        }.runTaskTimer(events, 0, 20);
    }

    @EventHandler
    private void itemPickup(PlayerPickupItemEvent event) {
        final Player player = event.getPlayer();
        final Location itemLoc = event.getItem().getLocation();
        if (!validateSession(player, EventType.SNAKE, false, State.STARTED)) {
            return;
        }
        event.setCancelled(false);

        final SnakeSession session = (SnakeSession)getSession(player);
        session.getBoard().setScore(DisplaySlot.SIDEBAR, player.getName(), session.getBoard().getScore(DisplaySlot.SIDEBAR, player.getName()) + 2);

        ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.APPLE, (byte)0), 0.3f, 0.8f, 0.3f, 0.1f, 100, event.getPlayer().getLocation().add(0,1,0), 500);
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.EAT, 2, 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                Cuboid floor = session.getMap().getCuboid("floor");;
                Location loc = floor.getMinLoc();
                loc.add(CWUtil.randomFloat(0, floor.getWidth()), 1, CWUtil.randomFloat(0, floor.getLength()));

                Item drop = CWUtil.dropItemStack(loc, new CWItem(Material.APPLE).setName("&c&lApple"));
                drop.setVelocity(new Vector(0,0,0));
                drop.setMetadata("permanent", new FixedMetadataValue(events, true));
                session.apples.add(drop);
            }
        }.runTaskLater(events, 60);
    }

    @EventHandler
    private void move(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        Player player = event.getPlayer();
        CWPlayer cwp = getCWPlayer(player);
        if (!validateSession(event.getPlayer(), EventType.SNAKE, false, State.STARTED)) {
            return;
        }
        SnakeSession session = (SnakeSession)getSession(cwp);

        List<Vector> prevLocations = new ArrayList<Vector>();
        if (session.prevLocations.containsKey(player.getUniqueId())) {
            prevLocations = session.prevLocations.get(player.getUniqueId());
        }

        Block to = event.getTo().getBlock();
        if (to.getType() != Material.AIR) {
            Prefix prefix = Prefix.fromColor(to.getData());
            Set<Team> teams = session.getBoard().getTeams();
            for (Team team : teams) {
                if (CWUtil.stripAllColor(team.getDisplayName()).equalsIgnoreCase(prefix.name)) {
                    if (team.getPlayers().isEmpty()) {
                        return;
                    }
                    OfflinePlayer ofPlayer = team.getPlayers().iterator().next();
                    if (player.getName().equalsIgnoreCase(ofPlayer.getName())) {
                        if ((prevLocations.size() > 0 && !prevLocations.get(0).equals(to.getLocation().toVector())) && (prevLocations.size() > 1 && !prevLocations.get(1).equals(to.getLocation().toVector()))) {
                            session.broadcast("&3&l" + player.getName() + " &bhit his own tail!", true);
                            removePlayer(session, player);
                        }
                    } else {
                        session.broadcast("&3&l" + player.getName() + " &bhit " + team.getPrefix() + "&l" + ofPlayer.getName() + "'s &btail!", true);
                        removePlayer(session, player);
                        return;
                    }
                }
            }
        }

        int length = session.getBoard().getScore(DisplaySlot.SIDEBAR, player.getName());
        Team team = session.getBoard().getBukkitBoard().getPlayerTeam(player);

        if (team == null || team.getDisplayName() == null) {
            return;
        }
        Prefix prefix = Prefix.fromString(CWUtil.stripAllColor(team.getDisplayName()));
        if (prefix == null) {
            return;
        }
        byte color = prefix.colorData;

        prevLocations.add(0, to.getLocation().toVector());
        session.prevLocations.put(player.getUniqueId(), prevLocations);

        if (prevLocations.size() > length) {
            Vector v = prevLocations.get(prevLocations.size()-1);
            to.getWorld().getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ()).setType(Material.AIR);
            prevLocations.remove(prevLocations.size()-1);
        }

        for (int i = 0; i < length && i < prevLocations.size(); i++) {
            Vector v = prevLocations.get(i);
            to.getWorld().getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ()).setTypeIdAndData(171, color, false);
        }
    }

    public void removePlayer(SnakeSession session, Player player) {
        if (session.prevLocations.containsKey(player.getUniqueId())) {
            for (Vector v : session.prevLocations.get(player.getUniqueId())) {
                player.getWorld().getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ()).setType(Material.AIR);
            }
        }

        session.setPotentialWinners(session.getAllPlayers(false));
        session.removePotentialWinner(player.getUniqueId());
        session.getBoard().resetScore(player.getName());
        session.switchToSpectator(player);
    }

}
