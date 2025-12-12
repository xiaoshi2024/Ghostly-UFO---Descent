package com.xiaoshi2022.ghostly_ufo_descent.event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Optional;

@EventBusSubscriber(modid = "ghostly_ufo_descent")
public class SoulStateHandler {

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 检查玩家是否处于灵魂状态
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
            if (serverPlayer.getPersistentData().contains("soul_state")) {
                // 根据当前维度设置玩家的隐身状态
                updateSoulVisibility(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // 玩家登录时检查并更新灵魂状态
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
            if (serverPlayer.getPersistentData().contains("soul_state")) {
                updateSoulVisibility(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingJumpEvent event) {
        // 每tick检查一次，确保玩家状态正确
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
            // 只有当玩家真的处于灵魂状态时才更新
            Optional<Boolean> soulStateOpt = serverPlayer.getPersistentData().getBoolean("soul_state");
            if (soulStateOpt.isPresent() && soulStateOpt.get()) {
                // 只在玩家维度变化时更新，避免不必要的计算
                if (serverPlayer.tickCount % 20 == 0) { // 每20tick(1秒)检查一次
                    updateSoulVisibility(serverPlayer);
                }
            }
        }
    }

    /**
     * 处理玩家在梦境世界死亡的事件
     * 当玩家在梦境世界死亡时，将其传送到主世界肉身位置
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            // 检查玩家是否在梦境世界并且处于灵魂状态
            boolean isInDreamWorld = player.level().dimension().toString().equals("ghostly_ufo_descent:dream_world");
            boolean isInSoulState = player.getPersistentData().getBoolean("soul_state").orElse(false);
            
            if (isInDreamWorld && isInSoulState) {
                // 取消死亡事件，防止玩家真正死亡
                event.setCanceled(true);
                
                // 获取主世界维度
                ResourceKey<Level> overworldKey = Level.OVERWORLD;
                ServerLevel overworld = player.level().getServer().getLevel(overworldKey);
                
                if (overworld != null) {
                    // 从玩家的持久化数据中获取传送回主世界的位置
                    // 检查是否存在必要的数据
                    boolean hasSarcophagusData = player.getPersistentData().contains("sarcophagus_x") && 
                                              player.getPersistentData().contains("sarcophagus_y") && 
                                              player.getPersistentData().contains("sarcophagus_z") &&
                                              player.getPersistentData().contains("sarcophagus_yaw") &&
                                              player.getPersistentData().contains("sarcophagus_pitch");
                    
                    if (hasSarcophagusData) {
                        // 获取位置数据
                        double x = player.getPersistentData().getDouble("sarcophagus_x").orElse(0.5);
                        double y = player.getPersistentData().getDouble("sarcophagus_y").orElse(70.0);
                        double z = player.getPersistentData().getDouble("sarcophagus_z").orElse(0.5);
                        float yaw = player.getPersistentData().getFloat("sarcophagus_yaw").orElse(player.getYRot());
                        float pitch = player.getPersistentData().getFloat("sarcophagus_pitch").orElse(player.getXRot());
                        
                        // 重置玩家的灵魂状态
                        player.getPersistentData().putBoolean("soul_state", false);
                        
                        // 恢复玩家状态
                        player.setInvisible(false);
                        player.setInvulnerable(false);
                        player.setHealth(player.getMaxHealth());
                        player.clearFire();
                        
                        // 传送玩家到主世界肉身位置
                        player.teleportTo(overworld, x, y, z, java.util.Set.of(), yaw, pitch, false);
                        
                        // 发送消息给玩家
                        player.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.sarcophagus.death_return_to_body"));
                    } else {
                        // 如果没有必要的数据，使用默认位置并提示玩家
                        double x = 0.5;
                        double y = 70.0;
                        double z = 0.5;
                        float yaw = player.getYRot();
                        float pitch = player.getXRot();
                        
                        // 重置玩家的灵魂状态
                        player.getPersistentData().putBoolean("soul_state", false);
                        
                        // 恢复玩家状态
                        player.setInvisible(false);
                        player.setInvulnerable(false);
                        player.setHealth(player.getMaxHealth());
                        player.clearFire();
                        
                        // 传送玩家到默认位置
                        player.teleportTo(overworld, x, y, z, java.util.Set.of(), yaw, pitch, false);
                        
                        // 发送消息给玩家，提示缺少石棺数据
                        player.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.ufo.no_sarcophagus_data"));
                    }
                } else {
                    // 如果找不到主世界，发送错误消息
                    player.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.ufo.overworld_not_found"));
                }
            }
        }
    }
    
    /**
     * 根据玩家当前所在维度更新灵魂状态的隐身效果和无敌状态
     */
    private static void updateSoulVisibility(ServerPlayer player) {
        // 检查玩家是否在Dreamworld维度
        boolean isInDreamWorld = player.level().dimension().toString().equals("ghostly_ufo_descent:dream_world");
        
        if (isInDreamWorld) {
            // 在深梦维度中，灵魂状态可见且可受到伤害
            player.setInvisible(false);
            player.setInvulnerable(false);
        } else {
            // 在主世界或其他维度中，灵魂状态隐身且无敌
            player.setInvisible(true);
            player.setInvulnerable(true);
        }
    }
}