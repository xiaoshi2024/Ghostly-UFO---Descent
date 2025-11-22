package com.xiaoshi2022.ghostly_ufo_descent.event;

import com.xiaoshi2022.ghostly_ufo_descent.registry.BlockRegistry;
import com.xiaoshi2022.ghostly_ufo_descent.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = "ghostly_ufo_descent")
public class CropPlantingHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (stack.getItem() == ItemRegistry.PHAGAN_SEEDS.get()) {
            // 检查点击的方块是否适合种植
            if (isSuitableSoil(level.getBlockState(pos))) {
                BlockPos cropPos = pos.above();

                if (level.isEmptyBlock(cropPos)) {
                    if (!level.isClientSide()) {
                        level.setBlock(cropPos, BlockRegistry.PHAGENABOTANYS.get().defaultBlockState(), 3);

                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }

                        // 播放种植音效
                        level.playSound(null, cropPos,
                                net.minecraft.sounds.SoundEvents.CROP_PLANTED,
                                net.minecraft.sounds.SoundSource.BLOCKS,
                                1.0F, 1.0F);
                    }

                    event.setCanceled(true);
                }
            }
        }
    }

    // 检查方块是否适合种植
    private static boolean isSuitableSoil(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.DIRT) || // 所有泥土类方块
                state.is(Blocks.FARMLAND) ||                   // 耕地
                state.is(Blocks.GRASS_BLOCK) ||                // 草方块
                state.is(Blocks.PODZOL) ||                     // 灰化土
                state.is(Blocks.MYCELIUM) ||                   // 菌丝
                state.is(Blocks.SOUL_SOIL) ||                  // 灵魂土
                state.is(Blocks.MUD) ||                        // 泥巴
                state.is(Blocks.MUDDY_MANGROVE_ROOTS);         // 泥泞的红树根
    }
}