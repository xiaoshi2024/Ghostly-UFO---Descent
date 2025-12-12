//package com.xiaoshi2022.ghostly_ufo_descent.world.dimension;
//
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.phys.Vec3;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
//
//// 这个类用于注册梦境世界的特殊视觉效果
//@EventBusSubscriber(modid = "ghostly_ufo_descent", value = Dist.CLIENT)
//public class DreamworldDimension {
//    @SubscribeEvent
//    public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
//        // 创建自定义的维度特效
//        event.register(ResourceLocation.parse("ghostly_ufo_descent:dream_world"), new RegisterDimensionSpecialEffectsEvent.DimensionSpecialEffects() {
//            @Override
//            public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
//                // 设置梦境世界的雾色
//                return new Vec3(0, 0.6, 0.4).multiply(sunHeight * 0.94 + 0.06, sunHeight * 0.94 + 0.06, sunHeight * 0.91 + 0.09);
//            }
//
//            @Override
//            public boolean isFoggyAt(int x, int y) {
//                // 梦境世界默认不产生雾
//                return false;
//            }
//        });
//    }
//}