package org.vlavik.oldmace.Mace;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Strider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.vlavik.oldmace.Managers.MaceManager;
import org.vlavik.oldmace.OldMace;

import java.util.Map;

public class MaceFixer implements Listener {

    //Так как для булавы используется Удочка с наростом,
    // этот класс исключает возможность пользоваться Булавой как конской броней

    private static final MaceManager maceManager = OldMace.getMaceManager();

    //Что то накостылял, вроде работает
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);
        if (first == null || !maceManager.isMace(first)) return;
        if (second != null && !maceManager.isMace(second) && second.getType() != Material.ENCHANTED_BOOK) {
            event.setResult(null);
            return;
        }

        ItemStack result = first.clone();
        ItemMeta meta = result.getItemMeta();
        boolean hasChanges = false;
        boolean hasRename = false;

        if (second != null && maceManager.isMace(second)) {
            ItemMeta secondMeta = second.getItemMeta();

            int health1 = getCustomDurability(first);
            int health2 = getCustomDurability(second);

            if (health1 < MaceManager.MAX_MACE_DURABILITY || health2 < MaceManager.MAX_MACE_DURABILITY) {
                int bonus = (int) (MaceManager.MAX_MACE_DURABILITY * 0.12);
                int newHealth = health1 + health2 + bonus;
                if (newHealth > MaceManager.MAX_MACE_DURABILITY) newHealth = MaceManager.MAX_MACE_DURABILITY;

                setCustomDurability(meta, newHealth);
                hasChanges = true;
            }

            if (mergeEnchantments(meta, secondMeta.getEnchants())) {
                hasChanges = true;
            }
        }
        else if (second != null && second.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) second.getItemMeta();

            if (mergeEnchantments(meta, bookMeta.getStoredEnchants())) {
                hasChanges = true;
            }
            setCustomDurability(meta, getCustomDurability(first));
        }

        String renameText = inv.getRenameText();
        if (renameText != null && !renameText.isEmpty() && !renameText.equals(PlainComponentSerializer.plain().serialize(meta.displayName()))) {
            meta.displayName(CreateMaceSession.createMaceName(renameText).decoration(TextDecoration.ITALIC,true));
            hasRename = true;
        }

        if (!hasChanges && !hasRename) {
            event.setResult(null);
            return;
        }
        int baseCost = 0;
        if (meta instanceof Repairable) {
            Repairable repairable = (Repairable) meta;
            baseCost = repairable.hasRepairCost() ? repairable.getRepairCost() : 0;
            repairable.setRepairCost(baseCost * 2 + 1);
        }

        result.setItemMeta(meta);
        event.setResult(result);

        int cost = 0;
        if (hasRename) cost += 1;
        if (hasChanges) cost += baseCost;

        final int finaleCost = cost;
        Bukkit.getScheduler().runTask(OldMace.getInstance(), () ->{
            inv.setRepairCost(finaleCost);
        });
    }


    private boolean mergeEnchantments(ItemMeta targetMeta, Map<Enchantment, Integer> sourceEnchants) {
        boolean changed = false;
        for (Map.Entry<Enchantment, Integer> entry : sourceEnchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int sourceLevel = entry.getValue();
            int currentLevel = targetMeta.getEnchantLevel(enchant);

            if (currentLevel < sourceLevel) {
                targetMeta.addEnchant(enchant, sourceLevel, true);
                changed = true;
            } else if (currentLevel == sourceLevel && sourceLevel < enchant.getMaxLevel()) {
                targetMeta.addEnchant(enchant, sourceLevel + 1, true);
                changed = true;
            }
        }
        return changed;
    }

    private int getCustomDurability(ItemStack item) {
        if (!item.hasItemMeta()) return MaceManager.MAX_MACE_DURABILITY;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.getOrDefault(MaceManager.actualDurabilityKey, PersistentDataType.INTEGER, MaceManager.MAX_MACE_DURABILITY);
    }

    private void setCustomDurability(ItemMeta meta, int realHealth) {
        meta.getPersistentDataContainer().set(MaceManager.actualDurabilityKey, PersistentDataType.INTEGER, realHealth);

        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            int visualDamage = 100 - (int) Math.ceil((double) realHealth / 5.0);
            damageable.setDamage(visualDamage);
        }
    }
    @EventHandler
    public void onStriderTempt(EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Strider) {
            if (event.getReason() == EntityTargetEvent.TargetReason.TEMPT) {
                if (event.getTarget() instanceof Player) {
                    Player player = (Player) event.getTarget();
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    if (maceManager.isMace(mainHand) || maceManager.isMace(offHand)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onStriderBoost(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            ItemStack item = event.getItem();

            if (item != null && maceManager.isMace(item)) {
                if (player.getVehicle() instanceof Strider) {
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onStriderInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Strider) {
            Player player = event.getPlayer();
            // Получаем предмет в той руке, которой кликнули
            ItemStack item = player.getInventory().getItem(event.getHand());

            if (item != null && maceManager.isMace(item)) {
                event.setCancelled(true);
            }
        }
    }
}