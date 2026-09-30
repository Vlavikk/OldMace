package org.vlavik.oldmace.Mace;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.vlavik.oldmace.Managers.MaceManager;
import org.vlavik.oldmace.OldMace;

import java.util.*;

public class CreateMaceSession {
    private final MaceManager maceManager = OldMace.getMaceManager();

    private static final Material MACE_MATERIAL = Material.IRON_HORSE_ARMOR;
    private static final NamespacedKey NAMESPACED_KEY_MACE = new NamespacedKey(OldMace.getInstance(),"mace");

    private final ItemStack result;
    public CreateMaceSession() {
        result = logic();
    }

    private ItemStack logic(){
        ItemStack itemStack = new ItemStack(MACE_MATERIAL);
        itemStack.editMeta(meta -> {
            meta.displayName(Component.text("Булава")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC,false));

            meta.setCustomModelData(670001);
            meta.getPersistentDataContainer().set(NAMESPACED_KEY_MACE, PersistentDataType.STRING,"mace");

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addAttributeModifier(
                    Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier(
                            UUID.randomUUID(),
                            "mace_damage",
                            5,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlot.HAND
                    )
            );
            meta.addAttributeModifier(
                    Attribute.GENERIC_ATTACK_SPEED,
                    new AttributeModifier(
                            UUID.randomUUID(),
                            "mace_speed",
                            -3.4,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlot.HAND
                    )
            );
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));

            lore.add(Component.translatable("item.modifiers.mainhand")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC,false));

            lore.add(Component.text(" "+(int) MaceManager.DEFAULT_MACE_DAMAGE +" ")
                    .color(NamedTextColor.DARK_GREEN)
                    .decoration(TextDecoration.ITALIC,false)
                    .append(Component.translatable("attribute.name.generic.attack_damage"))
            );
            lore.add(Component.text(" "+ MaceManager.DEFAULT_MACE_SPEED +" ")
                    .color(NamedTextColor.DARK_GREEN)
                    .decoration(TextDecoration.ITALIC,false)
                    .append(Component.translatable("attribute.name.generic.attack_speed"))
            );

            meta.lore(lore);

        });

        return itemStack;
    }

    public ItemStack getResult() {
        return result;
    }

    public static Material getMaceMaterial() {
        return MACE_MATERIAL;
    }

    public static NamespacedKey getNamespacedKeyMace() {
        return NAMESPACED_KEY_MACE;
    }
}
