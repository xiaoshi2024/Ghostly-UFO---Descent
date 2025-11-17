package com.xiaoshi2022.ghostly_ufo_descent.block.entity;

import com.xiaoshi2022.ghostly_ufo_descent.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.util.GeckoLibUtil;

public class UfoL_blockentity extends BlockEntity implements GeoBlockEntity {
    protected static final RawAnimation SPIN = RawAnimation.begin().thenPlay("spin");
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // We register a new DataTicket under our modid to use for holding the raining predicate for later
    public static final DataTicket<Long> DAY_TIME = DataTicket.create("ufo_day_time", Long.class);


    public UfoL_blockentity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.UFO_L_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(animTest -> {
            long dayTime = animTest.getData(DAY_TIME);

            if (dayTime > 23000 || dayTime < 13000) {
                return animTest.setAndContinue(IDLE);
            }
            else {
                return animTest.setAndContinue(SPIN);
            }
        }));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

