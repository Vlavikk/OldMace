package org.vlavik.oldmace.Utils;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.vlavik.oldmace.Managers.MaceManager;

import java.util.concurrent.ThreadLocalRandom;

public class ParticleUtils {

    public static void spawnSmashAttackParticles(Block block, int count) {
        BlockData data = block.getBlockData();
        if (block.getType().isAir()) return;

        World world = block.getWorld();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        double cx = block.getX() + 0.5;
        double cy = block.getY() + 0.5 + 0.5;
        double cz = block.getZ() + 0.5;

        // Столб
        for (int i = 0; i < count / 3.0F; i++) {
            double x = cx + rnd.nextGaussian() / 2.0;
            double y = cy;
            double z = cz + rnd.nextGaussian() / 2.0;

            double xd = rnd.nextGaussian() * 0.2F;
            double yd = randomVerticalSpeed(rnd,0.2F);
            double zd = rnd.nextGaussian() * 0.2F;

            spawn(world, x, y, z, xd, yd, zd, data);
        }

        // Кольцо
        for (int i = 0; i < count / 1.5F; i++) {
            double x = cx + MaceManager.SMASH_ATTACK_KNOCKBACK_RADIUS * Math.cos(i) + rnd.nextGaussian() / 2.0;
            double y = cy;
            double z = cz + MaceManager.SMASH_ATTACK_KNOCKBACK_RADIUS * Math.sin(i) + rnd.nextGaussian() / 2.0;

            double xd = rnd.nextGaussian() * 0.05F;
            double yd = randomVerticalSpeed(rnd,0.05F);
            double zd = rnd.nextGaussian() * 0.05F;

            spawn(world, x, y, z, xd, yd, zd, data);
        }
    }

    private static void spawn(World world, double x, double y, double z,
                              double xd, double yd, double zd, BlockData data) {
        world.spawnParticle(Particle.BLOCK_DUST, x, y, z, 1, xd, yd, zd, 1, data);
    }
    private static double randomVerticalSpeed(ThreadLocalRandom rnd, float baseValue) {
        if (rnd.nextDouble() < 0.50) {
            return (1.0 + rnd.nextDouble() * 3.0) + Math.abs(rnd.nextGaussian() * baseValue);
        } else {
            return rnd.nextGaussian() * baseValue;
        }
    }
}
