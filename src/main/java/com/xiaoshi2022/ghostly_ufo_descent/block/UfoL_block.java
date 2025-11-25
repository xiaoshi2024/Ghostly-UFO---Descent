package com.xiaoshi2022.ghostly_ufo_descent.block;

import com.mojang.serialization.MapCodec;
import com.xiaoshi2022.ghostly_ufo_descent.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Optional;

public class UfoL_block extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = makeShape();

    public UfoL_block(BlockBehaviour.Properties properties) {
        super(properties
                .strength(4.0F, 8.0F)  // 添加合适的硬度和抗性
                .noOcclusion()        // 无遮挡渲染
                .isRedstoneConductor((state, getter, pos) -> false)  // 非完整方块，不传导红石
                .isSuffocating((state, getter, pos) -> false)        // 不会造成窒息
                .isViewBlocking((state, getter, pos) -> false)       // 不阻挡视线
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(UfoL_block::new);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // 只在服务器端处理
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        boolean operationPerformed = false;

        // 检查玩家是否处于灵魂状态
        if (player instanceof ServerPlayer serverPlayer) {
            Optional<Boolean> soulStateOpt = serverPlayer.getPersistentData().getBoolean("soul_state");
            boolean soulState = soulStateOpt.orElse(false);

            if (soulState) {
                // 检查玩家是否在Dreamworld维度
                boolean isInDreamWorld = serverPlayer.level().dimension().location().toString().equals("ghostly_ufo_descent:dream_world");

                if (isInDreamWorld) {
                    // 从玩家的持久化数据中获取传送回主世界的位置
                    Optional<Double> xOpt = serverPlayer.getPersistentData().getDouble("sarcophagus_x");
                    Optional<Double> yOpt = serverPlayer.getPersistentData().getDouble("sarcophagus_y");
                    Optional<Double> zOpt = serverPlayer.getPersistentData().getDouble("sarcophagus_z");
                    Optional<Float> yawOpt = serverPlayer.getPersistentData().getFloat("sarcophagus_yaw");
                    Optional<Float> pitchOpt = serverPlayer.getPersistentData().getFloat("sarcophagus_pitch");

                    // 检查所有必需的数据都存在
                    if (xOpt.isPresent() && yOpt.isPresent() && zOpt.isPresent() &&
                            yawOpt.isPresent() && pitchOpt.isPresent()) {

                        // 获取主世界维度
                        ResourceKey<Level> overworldKey = Level.OVERWORLD;
                        ServerLevel overworld = serverPlayer.level().getServer().getLevel(overworldKey);

                        if (overworld != null) {
                            // 从 Optional 中获取实际值
                            double x = xOpt.get();
                            double y = yOpt.get();
                            double z = zOpt.get();
                            float yaw = yawOpt.get();
                            float pitch = pitchOpt.get();

                            // 传送玩家回主世界
                            serverPlayer.teleportTo(overworld, x, y, z, java.util.Set.of(), yaw, pitch, false);

                            // 重置玩家的灵魂状态
                            serverPlayer.setInvisible(true); // 不恢复
                            serverPlayer.setInvulnerable(false); // 恢复可受伤
                            serverPlayer.getPersistentData().putBoolean("soul_state", false);

                            // 发送消息给玩家
                            serverPlayer.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.ufo.returned_to_body"));

                            operationPerformed = true;
                        } else {
                            serverPlayer.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.ufo.overworld_not_found"));
                        }
                    } else {
                        serverPlayer.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.ufo.no_sarcophagus_data"));
                    }
                } else {
                    serverPlayer.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.ufo.not_in_dreamworld"));
                }
            } else {
                serverPlayer.sendSystemMessage(Component.translatable("block.ghostly_ufo_descent.ufo.not_soul_state"));
            }
        }

        // 如果没有执行传送操作，则切换发光状态
        if (!operationPerformed) {
            level.setBlock(pos, state.cycle(POWERED), 3);
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BlockEntityRegistry.UFO_L_BLOCK_ENTITY.get().create(blockPos, blockState);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE; // 使用你定义的复杂形状
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE; // 碰撞形状与视觉形状一致
    }

    // 移除不存在的方法

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // 使用实体方块渲染
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F; // 完全亮度，避免阴影
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getValue(POWERED) ? 15 : 0; // 发光状态时亮度为15，否则为0
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.below()).isSolid();
    }

    // 优化的 VoxelShape - 使方块更容易被点击到
    private static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();

        // 基础主体 - 更大更明显的核心区域
        shape = Shapes.join(shape, Shapes.box(0.25, 0.3, 0.25, 0.75, 0.7, 0.75), BooleanOp.OR);

        // 底部圆盘 - 覆盖更大区域便于点击
        shape = Shapes.join(shape, Shapes.box(0.1, 0.35, 0.1, 0.9, 0.4, 0.9), BooleanOp.OR);

        // 顶部发光部分
        shape = Shapes.join(shape, Shapes.box(0.4, 0.7, 0.4, 0.6, 0.9, 0.6), BooleanOp.OR);

        // 扩展的交互区域 - 增加侧面的碰撞箱
        shape = Shapes.join(shape, Shapes.box(0.0, 0.4, 0.4, 0.2, 0.6, 0.6), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8, 0.4, 0.4, 1.0, 0.6, 0.6), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4, 0.4, 0.0, 0.6, 0.6, 0.2), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4, 0.4, 0.8, 0.6, 0.6, 1.0), BooleanOp.OR);

        // 中部平台 - 增加更多交互点
        shape = Shapes.join(shape, Shapes.box(0.15, 0.45, 0.15, 0.85, 0.55, 0.85), BooleanOp.OR);

        return shape.optimize(); // 优化形状
    }
}