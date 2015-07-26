package com.clashwars.events.abilities.race;

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

public class Swap extends BaseAbility {

    public Swap() {
        super();
        ability = Ability.SWAP;
        displayName = "&c&lSwap";
        description = new String[] {"Swap positions with a player!"};
        usage = new String[] {"Aim at a player and right click!"};
        castItem = new CWItem(Material.EYE_OF_ENDER, 1, (short)0, displayName);
    }


}
