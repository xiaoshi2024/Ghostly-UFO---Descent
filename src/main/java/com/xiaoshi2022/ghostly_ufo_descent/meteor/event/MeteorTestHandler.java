package com.xiaoshi2022.ghostly_ufo_descent.meteor.event;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import com.xiaoshi2022.ghostly_ufo_descent.meteor.entity.EntityMeteor;
import net.minecraft.world.phys.Vec3;

@EventBusSubscriber(modid = GhostlyUFODescent.MODID)
public class MeteorTestHandler {

    // 允许玩家使用下界之星右键点击地面来测试陨石生成功能
    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() && event.getItemStack().getItem() == Items.NETHER_STAR) {
            Player player = event.getEntity();
            if (player instanceof ServerPlayer serverPlayer) {
                // 在玩家附近生成一个测试陨石
                EntityMeteor meteor = EntityMeteor.create(
                        serverPlayer.level(),
                        serverPlayer.getX() + 20, // 生成在玩家前方20格
                        serverPlayer.getY() + 50, // 生成在高空50格
                        serverPlayer.getZ(),
                        new Vec3(-1.0, -3.0, 0.0), // 运动方向
                        3      // 陨石大小
                );

                if (meteor != null) {
                    serverPlayer.level().addFreshEntity(meteor);

                    // 发送测试消息
                    player.displayClientMessage(Component.literal("测试陨石已生成！请查看指定位置的实体和方块生成情况。"), false);
                    player.displayClientMessage(Component.literal("- 测试模式：100%概率生成 GhostlySarcophagus_block"), false);
                    player.displayClientMessage(Component.literal("- 测试模式：100%概率生成 UfoPangenas"), false);
                    player.displayClientMessage(Component.literal("- 测试模式：100%概率生成 SporeStarPerson"), false);

                    // 生成粒子效果作为标记
                    for (int i = 0; i < 20; i++) {
                        double x = serverPlayer.getX() + 20 + (serverPlayer.getRandom().nextDouble() - 0.5) * 2;
                        double z = serverPlayer.getZ() + (serverPlayer.getRandom().nextDouble() - 0.5) * 2;
                        serverPlayer.level().sendParticles(
                                ParticleTypes.FLAME,
                                x,
                                serverPlayer.getY() + 1,
                                z,
                                1, 0, 0, 0, 0.1
                        );
                    }
                }
            }
        }
    }
}