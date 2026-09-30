package org.vlavik.oldmace.Utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Wall;
import org.bukkit.entity.LivingEntity;

public class LocationUtils {

    public static Block getOnPos(LivingEntity entity) {
        return getOnPos(entity, 1.0E-5f);
    }


     //Аналог NMS-метода getOnPos(float offset).
     //Возвращает блок, на котором стоит сущность (с учётом особенностей заборов/стен/калиток).
    private static Block getOnPos(LivingEntity entity, float offset) {
        Location loc = entity.getLocation();

        int x = (int) Math.floor(loc.getX());
        int y = (int) Math.floor(loc.getY() - offset);
        int z = (int) Math.floor(loc.getZ());

        Block candidate = loc.getWorld().getBlockAt(x, y, z);

        if (offset <= 1.0E-5f) return candidate;
        Block below = loc.getWorld().getBlockAt(x, y - 1, z);
        BlockData data = below.getBlockData();

        boolean isFence = data instanceof Fence;
        boolean isWall = data instanceof Wall;
        boolean isGate = data instanceof Gate;

        boolean shouldShift = (offset > 0.5f || !isFence) && !isWall && !isGate;

        if (shouldShift) return candidate;
        return loc.getWorld().getBlockAt(x, y, z);
    }
}
