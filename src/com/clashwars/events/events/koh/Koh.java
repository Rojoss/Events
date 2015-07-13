package com.clashwars.events.events.koh;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.modifiers.IntModifierOption;
import com.clashwars.events.modifiers.Modifier;
import com.clashwars.events.modifiers.ModifierOption;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import com.clashwars.events.events.BaseEvent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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

}
