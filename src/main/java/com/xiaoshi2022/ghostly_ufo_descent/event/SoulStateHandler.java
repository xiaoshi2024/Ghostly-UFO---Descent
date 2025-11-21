package com.xiaoshi2022.ghostly_ufo_descent.event;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.server.level.ServerPlayer;

@EventBusSubscriber(modid = "ghostly_ufo_descent")
public class SoulStateHandler {

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 检查玩家是否处于灵魂状态
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
            if (serverPlayer.getPersistentData().getBoolean("soul_state") != null) {
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
            if (serverPlayer.getPersistentData().getBoolean("soul_state") != null) {
                updateSoulVisibility(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingJumpEvent event) {
        // 每tick检查一次，确保玩家状态正确
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
            if (serverPlayer.getPersistentData().getBoolean("soul_state") != null) {
                // 只在玩家维度变化时更新，避免不必要的计算
                if (serverPlayer.tickCount % 20 == 0) { // 每20tick(1秒)检查一次
                    updateSoulVisibility(serverPlayer);
                }
            }
        }
    }

    /**
     * 根据玩家当前所在维度更新灵魂状态的隐身效果和无敌状态
     */
    private static void updateSoulVisibility(ServerPlayer player) {
        // 检查玩家是否在Dreamworld维度
        boolean isInDreamWorld = player.level().dimension().location().toString().equals("ghostly_ufo_descent:dream_world");
        
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