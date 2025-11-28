package com.xiaoshi2022.ghostly_ufo_descent.transformation;

import com.xiaoshi2022.ghostly_ufo_descent.api.codec.CodecUtils;
import com.xiaoshi2022.ghostly_ufo_descent.entities.SpiritPossessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 化形管理器，负责协调整个化形过程
 */
public class TransformationManager {
    
    /**
     * 方块化形为玩家形象
     * @param player 玩家实体
     * @param level 游戏世界
     * @param pos 方块位置
     * @param state 方块状态
     * @param block 方块实例
     * @param blockEntity 方块实体
     * @return 是否成功化形
     */
    public static boolean transformBlock(Player player, Level level, BlockPos pos, BlockState state, Block block, BlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        
        try {
            // 创建灵魂粒子效果
            spawnSoulParticles(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15);
            
            // 播放化形声音
            level.playSound(null, pos, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            // 获取基于方块类型的眼睛颜色
            byte eyeColor = getEyeColorFromBlock(block);
            
            // 创建化形实体（使用SpiritPossessor）
            SpiritPossessor transformedEntity = SpiritPossessor.createWithPlayer(level, player);
            
            // 设置位置（方块中心）
            transformedEntity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            transformedEntity.setYRot(player.getYRot());
            
            // 设置眼睛颜色
            transformedEntity.setEyeColorType(eyeColor);
            
            // 存储化形相关信息
            transformedEntity.getPersistentData().putString("transformation_type", "block");
            transformedEntity.getPersistentData().putString("original_block", BuiltInRegistries.BLOCK.getKey(block).toString());
            
            // 应用化形属性
            TransformationUtils.setTransformationProperties(transformedEntity);
            
            // 生成实体
            level.addFreshEntity(transformedEntity);
            
            // 移除原方块
            level.removeBlock(pos, false);
            
            return true;
            
        } catch (Exception e) {
            // 如果化形失败，尝试添加简单的灵魂效果
            try {
                addSimpleSoulEffectToBlock(level, pos, state, blockEntity);
            } catch (Exception ex) {
                // 忽略可能的错误
            }
            return false;
        }
    }
    
    /**
     * 为物品添加化形效果并创建覆灵者实体
     * @param player 玩家实体
     * @param level 游戏世界
     * @param itemEntity 物品实体
     * @return 是否成功添加化形效果
     */
    public static boolean transformItem(Player player, Level level, ItemEntity itemEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        
        try {
            ItemStack itemStack = itemEntity.getItem();
            
            // 创建灵魂粒子效果
            spawnSoulParticles(serverLevel, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 15);
            
            // 播放附魔声音
            level.playSound(null, itemEntity.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // 获取基于物品类型的眼睛颜色
            byte eyeColor = getItemEyeColor(itemStack);
            
            // 创建覆灵者实体
            SpiritPossessor spiritPossessor = SpiritPossessor.createWithPlayer(level, player);
            
            // 设置位置（物品位置）
            spiritPossessor.setPos(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
            spiritPossessor.setYRot(player.getYRot());
            
            // 设置眼睛颜色
            spiritPossessor.setEyeColorType(eyeColor);
            
            // 存储化形相关信息
            spiritPossessor.getPersistentData().putString("transformation_type", "item");
            spiritPossessor.getPersistentData().putString("original_item", itemStack.getItem().toString());
            // 存储物品栈的完整数据
            CompoundTag itemTag = CodecUtils.toNBT(ItemStack.CODEC, itemStack)
                    .filter(CompoundTag.class::isInstance)
                    .map(CompoundTag.class::cast)
                    .orElseGet(CompoundTag::new);
            spiritPossessor.getPersistentData().put("original_item_stack", itemTag);
            
            // 应用化形属性
            TransformationUtils.setTransformationProperties(spiritPossessor);
            
            // 生成实体
            level.addFreshEntity(spiritPossessor);
            
            // 消耗物品
            itemStack.shrink(1);
            if (itemStack.isEmpty()) {
                itemEntity.remove(Entity.RemovalReason.DISCARDED);
            } else {
                itemEntity.setItem(itemStack);
            }
            
            return true;
            
        } catch (Exception e) {
            // 如果化形失败，尝试简单效果
            try {
                itemEntity.setGlowingTag(true);
                itemEntity.setPickUpDelay(20);
            } catch (Exception ex) {
                // 忽略可能的错误
            }
            return false;
        }
    }
    
    /**
     * 生成灵魂粒子效果
     * @param level 服务器世界
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param count 粒子数量
     */
    private static void spawnSoulParticles(ServerLevel level, double x, double y, double z, int count) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 1.0;
            double offsetY = (random.nextDouble() - 0.5) * 1.0;
            double offsetZ = (random.nextDouble() - 0.5) * 1.0;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x + offsetX, y + offsetY, z + offsetZ, 1, 0.0, 0.0, 0.0, 0.01);
        }
    }
    
    /**
     * 根据方块类型获取眼睛颜色
     * @param block 方块实例
     * @return 眼睛颜色索引
     */
    private static byte getEyeColorFromBlock(Block block) {
        // 实现7种颜色的眼睛瞳孔皮肤层
        // 简化实现：使用方块名称的哈希值来确定颜色
        String blockName = block.getClass().getSimpleName().toLowerCase();
        // 这里可以根据需要扩展颜色逻辑
        return 0; // 默认绿色（实际实现中会根据blockName计算）
    }
    
    /**
     * 根据物品类型获取眼睛颜色
     * @param itemStack 物品栈
     * @return 眼睛颜色索引
     */
    private static byte getItemEyeColor(ItemStack itemStack) {
        // 简化实现，返回默认颜色
        return 0; // 默认绿色
    }
    
    /**
     * 存储化形数据到实体
     * @param entity 化形实体
     * @param player 玩家实体
     * @param type 化形类型
     * @param eyeColor 眼睛颜色
     */
    private static void storeTransformationData(SpiritPossessor entity, Player player, String type, byte eyeColor) {
        // 直接使用setEyeColorType方法设置眼睛颜色
        entity.setEyeColorType(eyeColor);
        
        // 存储化形相关信息
        entity.getPersistentData().putString("transformation_type", type);
    }
    
    /**
     * 添加简单的灵魂效果到方块
     * @param level 游戏世界
     * @param pos 方块位置
     * @param state 方块状态
     * @param blockEntity 方块实体
     */
    private static void addSimpleSoulEffectToBlock(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        // 简化实现，不使用复杂的方块属性操作
        if (blockEntity != null) {
            // 尝试标记方块实体
            try {
                blockEntity.setChanged();
            } catch (Exception e) {
                // 忽略错误
            }
        }
    }
}
