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

public class PhagenaCropBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PhagenaCropBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.PHAGENA_CROP.get(), pos, state);
    }

    // 定义数据票据[用于实现动画数据]
    public static final DataTicket<Integer> CROP_AGE = DataTicket.create("crop_age", Integer.class);
    public static final DataTicket<Long> DAY_TIME = DataTicket.create("day_time", Long.class);


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("grow_controller", 0, state -> {
            // 根据作物的生长阶段返回对应的动画
            if (getBlockState().getBlock() instanceof com.xiaoshi2022.ghostly_ufo_descent.block.custom.Phagenabotanys crop) {
                int age = getBlockState().getValue(com.xiaoshi2022.ghostly_ufo_descent.block.custom.Phagenabotanys.AGE);
                switch (age) {
                    case 0:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("stage_0"));
                    case 1:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("stage_1"));
                    case 2:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("stage_2"));
                    case 3:
                        return state.setAndContinue(RawAnimation.begin().thenPlay("stage_3"));
                }
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("stage_0"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}