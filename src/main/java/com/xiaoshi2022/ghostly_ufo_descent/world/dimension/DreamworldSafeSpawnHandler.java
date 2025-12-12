package com.xiaoshi2022.ghostly_ufo_descent.world.dimension;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

@EventBusSubscriber(modid = GhostlyUFODescent.MODID)
public class DreamworldSafeSpawnHandler {

    public static final Identifier DREAM_WORLD = Identifier.parse("ghostly_ufo_descent:dream_world");

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        ResourceKey<Level> dimensionKey = event.getDimension();
        Identifier dimension = dimensionKey.identifier();

        if (dimension.equals(DREAM_WORLD)) {
            // 确保实体生成在安全位置
            Level level = entity.level();
            BlockPos spawnPos = findSafeSpawnPosition(level, entity.blockPosition());

            if (!spawnPos.equals(entity.blockPosition())) {
                entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5);
            }
        }
    }

    public static BlockPos findSafeSpawnPosition(Level level, BlockPos startPos) {
        // 在Y=60层寻找安全的生成点（适合梦镜维度的高度）
        int surfaceY = 60;
        BlockPos surfacePos = new BlockPos(startPos.getX(), surfaceY, startPos.getZ());

        // 检查当前位置是否安全
        if (isSafeSpawnPosition(level, surfacePos)) {
            return surfacePos;
        }

        // 如果当前位置不安全，在周围寻找安全位置
        for (int radius = 1; radius <= 15; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) == radius || Math.abs(z) == radius) {
                        BlockPos checkPos = surfacePos.offset(x, 0, z);
                        if (isSafeSpawnPosition(level, checkPos)) {
                            return checkPos;
                        }
                    }
                }
            }
        }

        // 如果找不到安全位置，强制在Y=70生成（更高的位置确保安全）
        return new BlockPos(startPos.getX(), 70, startPos.getZ());
    }

    private static boolean isSafeSpawnPosition(Level level, BlockPos pos) {
        // 检查脚下方块是否坚固
        BlockState feetBlock = level.getBlockState(pos);
        BlockState headBlock = level.getBlockState(pos.above());
        BlockState groundBlock = level.getBlockState(pos.below());

        // 安全条件：脚下是固体，头部是空气，站立位置是空气
        return groundBlock.isSolid() &&
                headBlock.isAir() &&
                feetBlock.isAir();
    }
}