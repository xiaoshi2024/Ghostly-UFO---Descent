package com.xiaoshi2022.ghostly_ufo_descent;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import static com.xiaoshi2022.ghostly_ufo_descent.registry.BlockEntityRegistry.BLOCK_ENTITIES;
import static com.xiaoshi2022.ghostly_ufo_descent.registry.BlockRegistry.BLOCKS;
import static com.xiaoshi2022.ghostly_ufo_descent.registry.ItemRegistry.*;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(GhostlyUFODescent.MODID)
public class GhostlyUFODescent {
    // 在公共位置定义 mod id 以供所有内容引用
    public static final String MODID = "ghostly_ufo_descent";
    // 直接引用 slf4j 记录器
    public static final Logger LOGGER = LogUtils.getLogger();


    // 创建一个延迟寄存器来保存 CreativeModeTabs，这些 CreativeModeTabs 都将在“ghostly_ufo_descent”命名空间下注册
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // 为示例项目创建一个 ID 为“ghostly_ufo_descent：example_tab”的广告素材选项卡，该选项卡位于战斗选项卡之后
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GHOSTLY_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ghostly_ufo_descent")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> GHOSTLY_SCROLL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(GHOSTLY_SCROLL.get());
                output.accept(GHOSTLY_SARCOPHAGUS_ITEM.get());
            }).build());

    // mod 类的构造函数是加载 mod 时运行的第一个代码。
    // FML 将识别一些参数类型，如 IEventBus 或 ModContainer，并自动传入它们。
    public GhostlyUFODescent(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        BLOCK_ENTITIES.register(modEventBus);

//        注册我们自己参加我们感兴趣的服务器和其他游戏活动。
//        请注意，当且仅当我们希望 *this* 类 （GhostlyUFODescent） 直接响应事件时，这是必要的。
//        如果此类中没有带@SubscribeEvent注释的函数，请不要添加此行，例如下面的 onServerStarting（）。
        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        //一些常见的设置代码
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    //将示例块项添加到构建块选项卡
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept((ItemLike) GHOSTLY_SCROLL);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
