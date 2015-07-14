package com.clashwars.events.listeners;

import com.clashwars.cwcore.debug.Debug;
import com.clashwars.events.Events;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

public class ProtectionListener implements Listener {

    private Events events;

    public ProtectionListener(Events events) {
        this.events = events;
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void entityDamage(EntityDamageEvent event) {
        event.setCancelled(true);

        if (event.getEntity() instanceof Player) {
            ItemStack[] armorItems = ((Player) event.getEntity()).getInventory().getArmorContents();
            for (ItemStack armor : armorItems) {
                armor.setDurability((short) 0);
            }

            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                ((Player) event.getEntity()).setFireTicks(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void inventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void blockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void blockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void itemDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void itemPickup(PlayerPickupItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void explosion(ExplosionPrimeEvent event) {
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    private void interact(PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setUseInteractedBlock(Event.Result.DENY);
        if (event.getItem() != null && event.getItem().getType().getMaxDurability() > 0) {
            event.getItem().setDurability((short) 0);
        }

        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
        }

        if (event.getItem() != null && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            Material type = event.getItem().getType();

            if (type == Material.PAINTING || type == Material.SNOW_BALL || type == Material.FIREBALL || type == Material.ENDER_PEARL || type == Material.EXP_BOTTLE || type == Material.ITEM_FRAME
                    || type == Material.MINECART || type == Material.COMMAND_MINECART || type == Material.EXPLOSIVE_MINECART || type == Material.HOPPER_MINECART || type == Material.POWERED_MINECART
                    || type == Material.STORAGE_MINECART || type == Material.BOAT || type == Material.EGG || type == Material.EYE_OF_ENDER || type == Material.FISHING_ROD || type == Material.MONSTER_EGG
                    || type == Material.ARMOR_STAND || type == Material.BANNER || type == Material.FIREWORK) {
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                event.getPlayer().updateInventory();
            }

            if (type == Material.POTION && event.getItem().getDurability() >= 16385) {
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
                event.getPlayer().updateInventory();
            }
        }
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void projLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player && ((Player)event.getEntity().getShooter()).getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void itemConsume(PlayerItemConsumeEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void interactEntity(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        //Allow shearing mobs (Handled by shear event)
        if (event.getPlayer().getItemInHand().getType() == Material.SHEARS) {
            return;
        }
        //Allow leashing mobs (Handled by leash event)
        if (event.getPlayer().getItemInHand().getType() == Material.LEASH) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void shearEntity(PlayerShearEntityEvent event) {
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void leashEntity(PlayerLeashEntityEvent event) {
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void bedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void vehicleDamage(VehicleDamageEvent event) {
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void bucketFill(PlayerBucketFillEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void bucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void portalUse(PlayerPortalEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void weatherChance(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void armorstandEdit(PlayerArmorStandManipulateEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void blockFade(BlockFadeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void blockGrow(BlockGrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void leaveDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void foodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        event.setFoodLevel(20);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void blockSpread(BlockSpreadEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void blockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private void hangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player && ((Player)event.getRemover()).getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }


}
