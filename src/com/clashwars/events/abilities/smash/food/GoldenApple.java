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

public class GoldenApple extends BaseAbility {

    public GoldenApple() {
        super();
        ability = Ability.GOLDEN_APPLE;
        displayName = "&6&lGolden Apple";
        description = new String[] {"Restores &a&l100 &7&odamage over time."};
        castItem = new CWItem(Material.GOLDEN_APPLE, 1, (short)1, displayName);
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
                    player.playSound(player.getLocation(), Sound.BURP, 1, 0.8f);
                    cancel();
                    return;
                }
                i++;
                player.setLevel(Math.max(player.getLevel() - 20, 0));
                ParticleEffect.HEART.display(0.3f, 0.3f, 0.3f, 0.1f, 5, player.getLocation().add(0, 2, 0));
                ParticleEffect.ITEM_CRACK.display(new ParticleEffect.ItemData(Material.GOLDEN_APPLE, (byte)1), 0.5f, 0.5f, 0.5f, 0.1f, 20, player.getLocation().add(0,1,0));
                player.playSound(player.getLocation(), Sound.EAT, 1, 0.6f);
            }
        }.runTaskTimer(events, 0, 4);
        CWUtil.removeItemsFromHand(player, 1);
    }

    @EventHandler
    public void interact(DelayedPlayerInteractEvent event) {
        super.interact(event);
    }
}
