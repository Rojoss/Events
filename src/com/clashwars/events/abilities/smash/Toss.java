package com.clashwars.events.abilities.smash;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.packet.ParticleEffect;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.abilities.BaseAbility;
import com.clashwars.events.damage.AbilityDmg;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

public class Toss extends BaseAbility {

    public Toss() {
        super();
        ability = Ability.TOSS;
        displayName = "&c&lToss";
        description = new String[] {"Toss a player in the air."};
        usage = new String[] {"Right click on a player to toss him."};
        castItem = new CWItem(Material.IRON_INGOT, 1, (short)0, displayName);
    }


    @EventHandler
    public void onEntityRightClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        Player player = event.getPlayer();
        Player target = (Player) event.getRightClicked();
        if (!canCast(player) || !isCastItem(player.getItemInHand())) {
            return;
        }

        if (onCooldown(player)) {
            return;
        }

        new AbilityDmg(target, 10, ability, player);
        target.setVelocity(new Vector(0, 1.5f, 0));
        player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 1, 0.6f);
        ParticleEffect.CLOUD.display(0.3f, 0.5f, 0.3f, 0, 20, target.getLocation());
        CWUtil.removeItemsFromHand(player, 1);
    }
}
