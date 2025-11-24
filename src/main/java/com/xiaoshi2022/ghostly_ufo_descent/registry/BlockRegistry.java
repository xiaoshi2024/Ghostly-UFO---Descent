package com.xiaoshi2022.ghostly_ufo_descent.registry;

import com.xiaoshi2022.ghostly_ufo_descent.block.GhostlySarcophagus_block;
import com.xiaoshi2022.ghostly_ufo_descent.block.UfoL_block;
import com.xiaoshi2022.ghostly_ufo_descent.block.custom.Phagenabotanys;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.MODID;

public final class BlockRegistry {

    public static void init() {}

    //创建一个延迟寄存器来保存区块，这些区块都将在"ghostly_ufo_descent"命名空间下注册
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final Supplier<GhostlySarcophagus_block> GHOSTLY_SARCOPHAGUS_BLOCK =
            BLOCKS.registerBlock("ghostly_sarcophagus", properties -> new GhostlySarcophagus_block(
                    properties.noOcclusion()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(50.0F, 1200.0F) // 黑曜石级别的硬度和爆炸抗性
                            .requiresCorrectToolForDrops() // 需要正确的工具才能掉落
    ));

    public static final Supplier<UfoL_block> UFO_L_BLOCK =
            BLOCKS.registerBlock("ufo_l", properties -> new UfoL_block(
                    properties.noOcclusion()
                            .mapColor(MapColor.COLOR_BLUE)
                            .strength(50.0F, 1200.0F) // 黑曜石级别的硬度和爆炸抗性，与棺材保持一致
                            .requiresCorrectToolForDrops() // 需要正确的工具才能掉落
            ));

    //作物
    public static final DeferredBlock<Block> PHAGENABOTANYS = BLOCKS.registerBlock("phagena_crop",
            (properties) -> new Phagenabotanys(properties.mapColor(MapColor.PLANT).strength(0.2F)
                    .randomTicks().sound(SoundType.CROP).noOcclusion().pushReaction(PushReaction.DESTROY)));
}