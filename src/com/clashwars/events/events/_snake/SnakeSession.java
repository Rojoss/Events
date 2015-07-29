package com.clashwars.events.events._snake;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.player.Vanish;
import com.clashwars.cwcore.scoreboard.Criteria;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.SessionData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SnakeSession extends GameSession {

    public List<Item> apples = new ArrayList<Item>();
    public HashMap<UUID, List<Vector>> prevLocations = new HashMap<UUID, List<Vector>>();
    public HashMap<UUID, Vector> moveLocations = new HashMap<UUID, Vector>();
    public HashMap<UUID, Integer> penalityPoints = new HashMap<UUID, Integer>();

    public SnakeSession(SessionData data, boolean loadedFromConfig) {
        super(data, loadedFromConfig);
        session = this;
        maxTime = 300;
    }

    @Override
    public void lock() {
        super.lock();

        Objective sidebarObj = board.addObjective("points-side", "&a&lSNAKE LENGTH", Criteria.DUMMY, DisplaySlot.SIDEBAR, true);

        List<UUID> playerList = getAllPlayers(false);
        for (UUID player : playerList) {
            String playerName = CWUtil.getName(player);
            board.setScore(DisplaySlot.SIDEBAR, playerName, 3);
        }

        setupTeams(1);
    }

    @Override
    public void start() {
        super.start();

        Cuboid floor = getMap().getCuboid("floor");;
        int players = getPlayerCount(false) - 1;
        for (int i = 0; i < players; i++) {
            Location loc = floor.getMinLoc();
            loc.add(CWUtil.randomFloat(0, floor.getWidth()), 1, CWUtil.randomFloat(0, floor.getLength()));

            Item drop = CWUtil.dropItemStack(loc, new CWItem(Material.APPLE).setName("&c&lApple"));
            drop.setVelocity(new Vector(0,0,0));
            drop.setMetadata("permanent", new FixedMetadataValue(events, true));
            apples.add(drop);
        }

        List<UUID> allPlayers = getAllPlayers(false);
        for (UUID uuid : allPlayers) {
            Vanish.vanish(uuid);
        }
    }

    @Override
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        for (Item apple : apples) {
            if (apple != null) {
                apple.remove();
            }
        }
        List<Block> blocks = session.getMap().getCuboid("floor").getBlocks();
        for (Block block : blocks) {
            if (block.getType() == Material.CARPET) {
                block.setType(Material.AIR);
            }
        }
        delete();
        return true;
    }

    @Override
    public void teleportPlayer(Player player) {
        super.teleportPlayer(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, -6));
    }

    @Override
    public void leave(OfflinePlayer player, boolean force) {
        super.leave(player, force);
        if (!prevLocations.containsKey(player.getUniqueId())) {
            return;
        }
        for (Vector v : prevLocations.get(player.getUniqueId())) {
            if (player.isOnline()) {
                ((Player)player).getWorld().getBlockAt(v.getBlockX(), v.getBlockY(), v.getBlockZ()).setType(Material.AIR);
            }
        }
    }

}
