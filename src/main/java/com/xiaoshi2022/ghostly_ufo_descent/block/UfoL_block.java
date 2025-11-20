package com.xiaoshi2022.ghostly_ufo_descent.block;

import com.mojang.serialization.MapCodec;
import com.xiaoshi2022.ghostly_ufo_descent.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class UfoL_block extends BaseEntityBlock implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    private static final VoxelShape SHAPE = makeShape();

    public UfoL_block(BlockBehaviour.Properties properties) {
        super(properties
                .strength(4.0F, 8.0F)  // 添加合适的硬度和抗性
                .noOcclusion()        // 无遮挡渲染
                .isRedstoneConductor((state, getter, pos) -> false)  // 非完整方块，不传导红石
                .isSuffocating((state, getter, pos) -> false)        // 不会造成窒息
                .isViewBlocking((state, getter, pos) -> false)       // 不阻挡视线
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(UfoL_block::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // 使用实体方块渲染
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F; // 完全亮度，避免阴影
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.below()).isSolid();
    }

    // 修复后的 VoxelShape - 修正所有超出范围的坐标
    private static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();

        // 修正所有坐标到 0-1 范围内
        shape = Shapes.join(shape, Shapes.box(0.375, 0.3125, 0.375, 0.625, 0.60625, 0.625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.35, 0, 1, 0.39375, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.4375, 0.75, 0.4375, 0.5625, 0.875, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.09375, 0.375, 0.0625, 0.921875, 0.5, 0.953125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.475, 0, 1, 0.51875, 1), BooleanOp.OR);

        // 修正超出边界的坐标
        shape = Shapes.join(shape, Shapes.box(0.984375, 0.4375, 0.46875, 1.0, 0.5625, 0.5625), BooleanOp.OR); // 修正 1.0625 -> 1.0
        shape = Shapes.join(shape, Shapes.box(0.984375, 0.3125, 0.46875, 1.0, 0.4375, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.984375, 0.1875, 0.46875, 1.0, 0.3125, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0, 0.4375, 0.46875, 0.03125, 0.5625, 0.5625), BooleanOp.OR); // 修正 -0.046875 -> 0.0
        shape = Shapes.join(shape, Shapes.box(0.0, 0.3125, 0.46875, 0.03125, 0.4375, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0, 0.1875, 0.46875, 0.03125, 0.3125, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.203125, 0.4375, 0.96875, 0.28125, 0.5625, 1.0), BooleanOp.OR); // 修正 1.0625 -> 1.0
        shape = Shapes.join(shape, Shapes.box(0.203125, 0.3125, 0.96875, 0.28125, 0.4375, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.203125, 0.1875, 0.96875, 0.28125, 0.3125, 1.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.703125, 0.4375, 0.0, 0.78125, 0.5625, 0.0), BooleanOp.OR); // 修正 -0.09375 -> 0.0
        shape = Shapes.join(shape, Shapes.box(0.703125, 0.3125, 0.0, 0.78125, 0.4375, 0.0), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.703125, 0.1875, 0.0, 0.78125, 0.3125, 0.0), BooleanOp.OR);

        shape = Shapes.join(shape, Shapes.box(0.1875, 0.5375, 0.665625, 0.8, 0.58125, 0.74375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.5375, 0.265625, 0.8, 0.58125, 0.34375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.5375, 0.353125, 0.3, 0.58125, 0.65625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6875, 0.5375, 0.353125, 0.8, 0.58125, 0.65625), BooleanOp.OR);

        return shape.optimize(); // 优化形状
    }
}