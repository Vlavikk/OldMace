package org.vlavik.oldmace.Mace;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.vlavik.oldmace.Managers.MaceManager;
import org.vlavik.oldmace.OldMace;

public class MaceFixer implements Listener {

    //Так как для булавы используется Железная конская броня,
    // этот класс исключает возможность пользоваться Булавой как конской броней

    private static final MaceManager maceManager = OldMace.getMaceManager();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSmelt(FurnaceSmeltEvent event) {
        if (maceManager.isMace(event.getSource())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBurn(FurnaceBurnEvent event) {
        if (maceManager.isMace(event.getFuel())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMerchantClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.MERCHANT) {
            return;
        }
        if (maceManager.isMace(event.getCurrentItem()) || maceManager.isMace(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMerchantDrag(InventoryDragEvent event) {
        if (event.getInventory().getType() != InventoryType.MERCHANT) {
            return;
        }
        for (ItemStack item : event.getNewItems().values()) {
            if (maceManager.isMace(item)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        if (maceManager.isMace(first) || maceManager.isMace(second)) {
            event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHorseInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof AbstractHorse)) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (maceManager.isMace(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMoveItem(InventoryMoveItemEvent event) {
        if (!isFurnaceLike(event.getDestination().getType())) {
            return;
        }
        if (maceManager.isMace(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFurnaceClick(InventoryClickEvent event) {
        if (!isFurnaceLike(event.getView().getTopInventory().getType())) {
            return;
        }

        Inventory clicked = event.getClickedInventory();
        Inventory top = event.getView().getTopInventory();

        if (clicked != null && clicked.equals(top)) {
            if (maceManager.isMace(event.getCursor())) {
                event.setCancelled(true);
                return;
            }

            InventoryAction action = event.getAction();
            if (action == InventoryAction.HOTBAR_SWAP
                    || action == InventoryAction.HOTBAR_MOVE_AND_READD
                    || action == InventoryAction.SWAP_WITH_CURSOR) {
                ItemStack hotbarItem = event.getWhoClicked().getInventory()
                        .getItem(event.getHotbarButton());
                if (maceManager.isMace(hotbarItem)) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        if (event.isShiftClick() && maceManager.isMace(event.getCurrentItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFurnaceDrag(InventoryDragEvent event) {
        if (!isFurnaceLike(event.getView().getTopInventory().getType())) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        for (int slot : event.getRawSlots()) {
            if (slot >= top.getSize()) {
                continue;
            }
            for (ItemStack item : event.getNewItems().values()) {
                if (maceManager.isMace(item)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHorseInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof AbstractHorse)) {
            return;
        }

        Inventory clicked = event.getClickedInventory();
        Inventory top = event.getView().getTopInventory();

        if (clicked != null && clicked.equals(top)) {
            if (maceManager.isMace(event.getCursor())) {
                event.setCancelled(true);
                return;
            }

            InventoryAction action = event.getAction();
            if (action == InventoryAction.HOTBAR_SWAP
                    || action == InventoryAction.HOTBAR_MOVE_AND_READD
                    || action == InventoryAction.SWAP_WITH_CURSOR) {
                ItemStack hotbarItem = event.getWhoClicked().getInventory()
                        .getItem(event.getHotbarButton());
                if (maceManager.isMace(hotbarItem)) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        if (event.isShiftClick() && maceManager.isMace(event.getCurrentItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHorseInventoryDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof AbstractHorse)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        for (int slot : event.getRawSlots()) {
            if (slot >= top.getSize()) {
                continue;
            }
            for (ItemStack item : event.getNewItems().values()) {
                if (maceManager.isMace(item)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDispenseArmor(BlockPreDispenseEvent event) {
        ItemStack item = event.getItemStack();
        if (!maceManager.isMace(item)) return;

        Block dispenserBlock = event.getBlock();
        if (!(dispenserBlock.getBlockData() instanceof Directional)) {
            return;
        }

        Directional directional = (Directional) dispenserBlock.getBlockData();
        Block targetBlock = dispenserBlock.getRelative(directional.getFacing());
        Location targetLoc = targetBlock.getLocation().add(0.5, 0.5, 0.5);

        for (Entity entity : targetBlock.getWorld().getNearbyEntities(targetLoc, 0.7, 0.7, 0.7)) {
            if (entity instanceof Horse) event.setCancelled(true);
        }
    }

    private boolean isFurnaceLike(InventoryType type) {
        return type == InventoryType.FURNACE
                || type == InventoryType.BLAST_FURNACE
                || type == InventoryType.SMOKER;
    }
}