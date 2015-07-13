package com.clashwars.events.events.koh;

import com.clashwars.cwcore.helpers.CWItem;
import com.clashwars.events.events.GameSession;
import com.clashwars.events.setup.SetupOption;
import com.clashwars.events.setup.SetupType;
import com.clashwars.events.events.BaseEvent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class Koh extends BaseEvent {

    public Koh() {
        super();
        setupOptions.add(new SetupOption(SetupType.CUBOID, "hill", "Area where capturing is triggered."));

        equipment.add(new CWItem(Material.DIAMOND_SWORD, 1));
    }

    public List<CWItem> getEquipment(GameSession session) {
        List<CWItem> equipment = new ArrayList<CWItem>();
        equipment.add(new CWItem(Material.DIAMOND_HELMET));
        equipment.add(new CWItem(Material.DIAMOND_CHESTPLATE));
        equipment.add(new CWItem(Material.DIAMOND_LEGGINGS));
        equipment.add(new CWItem(Material.DIAMOND_BOOTS));

        equipment.add(new CWItem(Material.DIAMOND_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 3).addEnchant(Enchantment.KNOCKBACK, 1));
        equipment.add(new CWItem(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 3).addEnchant(Enchantment.ARROW_KNOCKBACK, 1).addEnchant(Enchantment.ARROW_INFINITE, 1));

        equipment.add(new CWItem(PotionType.INSTANT_HEAL, true, 3));
        equipment.add(new CWItem(Material.ARROW, 1).setSlot(9));
        return equipment;
    }

}
