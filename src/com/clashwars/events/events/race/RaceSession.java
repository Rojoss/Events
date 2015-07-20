package com.clashwars.events.events.race;

import com.clashwars.cwcore.cuboid.Cuboid;
import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.dependencies.CWWorldEdit;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.SessionData;
import com.clashwars.events.modifiers.Modifier;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.util.io.file.FilenameException;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RaceSession extends GameSession {

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
            CWWorldEdit.saveSchematic(beginWall.getMinLoc(), beginWall.getMaxLoc(), new File(saveDir, "race-wall"));
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
    }

    @Override
    public boolean reset() {
        if (!super.reset()) {
            return false;
        }
        Cuboid beginWall = getMap().getCuboid("beginWall");
        File saveDir = new File(events.getDataFolder(), "schematics");
        saveDir.mkdir();
        try {
            CWWorldEdit.loadSchematic(new File(saveDir, "race-wall"), beginWall.getMinLoc());
        } catch (FilenameException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
        return true;
    }
}