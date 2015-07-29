package com.clashwars.events.abilities.race;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.events.DelayedPlayerInteractEvent;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.packet.ParticleEffect;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.abilities.BaseAbility;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Rush extends BaseAbility {

    public Rush() {
        super();
        ability = Ability.RUSH;
        displayName = "&c&lRush";
        description = new String[] {"Receive a speed boost!"};
        usage = new String[] {"Right click the item!"};
        castItem = new CWItem(Material.SUGAR, 1, (short)0, displayName);
    }


    @Override
    public void castAbility(final Player player, Location triggerloc) {
        int amplifier = 1;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().getName().equalsIgnoreCase("SPEED")) {
                amplifier = effect.getAmplifier() + 2;
            }
        }
        CWUtil.stackPotionEffect(player, new PotionEffect(PotionEffectType.SPEED, 100, amplifier));

        ParticleEffect.SNOW_SHOVEL.display(0.3f, 0.3f, 0.3f, 0.2f, 50, player.getLocation().add(0, 2, 0));
        ParticleEffect.SNOWBALL.display(0.3f, 0.3f, 0.3f, 0.2f, 10, player.getLocation().add(0, 2, 0));
        player.playSound(player.getLocation(), Sound.DRINK, 1, 2f);
        player.getInventory().setItem(0, new ItemStack(Material.AIR));
    }

    @EventHandler
    public void interact(DelayedPlayerInteractEvent event) {
        super.interact(event);
    }

}
