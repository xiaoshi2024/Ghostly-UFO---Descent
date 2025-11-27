package com.xiaoshi2022.ghostly_ufo_descent.meteor.event;

import com.xiaoshi2022.ghostly_ufo_descent.Config;
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
    private static int tickCounter = 0;
    private static final int MAX_METEORS_PER_CHUNK = 1;

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        tickCounter++;
        // 使用更灵活的检查逻辑，确保配置更改后能更快触发
        if (tickCounter > Config.METEOR_CHECK_INTERVAL.get()) {
            tickCounter = 0;
        }
        
        if (tickCounter != Config.METEOR_CHECK_INTERVAL.get()) {
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

        // 获取世界的天数
        int worldDays = (int)(level.getGameTime() / 24000);
        
        // 检查世界是否达到配置的天数要求
        if (worldDays < Config.METEOR_SPAWN_DAY.get()) {
            // 只在开发模式或者特定条件下输出日志，避免频繁输出
            if (worldDays % 20 == 0) { // 每20天输出一次
                GhostlyUFODescent.LOGGER.debug("World day {} is less than required {}. Skipping meteor spawn.", worldDays, Config.METEOR_SPAWN_DAY.get());
            }
            return false;
        }
        
        // 检查是否有玩家在生存模式下
        List<? extends Player> players = level.players();
        boolean hasSurvivalPlayer = false;
        for (Player player : players) {
            if (!player.isCreative() && !player.isSpectator()) {
                hasSurvivalPlayer = true;
                break;
            }
        }
        
        if (!hasSurvivalPlayer) {
            // 只在没有生存玩家时偶尔输出日志
            if (worldDays % 10 == 0) {
                GhostlyUFODescent.LOGGER.debug("No survival mode players found. Skipping meteor spawn.");
            }
            return false;
        }
        
        // 使用随机概率决定是否生成陨石
        boolean shouldSpawn = random.nextDouble() < Config.METEOR_CHANCE.get();
        
        // 只在实际生成陨石或者概率较高时输出日志
        if (shouldSpawn) {
            GhostlyUFODescent.LOGGER.info("Meteor spawn triggered at world day {}", worldDays);
        } else if (random.nextDouble() < 0.1) { // 10%概率输出日志，避免太频繁
            GhostlyUFODescent.LOGGER.debug("Meteor spawn check at world day {}, chance not met.", worldDays);
        }
        
        return shouldSpawn;
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

        GhostlyUFODescent.LOGGER.info("Meteor spawned at ({}, {}, {}) targeting player at ({}, {}, {})",
                spawnX, spawnY, spawnZ,
                playerPos.getX(), playerPos.getY(), playerPos.getZ());
    }
}