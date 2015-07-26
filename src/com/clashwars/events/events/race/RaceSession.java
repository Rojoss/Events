package com.clashwars.events.events.race;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.dependencies.CWWorldEdit;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.packet.ParticleEffect;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.SessionData;
import com.clashwars.events.modifiers.Modifier;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.util.io.file.FilenameException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RaceSession extends GameSession {

    public List<Item> powerups = new ArrayList<Item>();

    public RaceSession(SessionData data, boolean loadedFromConfig) {
        super(data, loadedFromConfig);
        session = this;
        maxTime = 600;
    }

    @Override
    public void start() {
        super.start();
        Cuboid beginWall = getMap().getCuboid("beginWall");
        File saveDir = new File(events.getDataFolder(), "schematics");
        saveDir.mkdir();
        try {
            CWWorldEdit.saveSchematic(beginWall.getMinLoc(), beginWall.getMaxLoc(), new File(saveDir, "race-wall-" + getMap().getName()));
        } catch (FilenameException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        }

        List<Block> blocks = beginWall.getBlocks();
        for (Block block : blocks) {
            block.setType(Material.AIR);
        }

        for (Cuboid cuboid : getMap().getMultiCuboids("powerUps").values()) {
            int xSize = cuboid.getWidth();
            int zSize = cuboid.getLength();

            List<Location> spawnLocs = new ArrayList<Location>();
            if (xSize < zSize) {
                float distance = (float)zSize / 4;
                spawnLocs.add(new Location(cuboid.getWorld(), cuboid.getMinX() + 0.5f, cuboid.getMinY() + 0.5f, cuboid.getMinZ() + distance / 2));
                spawnLocs.add(new Location(cuboid.getWorld(), cuboid.getMinX() + 0.5f, cuboid.getMinY() + 0.5f, cuboid.getMinZ() + (distance / 2) + distance));
                spawnLocs.add(new Location(cuboid.getWorld(), cuboid.getMinX() + 0.5f, cuboid.getMinY() + 0.5f, cuboid.getMinZ() + (distance / 2) + distance * 2));
                spawnLocs.add(new Location(cuboid.getWorld(), cuboid.getMinX() + 0.5f, cuboid.getMinY() + 0.5f, cuboid.getMinZ() + (distance / 2) + distance * 3));
            } else {
                float distance = (float)xSize / 4;
                spawnLocs.add(new Location(cuboid.getWorld(), cuboid.getMinX() + distance / 2, cuboid.getMinY() + 0.5f, cuboid.getMinZ() + 0.5f));
                spawnLocs.add(new Location(cuboid.getWorld(), cuboid.getMinX() + (distance / 2) + distance, cuboid.getMinY() + 0.5f, cuboid.getMinZ() + 0.5f));
                spawnLocs.add(new Location(cuboid.getWorld(), cuboid.getMinX() + (distance / 2) + distance * 2, cuboid.getMinY() + 0.5f, cuboid.getMinZ() + 0.5f));
                spawnLocs.add(new Location(cuboid.getWorld(), cuboid.getMinX() + (distance / 2) + distance * 3, cuboid.getMinY() + 0.5f, cuboid.getMinZ() + 0.5f));
            }

            for (Location loc : spawnLocs) {
                Item drop = CWUtil.dropItemStack(loc, new CWItem(Material.WOOD_BUTTON).setName("&0Powerup"));
                drop.setVelocity(new Vector(0,0,0));
                drop.setMetadata("permanent", new FixedMetadataValue(events, true));
                powerups.add(drop);
            }
        }

    }

    @Override
    public void teleportPlayer(Player player) {
        super.teleportPlayer(player);
        int speed = getModifierOption(Modifier.RACE_SPEED).getInteger();
        if (speed == 0) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 9999999, speed));

    }


    @Override
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        for (Item item : powerups) {
            item.remove();
        }
        Cuboid beginWall = getMap().getCuboid("beginWall");
        File saveDir = new File(events.getDataFolder(), "schematics");
        saveDir.mkdir();
        try {
            CWWorldEdit.loadSchematic(new File(saveDir, "race-wall-" + getMap().getName()), beginWall.getMinLoc());
        } catch (FilenameException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
        delete();
        return true;
    }
}