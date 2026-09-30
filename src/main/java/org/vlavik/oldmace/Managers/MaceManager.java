package org.vlavik.oldmace.Managers;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.vlavik.oldmace.Mace.CreateMaceSession;

public class MaceManager {
    public static final double DEFAULT_MACE_DAMAGE = 6;
    public static final double DEFAULT_MACE_SPEED = 0.6;
    public static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0F;
    public static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 3.5F;
    public static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.7F;


    public boolean isMace(ItemStack itemStack){
        if (itemStack == null) return false;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return false;
        String data = meta.getPersistentDataContainer().get(CreateMaceSession.getNamespacedKeyMace(), PersistentDataType.STRING);
        return data != null && data.equals("mace");
    }

    public double getAttackDamageBonus(LivingEntity attacker){
        if (!canSmashAttack(attacker)) {
            return 0.0F;
        }
        double fallHeightThreshold1 = 3.0;
        double fallHeightThreshold2 = 8.0;
        double fallDistance = attacker.getFallDistance();
        double damage;
        if (fallDistance <= fallHeightThreshold1) {
            damage = 4.0 * fallDistance;
        } else if (fallDistance <= fallHeightThreshold2) {
            damage = 12.0 + 2.0 * (fallDistance - fallHeightThreshold1);
        } else {
            damage = 22.0 + fallDistance - fallHeightThreshold2;
        }
        return damage;
    }

    public boolean canSmashAttack(final LivingEntity attacker) {
        return attacker.getFallDistance() > 1.5;
    }

    public double getKnockbackPower(final LivingEntity attacker, final LivingEntity nearby, final Vector direction) {
        AttributeInstance attributeInstance = nearby.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        double knockbackResistance = attributeInstance == null ? 1 : attributeInstance.getValue();
        return (SMASH_ATTACK_KNOCKBACK_RADIUS - direction.length()) * SMASH_ATTACK_KNOCKBACK_POWER * (attacker.getFallDistance() > SMASH_ATTACK_HEAVY_THRESHOLD ? 2 : 1) * (1.0 - knockbackResistance);
    }

    public ItemStack createMaceItemStack(){
        return new CreateMaceSession().getResult();
    }
}
