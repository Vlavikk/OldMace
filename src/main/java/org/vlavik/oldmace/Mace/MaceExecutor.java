package org.vlavik.oldmace.Mace;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.vlavik.oldmace.Managers.MaceManager;
import org.vlavik.oldmace.OldMace;
import org.vlavik.oldmace.Utils.LocationUtils;
import org.vlavik.oldmace.Utils.ParticleUtils;

import java.util.Objects;
import java.util.function.Predicate;

public class MaceExecutor implements Listener {
    private final MaceManager maceManager = OldMace.getMaceManager();

    @EventHandler
    private void onDamage(EntityDamageByEntityEvent e){
        if(e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        if (e.getDamager() instanceof LivingEntity && e.getEntity() instanceof LivingEntity){
            LivingEntity attacker = (LivingEntity) e.getDamager();
            LivingEntity victim = (LivingEntity) e.getEntity();
            ItemStack activeAttackerItem = attacker.getEquipment().getItemInMainHand();
            if (attacker instanceof Player){
                Player playerAttacker = (Player) attacker;
                activeAttackerItem = playerAttacker.getInventory().getItemInMainHand();
            }
            if (activeAttackerItem == null || activeAttackerItem.getType() == Material.AIR || !maceManager.isMace(activeAttackerItem)) return;

            if (maceManager.canSmashAttack(attacker)){
                Location attackerLocation = attacker.getLocation();
                double bonusAttackDamage = maceManager.getAttackDamageBonus(attacker);
                double finaleDamage = (e.getDamage() + (bonusAttackDamage * (ExecutorUtils.isCritical(attacker) ? 1.5 : 1)));
                e.setDamage(finaleDamage);

                //Спавнит партиклы, по идее должна быть проверка на то стоит ли моб на земле, но она плохо работает(victim.isOnGround())
                Block downBlockVictim = LocationUtils.getOnPos(victim);
                ParticleUtils.spawnSmashAttackParticles(downBlockVictim,750);

                //Звуки удара булавы, отражает оригинальный код, хотя по факту в игре на 1.21 звуки как будто вообще рандомные, хз короче
                String soundKey;
                if (downBlockVictim.getType() == Material.AIR){
                    soundKey = "item.mace.smash_air";
                }else {
                    soundKey = attacker.getFallDistance() > 5 ? "item.mace.smash_ground_heavy" : "item.mace.smash_ground";
                }
                Sound sound = Sound.sound(Key.key(soundKey),
                        attacker instanceof Player ? Sound.Source.PLAYER : Sound.Source.NEUTRAL,
                        1,1);
                attacker.playSound(sound,attackerLocation.getX(),attackerLocation.getY(),attackerLocation.getZ());

                pushbackAttacker(attacker);
                knockbackNearbyMobs(attacker,victim);

                maceManager.applyDamageMace(activeAttackerItem,attacker,1);
                attacker.setFallDistance(0);
            }
        }
    }

    private void pushbackAttacker(LivingEntity attacker){
        Vector currentVelocity  = attacker.getVelocity();
        attacker.setVelocity(new Vector(currentVelocity.getX(),0.01,currentVelocity.getZ()));
    }

    private void knockbackNearbyMobs(LivingEntity attacker, LivingEntity victim){
        BoundingBox expandBox = victim.getBoundingBox().expand(MaceManager.SMASH_ATTACK_KNOCKBACK_RADIUS);
        expandBox.getCenter().toLocation(victim.getWorld()).getNearbyLivingEntities(
                expandBox.getMaxX() - expandBox.getCenterX(),
                expandBox.getMaxY() - expandBox.getCenterY(),
                expandBox.getMaxZ() - expandBox.getCenterZ(),
                knockbackPredicate(attacker,victim)
        ).forEach(nearby ->{
            Vector vectorAttackerToNearby = nearby.getLocation().toVector().subtract(victim.getLocation().toVector());
            double knockbackPower = maceManager.getKnockbackPower(attacker, nearby, vectorAttackerToNearby);
            Vector knockbackVector = vectorAttackerToNearby.normalize().multiply(knockbackPower);

            nearby.setVelocity(
                    nearby.getVelocity().add(new Vector(knockbackVector.getX(),MaceManager.SMASH_ATTACK_KNOCKBACK_POWER,knockbackVector.getZ())));
        });
    }
    private static Predicate<LivingEntity> knockbackPredicate(final LivingEntity attacker, final LivingEntity victim) {
        return nearby -> {
            Player nearbyPlayer = nearby instanceof Player ? (Player) nearby : null;

            boolean notSpectator = nearbyPlayer == null || !(nearbyPlayer.getGameMode() == GameMode.SPECTATOR);
            boolean notPlayer = nearby != attacker && nearby != victim;
            boolean notAlliedToPlayer = !ExecutorUtils.isAlliedTo(attacker,nearby);
            boolean notTamedByPlayer = !ExecutorUtils.isTamed(attacker,nearby);
            boolean notArmorStand = !ExecutorUtils.isArmorStand(nearby);
            boolean withinRange = victim.getLocation().distanceSquared(nearby.getLocation()) <= Math.pow(MaceManager.SMASH_ATTACK_KNOCKBACK_RADIUS, 2.0);
            boolean notFlyingInCreative = !(nearby instanceof Player && nearbyPlayer.getGameMode() == GameMode.CREATIVE && nearbyPlayer.isFlying());


            return notSpectator && notPlayer && notAlliedToPlayer && notTamedByPlayer && notArmorStand && withinRange && notFlyingInCreative;
        };
    }

    private static class ExecutorUtils{
        public static boolean isAlliedTo(LivingEntity entity, LivingEntity target) {
            if (entity == null || target == null) return false;
            if (entity.equals(target)) return true;

            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team1 = getEntityTeam(scoreboard, entity);
            Team team2 = getEntityTeam(scoreboard, target);

            return team1 != null && team2 != null && team1.equals(team2);
        }

        private static Team getEntityTeam(Scoreboard scoreboard, Entity entity) {
            String entry = (entity instanceof Player)
                    ? entity.getName()
                    : entity.getUniqueId().toString();

            return scoreboard.getEntryTeam(entry);
        }

        public static boolean isTamed(LivingEntity attacker, LivingEntity nearby){
            if (nearby instanceof Tameable){
                Tameable tameable = (Tameable) nearby;
                return (tameable.isTamed() && Objects.equals(tameable.getOwner(), attacker));
            }
            return false;
        }
        public static boolean isArmorStand(LivingEntity nearby){
            if (nearby instanceof ArmorStand){
                ArmorStand armorStand = (ArmorStand) nearby;
                return armorStand.isMarker();
            }
            return false;
        }

        //Честно сказать костыль, не смог найти метод проверки крит урона на 1.16.5
        public static boolean isCritical(LivingEntity attacker) {
            return attacker.getFallDistance() > 0.0F
                    && !attacker.isOnGround()
                    && !attacker.isInsideVehicle()
                    && !attacker.hasPotionEffect(PotionEffectType.BLINDNESS)
                    && attacker.getLocation().getBlock().getType() != Material.LADDER
                    && attacker.getLocation().getBlock().getType() != Material.VINE;
        }
    }
}
