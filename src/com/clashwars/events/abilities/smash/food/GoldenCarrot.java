package com.clashwars.events.abilities.smash.food;

import com.clashwars.cwcore.events.DelayedPlayerInteractEvent;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.packet.ParticleEffect;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.abilities.BaseAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

public class GoldenCarrot extends BaseAbility {

    public GoldenCarrot() {
        super();
        ability = Ability.GOLDEN_CARROT;
        displayName = "&6&lGolden Carrot";
        description = new String[] {"Restores &a&l50 &7&odamage over time."};
        castItem = new CWItem(Material.GOLDEN_CARROT, 1, (short)0, displayName);
    }

    @Override
    public void castAbility(final Player player, Location triggerLoc) {
        if (onCooldown(player)) {
            return;
        }

        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i > 5) {
                    player.playSound(player.getLocation(), Sound.BURP, 1, 0.9f);
                    cancel();
                    return;
                }
                i++;
                player.setLevel(Math.max(player.getLevel() - 10, 0));
                ParticleEffect.HEART.display(0.3f, 0.3f, 0.3f, 0.1f, 4, player.getLocation().add(0, 2, 0));
                ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.GOLDEN_CARROT, (byte)0), 0.5f, 0.5f, 0.5f, 0.1f, 20, player.getLocation().add(0,1,0));
                player.playSound(player.getLocation(), Sound.EAT, 1, 0.8f);
            }
        }.runTaskTimer(events, 0, 4);
        CWUtil.removeItemsFromHand(player, 1);
    }

    @EventHandler
    public void interact(DelayedPlayerInteractEvent event) {
        super.interact(event);
    }
}
