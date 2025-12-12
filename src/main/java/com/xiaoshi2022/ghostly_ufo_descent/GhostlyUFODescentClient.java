package com.xiaoshi2022.ghostly_ufo_descent;

import com.xiaoshi2022.ghostly_ufo_descent.client.model.layer.HornsRenderLayer;
import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.block.GhostlySarcophagusRenderer;
import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.block.PhagenaCropRenderer;
import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.block.UfoL_blockentityRenderer;
import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity.EntityMeteorRenderer;
import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity.SpiritPossessorRenderer;
import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity.SporeStarPersonRenerer;
import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity.UfoPangenasRenderer;
import com.xiaoshi2022.ghostly_ufo_descent.entities.SpiritPossessor;
import com.xiaoshi2022.ghostly_ufo_descent.entities.SporeStarPerson;
import com.xiaoshi2022.ghostly_ufo_descent.entities.UfoPangenas;
import com.xiaoshi2022.ghostly_ufo_descent.entities.playerbodys.CorpseRenderer;
import com.xiaoshi2022.ghostly_ufo_descent.registry.BlockEntityRegistry;
import com.xiaoshi2022.ghostly_ufo_descent.registry.EntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

// 此类不会在专用服务器上加载。从这里访问客户端代码是安全的。
@Mod(value = GhostlyUFODescent.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = GhostlyUFODescent.MODID, value = Dist.CLIENT)
public class GhostlyUFODescentClient {
    public GhostlyUFODescentClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

        // 注册实体渲染器
        EntityRenderers.register(EntityRegistry.SPORE_STAR_PERSON.get(), SporeStarPersonRenerer::new);
        // 添加CorpseEntity渲染器注册
        EntityRenderers.register(EntityRegistry.CORPSE_ENTITY.get(), CorpseRenderer::new);
        // 注册极噬者UFO渲染器
        EntityRenderers.register(EntityRegistry.UFO_PANGENAS.get(), UfoPangenasRenderer::new);
        // 注册覆灵者实体渲染器
        EntityRenderers.register(EntityRegistry.SPIRIT_POSSESSOR.get(), SpiritPossessorRenderer::new);

        EntityRenderers.register(EntityRegistry.METEOR.get(), EntityMeteorRenderer::new);

        // Some client setup code
        GhostlyUFODescent.LOGGER.info("HELLO FROM CLIENT SETUP");
        GhostlyUFODescent.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // 注册孢子星人的属性
        event.put(EntityRegistry.SPORE_STAR_PERSON.get(), SporeStarPerson.createAttributes());
        // 注册极噬者UFO的属性
        event.put(EntityRegistry.UFO_PANGENAS.get(), UfoPangenas.createAttributes());
        // 注册覆灵者的属性
        event.put(EntityRegistry.SPIRIT_POSSESSOR.get(), SpiritPossessor.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityRegistry.GHOSTLY_SARCOPHAGUS_BLOCK_ENTITY.get(),
                GhostlySarcophagusRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.UFO_L_BLOCK_ENTITY.get(),
                UfoL_blockentityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.PHAGENA_CROP.get(),
                PhagenaCropRenderer::new);
    }

    @SubscribeEvent // 只有在物理客户端的 MOD 事件总线上
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        // 对所有可能的玩家模型进行迭代.
        for (PlayerModelType type : event.getSkins()) {
            // 获取相关的AvatarRenderer.
            AvatarRenderer<AbstractClientPlayer> playerRenderer = event.getPlayerRenderer(type);
            if (playerRenderer != null) {
//                把图层添加到渲染器中
//                有合适的通用代码来支持玩家和玩家渲染器。
                playerRenderer.addLayer(new HornsRenderLayer(playerRenderer, event.getEntityModels()));
            }
        }
    }
}
