package com.xiaoshi2022.ghostly_ufo_descent.meteor.event;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import com.xiaoshi2022.ghostly_ufo_descent.meteor.entity.EntityMeteor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@EventBusSubscriber(modid = GhostlyUFODescent.MODID)
public class MeteorSpawnHandler {
    private static final int METEOR_CHECK_INTERVAL = 20 * 30; // 每30秒检查一次
    private static int tickCounter = 0;
    private static final double METEOR_CHANCE = 0.15; // 15%的几率生成陨石
    private static final int MAX_METEORS_PER_CHUNK = 1;

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        tickCounter++;
        if (tickCounter % METEOR_CHECK_INTERVAL != 0) {
            return;
        }

        // 检查是否有玩家在世界中
        List<ServerPlayer> players = (List<ServerPlayer>) event.getLevel().players();
        if (players.isEmpty()) {
            return;
        }

        // 检查陨石生成条件
        if (canSpawnMeteor(event.getLevel())) {
            spawnMeteor((ServerLevel) event.getLevel());
        }
    }

    private static boolean canSpawnMeteor(Level level) {
        RandomSource random = level.random;

        // 检查是否是主世界
        if (level.dimension() != Level.OVERWORLD) {
            return false;
        }

        // 检查是否是白天（可选）
        // if (level.isDay()) {
        //     return false;
        // }

        // 随机检查
        return random.nextDouble() < METEOR_CHANCE;
    }

    private static void spawnMeteor(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }

        // 随机选择一个玩家作为目标
        ServerPlayer targetPlayer = players.get(level.random.nextInt(players.size()));
        BlockPos playerPos = targetPlayer.blockPosition();

        // 生成陨石的位置（在玩家周围随机位置的高空）
        RandomSource random = level.random;
        int spawnDistance = 30 + random.nextInt(50); // 距离玩家30-80格
        double angle = random.nextDouble() * Math.PI * 2;

        double spawnX = playerPos.getX() + Math.cos(angle) * spawnDistance;
        double spawnZ = playerPos.getZ() + Math.sin(angle) * spawnDistance;
        double spawnY = level.getMaxY() + 50; // 在世界最高高度上方50格

        // 计算陨石的运动方向（朝向玩家）
        Vec3 direction = new Vec3(
                playerPos.getX() + 0.5 - spawnX,
                playerPos.getY() + 0.5 - spawnY,
                playerPos.getZ() + 0.5 - spawnZ
        ).normalize();

        // 陨石速度
        double speed = 0.8 + random.nextDouble() * 0.4;
        Vec3 motion = direction.scale(speed);

        // 确定陨石大小
        int size = 1 + random.nextInt(3); // 1-3

        // 创建陨石实体
        EntityMeteor meteor = new EntityMeteor(level, spawnX, spawnY, spawnZ, motion, size);
        level.addFreshEntity(meteor);

        GhostlyUFODescent.LOGGER.info("Meteor spawned at ({}, {}, {}) targeting player at ({}, {}, {})",
                spawnX, spawnY, spawnZ,
                playerPos.getX(), playerPos.getY(), playerPos.getZ());
    }
}