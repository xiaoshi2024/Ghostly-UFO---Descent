package com.xiaoshi2022.ghostly_ufo_descent.meteor.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import com.xiaoshi2022.ghostly_ufo_descent.meteor.entity.EntityMeteor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = GhostlyUFODescent.MODID)
public class MeteorTestHandler {

    // 注册测试指令
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        // 创建 /testmeteor 指令
        dispatcher.register(Commands.literal("testmeteor")
                .requires(source -> source.hasPermission(2)) // 需要管理员权限
                .executes(MeteorTestHandler::spawnTestMeteor)
                .then(Commands.argument("size", IntegerArgumentType.integer(1, 3)) // 添加可选的大小参数
                        .executes(context -> spawnTestMeteorWithSize(context, IntegerArgumentType.getInteger(context, "size"))))
        );
    }
    
    // 执行测试陨石生成
    private static int spawnTestMeteor(CommandContext<CommandSourceStack> context) {
        return spawnTestMeteorWithSize(context, 3); // 默认大小为3
    }
    
    // 执行指定大小的测试陨石生成
    private static int spawnTestMeteorWithSize(CommandContext<CommandSourceStack> context, int size) {
        CommandSourceStack source = context.getSource();
        
        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            // 在玩家附近生成一个测试陨石
            EntityMeteor meteor = EntityMeteor.create(
                    serverPlayer.level(),
                    serverPlayer.getX() + 20, // 生成在玩家前方20格
                    serverPlayer.getY() + 50, // 生成在高空50格
                    serverPlayer.getZ(),
                    new Vec3(-1.0, -3.0, 0.0), // 运动方向
                    size      // 陨石大小
            );

            if (meteor != null) {
                serverPlayer.level().addFreshEntity(meteor);

                // 发送测试消息
                source.sendSuccess(() -> Component.literal("测试陨石已生成！请查看指定位置的实体和方块生成情况。"), false);
                source.sendSuccess(() -> Component.literal("- 测试模式：100%概率生成 GhostlySarcophagus_block"), false);
                source.sendSuccess(() -> Component.literal("- 测试模式：40%概率生成 UfoPangenas"), false);
                source.sendSuccess(() -> Component.literal("- 测试模式：50%概率生成 SporeStarPerson"), false);

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
        
        return 1; // 命令执行成功
    }
}