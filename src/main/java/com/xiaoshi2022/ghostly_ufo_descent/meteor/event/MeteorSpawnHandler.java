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
    private static boolean hasMeteorSpawned = false; // 记录是否已经生成过陨石

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

        // 如果已经生成过陨石，则完全使用随机概率
        if (hasMeteorSpawned) {
            GhostlyUFODescent.LOGGER.info("Meteor has already spawned, using random chance: {}", METEOR_CHANCE);
            return random.nextDouble() < METEOR_CHANCE;
        }

        // 获取所有玩家
        List<? extends Player> players = level.players();
        
        // 检查是否有玩家在生存模式下待了4天（4天 = 4 * 24 * 72000 = 6912000 游戏刻）
        for (Player player : players) {
            if (!player.isCreative() && !player.isSpectator()) { // 只检查生存模式玩家
                // 使用player.experienceLevel作为简单的游戏时间指标，或者使用其他方式
                // 这里我们假设玩家达到一定等级意味着他们已经玩了足够长的时间
                // 或者我们可以简单地使用玩家的游戏天数
                int playerDays = (int)(level.getGameTime() / 24000); // 获取世界的天数
                if (playerDays >= 4) { // 如果世界已经存在4天或以上
                    GhostlyUFODescent.LOGGER.info("World has reached 4 days, triggering first meteor spawn.");
                    return true; // 直接返回true，触发陨石生成
                }
            }
        }

        // 如果没有玩家达到4天生存时间，则使用随机概率
        return random.nextDouble() < METEOR_CHANCE;
    }

    private static void spawnMeteor(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }

        // 获取世界天数
        int worldDays = (int)(level.getGameTime() / 24000);
        
        ServerPlayer targetPlayer;
        
        // 如果是第4天或以上，从所有玩家中随机选择一个
        if (worldDays >= 4) {
            GhostlyUFODescent.LOGGER.info("World has reached day {}, randomly selecting a player from all online players.", worldDays);
            targetPlayer = players.get(level.random.nextInt(players.size()));
        } else {
            // 否则优先选择生存模式玩家
            targetPlayer = null;
            for (ServerPlayer player : players) {
                if (!player.isCreative() && !player.isSpectator()) {
                    targetPlayer = player;
                    break;
                }
            }

            // 如果没有找到生存模式玩家，则随机选择一个玩家
            if (targetPlayer == null) {
                targetPlayer = players.get(level.random.nextInt(players.size()));
            }
        }

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

        // 标记已经生成过陨石
        hasMeteorSpawned = true;
        GhostlyUFODescent.LOGGER.info("Meteor spawned at ({}, {}, {}) targeting player at ({}, {}, {}). Meteor spawn status updated to: {}",
                spawnX, spawnY, spawnZ,
                playerPos.getX(), playerPos.getY(), playerPos.getZ(),
                hasMeteorSpawned);
    }
}