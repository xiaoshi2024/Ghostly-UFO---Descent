package com.xiaoshi2022.ghostly_ufo_descent.block.custom;

import com.xiaoshi2022.ghostly_ufo_descent.block.entity.PhagenaCropBlockEntity;
import com.xiaoshi2022.ghostly_ufo_descent.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class Phagenabotanys extends CropBlock implements EntityBlock {
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private static final VoxelShape[] SHAPE_BY_AGE =
            new VoxelShape[]{
                    Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
                    Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
                    Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
                    Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)};

    public Phagenabotanys(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // 告诉游戏使用 BlockEntity 渲染，而不是原版方块渲染
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[state.getValue(this.getAgeProperty())];
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ItemRegistry.PHAGAN_SEEDS.get();
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    // 实现 EntityBlock 接口的方法
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PhagenaCropBlockEntity(pos, state);
    }
}