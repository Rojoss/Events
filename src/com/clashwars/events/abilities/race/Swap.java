package com.clashwars.events.abilities.race;

import com.clashwars.cwcore.events.DelayedPlayerInteractEvent;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.packet.ParticleEffect;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.abilities.Ability;
import com.clashwars.events.abilities.BaseAbility;
import com.clashwars.events.damage.AbilityDmg;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

public class Swap extends BaseAbility {

    public Swap() {
        super();
        ability = Ability.SWAP;
        displayName = "&c&lSwap";
        description = new String[] {"Swap positions with a player!"};
        usage = new String[] {"Aim at a player and right click!"};
        castItem = new CWItem(Material.EYE_OF_ENDER, 1, (short)0, displayName);
    }

    @Override
    public void castAbility(final Player player, Location triggerloc) {

        final Player target = CWUtil.getTargetedPlayer(player, 10);

        if (target == null) {
            CWUtil.sendActionBar(player, CWUtil.integrateColor("&4&l>> &cLook at a player within 10 blocks range and click to use! &4&l<<"));
            return;
        }

        Location targetLoc = target.getLocation();
        Location playerLoc = player.getLocation();

        target.teleport(playerLoc);
        player.teleport(targetLoc);
        player.getWorld().playEffect(playerLoc, Effect.ENDER_SIGNAL, 1);
        player.getWorld().playEffect(targetLoc, Effect.ENDER_SIGNAL, 1);
        player.getWorld().playSound(playerLoc, Sound.ENDERMAN_TELEPORT, 0.5f, 0.6f);
        player.getWorld().playSound(targetLoc, Sound.ENDERMAN_TELEPORT, 1, 1.2f);
    }



    @EventHandler
    public void interact(DelayedPlayerInteractEvent event) {
        super.interact(event);
    }

}
