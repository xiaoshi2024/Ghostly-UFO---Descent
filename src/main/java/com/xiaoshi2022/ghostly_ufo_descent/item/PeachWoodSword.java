package com.xiaoshi2022.ghostly_ufo_descent.item;

import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.item.PeachWoodSwordRenerer;
import com.xiaoshi2022.ghostly_ufo_descent.registry.ToolMaterialRegistry;
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
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class PeachWoodSword extends Item implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PeachWoodSword(Properties properties) {
        // 使用工具材料设置剑的属性
        super(properties.sword(
                ToolMaterialRegistry.PEACH_WOOD_MATERIAL,
                // 剑的攻击伤害加成
                3,
                // 剑的攻击速度修饰符
                -2.4f
        ));
        GeoItem.registerSyncedAnimatable(this);
    }

    // 利用我们自己的渲染钩子来定义我们的自定义渲染器
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private PeachWoodSwordRenerer renderer;

            @Override
            public @Nullable GeoItemRenderer<PeachWoodSword> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new PeachWoodSwordRenerer();

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("idle_controller", 20, animTest -> PlayState.CONTINUE)
                .triggerableAnim("idle", IDLE));
        // We've marked the "activate" animation as being triggerable from the server
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel)
            triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), serverLevel), "idle_controller", "idle");

        return super.use(level, player, hand);
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
