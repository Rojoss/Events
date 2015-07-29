package com.clashwars.events.abilities.race;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.abilities.BaseAbility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class FishingRod extends BaseAbility {

    public FishingRod() {
        super();
        ability = Ability.FISHINGROD;
        displayName = "&c&lFishingRod";
        description = new String[] {"Pull a player back to you!"};
        usage = new String[] {"Aim at a player and right click!"};
        castItem = new CWItem(Material.FISHING_ROD, 1, (short)0, displayName);
    }

    @EventHandler
    public void fishingRodEvent(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY){
            return;
        }
        if (event.getCaught() instanceof Player) {
            final Player p = event.getPlayer();
            Vector dir = p.getLocation().getDirection();
            dir = dir.multiply(-1);
            event.getCaught().setVelocity(new Vector(dir.getX(), 0.5, dir.getZ()));
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.getInventory().setItem(0, new ItemStack(Material.AIR));
                    p.updateInventory();
                }
            }.runTaskLater(events, 1);
        }
    }

}
