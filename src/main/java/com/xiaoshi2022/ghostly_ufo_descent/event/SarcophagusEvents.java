package com.xiaoshi2022.ghostly_ufo_descent.event;

import com.xiaoshi2022.ghostly_ufo_descent.block.GhostlySarcophagus_block;
import com.xiaoshi2022.ghostly_ufo_descent.entities.playerbodys.CorpseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class SarcophagusEvents {

    @SubscribeEvent
    public void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        Level level = event.getLevel();
        BlockState state = level.getBlockState(pos);

        // 检查玩家是否右击了GhostlySarcophagus
        if (state.getBlock() instanceof GhostlySarcophagus_block && player instanceof ServerPlayer serverPlayer) {
            // 取消事件的进一步处理
            event.setCanceled(true);

            // 创建遗体实体
            CorpseEntity corpse = CorpseEntity.createFromSarcophagusSleep(serverPlayer);

            // 将遗体添加到世界
            level.addFreshEntity(corpse);
            
            // 清除玩家的装备，让它们留在尸体里
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                serverPlayer.setItemSlot(slot, ItemStack.EMPTY);
            }
            
            // 清除玩家的物品栏
            serverPlayer.getInventory().clearContent();
            
            // 设置玩家为灵魂出窍状态的视觉效果（例如：隐身效果）
            serverPlayer.setInvisible(true);
            
            // 给玩家一个灵魂状态的标签，便于后续识别
            serverPlayer.getPersistentData().putBoolean("soul_state", true);

            // 给玩家反馈
            player.displayClientMessage(Component.literal("灵魂出窍！你已离开躯体，进入灵魂状态。"), true);
            player.displayClientMessage(Component.literal("右键点击你的躯体可以重返肉身。"), true);

            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}