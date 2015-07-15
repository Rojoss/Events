package com.clashwars.events.events.koh;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.cwcore.utils.CWUtil;
import com.clashwars.events.events.EventType;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.events.State;
import com.clashwars.events.modifiers.IntModifierOption;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.player.CWPlayer;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import com.clashwars.events.events.BaseEvent;
import com.clashwars.events.util.Util;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Koh extends BaseEvent {

    public Koh() {
        super();
        setupOptions.add(new SetupOption(SetupType.CUBOID, "hill", "Area where capturing is triggered."));
        setupModifiers("KOH_");
    }

    public List<CWItem> getEquipment(GameSession session) {
        HashMap<Modifier, ModifierOption> modifierOptions = session.getModifierOptions();
        List<CWItem> equipment = new ArrayList<CWItem>();
        equipment.add(new CWItem(Material.DIAMOND_HELMET));
        equipment.add(new CWItem(Material.DIAMOND_CHESTPLATE));
        equipment.add(new CWItem(Material.DIAMOND_LEGGINGS));
        equipment.add(new CWItem(Material.DIAMOND_BOOTS));

        int knockback = modifierOptions.get(Modifier.KOH_KNOCKBACK).getInteger();
        if (knockback > 0) {
            equipment.add(new CWItem(Material.DIAMOND_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 3).addEnchant(Enchantment.KNOCKBACK, knockback));
            equipment.add(new CWItem(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 3).addEnchant(Enchantment.ARROW_KNOCKBACK, knockback).addEnchant(Enchantment.ARROW_INFINITE, 1));
        } else {
            equipment.add(new CWItem(Material.DIAMOND_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 3));
            equipment.add(new CWItem(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 3).addEnchant(Enchantment.ARROW_INFINITE, 1));
        }

        equipment.add(new CWItem(PotionType.INSTANT_HEAL, true, modifierOptions.get(Modifier.KOH_HEALTH_POTIONS).getInteger()));
        equipment.add(new CWItem(PotionType.INSTANT_DAMAGE, true, modifierOptions.get(Modifier.KOH_DAMAGE_POTIONS).getInteger()));
        equipment.add(new CWItem(Material.ARROW, 1).setSlot(9));
        return equipment;
    }


    @EventHandler
    private void death(PlayerDeathEvent event) {
        //TODO: Decrease lives.
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void respawn(PlayerRespawnEvent event) {
        CWPlayer cwp = getCWPlayer(event.getPlayer());

        GameSession session = cwp.getSession();
        if (session == null || session.getType() != EventType.KOH) {
            return;
        }

        //TODO: Put player in spectator if out of lives.

        CWUtil.resetPlayer(event.getPlayer(), GameMode.SURVIVAL);
        Util.equipItems(event.getPlayer(), getEquipment(session));

        Location loc = session.getTeleportLocation(cwp);
        Debug.bc(loc);
        event.setRespawnLocation(loc);
    }


    @EventHandler
    private void entityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE &&
                event.getCause() != EntityDamageEvent.DamageCause.FALL && event.getCause() != EntityDamageEvent.DamageCause.MAGIC) {
            return;
        }

        if (validateSession((Player)event.getEntity(), EventType.KOH, false, State.STARTED)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void projectileShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        if (validateSession((Player)event.getEntity().getShooter(), EventType.KOH, false, State.STARTED)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    private void interact(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.POTION) {
            return;
        }
        if (validateSession(event.getPlayer(), EventType.KOH, false, State.STARTED)) {
            event.setUseItemInHand(Event.Result.ALLOW);
            event.setCancelled(false);
        }
    }

}
