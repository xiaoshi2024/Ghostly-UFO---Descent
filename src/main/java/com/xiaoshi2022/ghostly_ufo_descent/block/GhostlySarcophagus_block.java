package com.xiaoshi2022.ghostly_ufo_descent.block;

import com.mojang.serialization.MapCodec;
import com.xiaoshi2022.ghostly_ufo_descent.entities.playerbodys.CorpseEntity;
import com.xiaoshi2022.ghostly_ufo_descent.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.xiaoshi2022.ghostly_ufo_descent.world.dimension.DreamworldSafeSpawnHandler;

import javax.annotation.Nullable;
import java.util.Set;

public class GhostlySarcophagus_block extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public GhostlySarcophagus_block(BlockBehaviour.Properties properties) {
        super(properties
                .strength(3.0F, 8.0F)  // 添加合适的硬度和抗性
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(GhostlySarcophagus_block::new);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // 只在服务器端处理
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // 检查是否是晚上（0-13000是白天，13000-24000是晚上）
        long dayTime = level.getDayTime() % 24000;
        if (dayTime >= 0 && dayTime < 13000) {
            // 白天不能使用
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.sarcophagus.cant_sleep_day"));
            }
            return InteractionResult.FAIL;
        }

        // 检查附近是否有怪物
        if (isNightMonsterNearby(level, pos)) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.sarcophagus.monsters_nearby"));
            }
            return InteractionResult.FAIL;
        }

        // 处理玩家交互逻辑
        if (player instanceof ServerPlayer serverPlayer) {
            // 保存玩家位置信息到持久化数据中，供后续传送使用
            serverPlayer.getPersistentData().putDouble("sarcophagus_x", pos.getX() + 0.5);
            serverPlayer.getPersistentData().putDouble("sarcophagus_y", pos.getY() + 1.5);
            serverPlayer.getPersistentData().putDouble("sarcophagus_z", pos.getZ() + 0.5);
            serverPlayer.getPersistentData().putFloat("sarcophagus_yaw", serverPlayer.getYRot());
            serverPlayer.getPersistentData().putFloat("sarcophagus_pitch", serverPlayer.getXRot());

            // 创建灵魂状态标记
            serverPlayer.getPersistentData().putBoolean("soul_state", true);
            
            // 创建玩家尸体实体（从SarcophagusEvents合并）
            CorpseEntity corpse = CorpseEntity.createFromSarcophagusSleep(serverPlayer);
            
            // 将尸体添加到世界
            level.addFreshEntity(corpse);
            
            // 清除玩家的装备，让它们留在尸体里
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                serverPlayer.setItemSlot(slot, ItemStack.EMPTY);
            }
            
            // 清除玩家的物品栏
            serverPlayer.getInventory().clearContent();
            
            // 设置玩家为灵魂状态的视觉效果
            serverPlayer.setInvisible(true);
            serverPlayer.setInvulnerable(true);
            
            // 给玩家反馈
            serverPlayer.displayClientMessage(Component.translatable("block.ghostly_ufo_descent.sarcophagus.soul_out_of_body"), true);
            serverPlayer.displayClientMessage(Component.translatable("block.ghostly_ufo_descent.sarcophagus.return_to_body"), true);

            // 实现传送逻辑
            try {
                // 获取Dreamworld维度
                ResourceKey<Level> dreamWorldKey = ResourceKey.create(Registries.DIMENSION,
                        ResourceLocation.fromNamespaceAndPath("ghostly_ufo_descent", "dream_world"));

                // 使用 getServer() 方法而不是直接访问 server 字段
                ServerLevel dreamLevel = serverPlayer.level().getServer().getLevel(dreamWorldKey);

                if (dreamLevel != null) {
                    // 使用主世界出生点坐标作为基础，添加随机偏移
                    BlockPos basePos = dreamLevel.getRespawnData().pos();
                    BlockPos randomPos = new BlockPos(
                            basePos.getX() + level.getRandom().nextInt(100) - 50,
                            basePos.getY(),
                            basePos.getZ() + level.getRandom().nextInt(100) - 50
                    );
                    
                    // 调用安全传送处理器获取安全位置
                    BlockPos safePos = DreamworldSafeSpawnHandler.findSafeSpawnPosition(dreamLevel, randomPos);
                    double x = safePos.getX() + 0.5;
                    double y = safePos.getY() + 1.0;
                    double z = safePos.getZ() + 0.5;

                    // 传送玩家到Dreamworld维度
                    serverPlayer.teleportTo(dreamLevel, x, y, z, Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot(), false);

                    serverPlayer.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.sarcophagus.entered_dream"));
                } else {
                    serverPlayer.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.sarcophagus.dimension_not_found"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                serverPlayer.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.sarcophagus.teleport_failed"));
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // 检查附近是否有怪物
    private boolean isNightMonsterNearby(Level level, BlockPos pos) {
        // 简单实现：检查周围20格范围内是否有攻击性生物
        return !level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
                new net.minecraft.world.phys.AABB(pos).inflate(20, 10, 20),
                mob -> mob.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER
                        && mob.isAlive()
        ).isEmpty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING,
                context.getHorizontalDirection().getClockWise().getClockWise());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.GHOSTLY_SARCOPHAGUS_BLOCK_ENTITY.get().create(blockPos, blockState);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> Block.box(0, 0, 0, 32, 16, 16);
            case SOUTH -> Block.box(-16, 0, 0, 16, 16, 16);
            case WEST -> Block.box(0, 0, -16, 16, 16, 16);
            default -> Block.box(0, 0, 0, 16, 16, 32);
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        for (BlockPos testPos : BlockPos.betweenClosed(pos,
                pos.relative(state.getValue(FACING).getClockWise(), 2))) {
            if (!testPos.equals(pos) && !world.getBlockState(testPos).isAir())
                return false;
        }

        return true;
    }

    // 移除错误的 onRemove 方法重写
}