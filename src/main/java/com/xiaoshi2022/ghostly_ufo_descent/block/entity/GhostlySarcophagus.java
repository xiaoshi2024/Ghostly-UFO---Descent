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

public class GhostlySarcophagus extends BlockEntity implements GeoBlockEntity {
    protected static final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    protected static final RawAnimation OPENX = RawAnimation.begin().thenPlay("openx");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // We register a new DataTicket under our modid to use for holding the raining predicate for later
    public static final DataTicket<Long> DAY_TIME = DataTicket.create("ghostly_ufo_descent_day_time", Long.class);


    public GhostlySarcophagus(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.GHOSTLY_SARCOPHAGUS_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(animTest -> {
            long dayTime = animTest.getData(DAY_TIME);

            if (dayTime > 23000 || dayTime < 13000) {
                return animTest.setAndContinue(OPEN);
            }
            else {
                return animTest.setAndContinue(OPENX);
            }
        }));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
