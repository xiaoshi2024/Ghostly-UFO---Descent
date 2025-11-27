package com.xiaoshi2022.ghostly_ufo_descent.transformation;

import com.xiaoshi2022.ghostly_ufo_descent.api.codec.CodecUtils;
import com.xiaoshi2022.ghostly_ufo_descent.entities.SpiritPossessor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;

/**
 * 化形工具类，负责处理化形后的特殊属性和行为
 */
public class TransformationUtils {
    
    /**
     * 设置化形实体的属性和装备
     * @param spiritPossessor 覆灵者实体
     */
    public static void setTransformationProperties(SpiritPossessor spiritPossessor) {
        // 重置所有属性到默认值
        resetDefaultAttributes(spiritPossessor);
        
        // 检查化形类型
        if (spiritPossessor.getPersistentData().contains("transformation_type")) {
            String transformationType = spiritPossessor.getPersistentData().getString("transformation_type").orElse("unknown");
            if (transformationType.equals("item")) {
                // 物品化形：让覆灵者手持物品
                equipItemForTransformation(spiritPossessor);
            } else if (transformationType.equals("block")) {
                // 方块化形：根据方块硬度设置属性
                setPropertiesFromBlock(spiritPossessor);
            }
        }
    }
    
    /**
     * 为物品化形的覆灵者装备对应的物品
     * @param spiritPossessor 覆灵者实体
     */
    private static void equipItemForTransformation(SpiritPossessor spiritPossessor) {
        if (spiritPossessor.getPersistentData().contains("original_item_stack")) {
            spiritPossessor.getPersistentData().getCompound("original_item_stack").ifPresent(itemTag -> {
                ItemStack itemStack = CodecUtils.fromNBT(ItemStack.CODEC, itemTag).orElse(ItemStack.EMPTY);
                
                if (!itemStack.isEmpty()) {
                    // 让覆灵者手持该物品
                    spiritPossessor.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
                    
                    // 播放装备声音
                    spiritPossessor.level().playSound(null, spiritPossessor.getX(), spiritPossessor.getY(), spiritPossessor.getZ(), 
                            SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 0.5F, 1.0F);
                }
            });
        }
    }
    
    /**
     * 根据方块化形设置覆灵者的属性（抗性和攻击力）
     * @param spiritPossessor 覆灵者实体
     */
    private static void setPropertiesFromBlock(SpiritPossessor spiritPossessor) {
        if (spiritPossessor.getPersistentData().contains("original_block")) {
            spiritPossessor.getPersistentData().getString("original_block").ifPresent(originalBlockId -> {
                // 尝试获取方块实例
                ResourceLocation blockRL = ResourceLocation.tryParse(originalBlockId);
                if (blockRL != null) {
                    BuiltInRegistries.BLOCK.get(blockRL).ifPresent(blockHolder -> {
                        Block block = blockHolder.value();
                        BlockState defaultState = block.defaultBlockState();
                        
                        // 获取方块硬度
                        float hardness = defaultState.getDestroySpeed(null, null);
                        
                        // 根据硬度计算属性加成
                        float healthMultiplier = 1.0F + (hardness * 0.2F); // 每点硬度增加20%生命值
                        float damageBonus = hardness * 0.5F; // 每点硬度增加0.5点攻击力
                        
                        // 限制最大加成，避免过于强大
                        healthMultiplier = Math.min(healthMultiplier, 5.0F); // 最大5倍生命值
                        damageBonus = Math.min(damageBonus, 10.0F); // 最大额外10点攻击力
                        
                        // 应用属性加成
                        applyBlockBasedAttributes(spiritPossessor, healthMultiplier, damageBonus);
                        
                        // 生成粒子效果表示属性变化
                        if (spiritPossessor.level() instanceof ServerLevel serverLevel) {
                            spawnTransformationParticles(serverLevel, spiritPossessor.getX(), spiritPossessor.getY(), spiritPossessor.getZ());
                        }
                    });
                }
            });
        }
    }
    
    /**
     * 应用基于方块的属性加成
     * @param spiritPossessor 覆灵者实体
     * @param healthMultiplier 生命值倍数
     * @param damageBonus 额外攻击力
     */
    private static void applyBlockBasedAttributes(SpiritPossessor spiritPossessor, float healthMultiplier, float damageBonus) {
        // 获取属性实例
        AttributeInstance healthAttribute = spiritPossessor.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance damageAttribute = spiritPossessor.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armorAttribute = spiritPossessor.getAttribute(Attributes.ARMOR);
        
        // 应用生命值加成
        if (healthAttribute != null) {
            // 获取基础生命值 (默认20.0)
            double baseHealth = healthAttribute.getBaseValue() / healthAttribute.getBaseValue();
            healthAttribute.setBaseValue(20.0 * healthMultiplier);
            // 更新当前生命值
            spiritPossessor.setHealth((float)(20.0 * healthMultiplier));
        }
        
        // 应用攻击力加成
        if (damageAttribute != null) {
            damageAttribute.setBaseValue(3.0 + damageBonus);
        }
        
        // 应用护甲加成（基于方块硬度）
        if (armorAttribute != null) {
            float armorBonus = damageBonus / 2.0F; // 护甲加成为攻击力加成的一半
            armorAttribute.setBaseValue(armorAttribute.getBaseValue() + armorBonus);
        }
    }
    
    /**
     * 重置覆灵者的属性到默认值
     * @param spiritPossessor 覆灵者实体
     */
    private static void resetDefaultAttributes(SpiritPossessor spiritPossessor) {
        // 清除所有装备
        spiritPossessor.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        spiritPossessor.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        spiritPossessor.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        spiritPossessor.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        spiritPossessor.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        spiritPossessor.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        
        // 重置属性到默认值
        AttributeInstance healthAttribute = spiritPossessor.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance damageAttribute = spiritPossessor.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armorAttribute = spiritPossessor.getAttribute(Attributes.ARMOR);
        
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(20.0);
            spiritPossessor.setHealth(20.0F);
        }
        
        if (damageAttribute != null) {
            damageAttribute.setBaseValue(3.0);
        }
        
        if (armorAttribute != null) {
            armorAttribute.setBaseValue(0.0);
        }
    }
    
    /**
     * 生成化形效果粒子
     * @param level 服务器世界
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     */
    private static void spawnTransformationParticles(ServerLevel level, double x, double y, double z) {
        RandomSource random = level.random;
        // 生成方块属性相关的粒子效果
        for (int i = 0; i < 10; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 1.0;
            double offsetY = (random.nextDouble() - 0.5) * 1.0 + 0.5; // 集中在身体部位
            double offsetZ = (random.nextDouble() - 0.5) * 1.0;
            
            // 根据方块属性选择粒子颜色（这里使用灵魂火粒子作为基础）
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x + offsetX, y + offsetY, z + offsetZ, 1, 0.0, 0.02, 0.0, 0.01);
        }
    }
}