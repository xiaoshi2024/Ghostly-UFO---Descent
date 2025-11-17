package com.xiaoshi2022.ghostly_ufo_descent.item;

import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.item.GhostlyScrollRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class GhostlyScroll  extends Item implements GeoItem {
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("open");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GhostlyScroll(Properties properties) {
        super(properties);

//        将我们的项目注册为服务器端处理。
//        这同时启用了动画数据同步和服务器端动画触发
        GeoItem.registerSyncedAnimatable(this);
    }

    // 利用我们自己的渲染钩子来定义我们的自定义渲染器
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GhostlyScrollRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<GhostlyScroll> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new GhostlyScrollRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("popup_controller", 20, animTest -> PlayState.CONTINUE)
                .triggerableAnim("open", OPEN));
        // We've marked the "activate" animation as being triggerable from the server
    }

    // Let's handle our use method so that we activate the animation when right-clicking while holding the box
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "popup_controller", "open");

        return super.use(level, player, hand);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
