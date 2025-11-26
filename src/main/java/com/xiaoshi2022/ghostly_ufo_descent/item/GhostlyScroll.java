package com.xiaoshi2022.ghostly_ufo_descent.item;

import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.item.GhostlyScrollRenderer;
import com.xiaoshi2022.ghostly_ufo_descent.entities.SpiritPossessor;
import com.xiaoshi2022.ghostly_ufo_descent.registry.EntityRegistry;
import com.xiaoshi2022.ghostly_ufo_descent.transformation.TransformationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.particles.ParticleTypes;
import java.util.EnumMap;
import org.jetbrains.annotations.NotNull;
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
import java.util.Random;

public class GhostlyScroll extends Item implements GeoItem {
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("open");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GhostlyScroll(Properties properties) {
        super(properties.durability(100)); // 设置总耐久为100点

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
        // 只有在副手使用时才触发覆灵技能
        if (hand == InteractionHand.OFF_HAND) {
            if (level instanceof ServerLevel serverLevel) {
                ItemStack scrollStack = player.getItemInHand(hand);
                
                // 检查耐久度是否足够
                if (scrollStack.getDamageValue() + 20 <= scrollStack.getMaxDamage()) {
                    triggerAnim(player, GeoItem.getOrAssignId(scrollStack, serverLevel), "popup_controller", "open");
                    
                    // 播放使用声音
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    // 扣除耐久度（20点）
                    scrollStack.hurtAndBreak(20, player, hand);
                    
                    // 检查玩家主手是否持有物品
                    ItemStack mainHandItem = player.getMainHandItem();
                    if (!mainHandItem.isEmpty()) {
                        // 主手持有物品，为其添加灵魂效果
                        
                        // 创建临时物品实体以应用效果
                        ItemEntity tempItemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), mainHandItem.copy());
                        addSoulEffectToItem(player, level, tempItemEntity);
                        
                        // 将效果应用回玩家主手的物品
                        player.setItemInHand(InteractionHand.MAIN_HAND, tempItemEntity.getItem());
                        
                        // 显示成功消息
                        player.displayClientMessage(Component.translatable("message.ghostly_ufo_descent.soul_applied_main_hand"), false);
                    } else {
                        // 主手没有物品，尝试瞄准目标
                        HitResult hitResult = player.pick(5.0D, 0.0F, false);
                        
                        // 处理目标（物品或方块）
                        if (hitResult.getType() == HitResult.Type.BLOCK) {
                            // 处理方块目标
                            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                            BlockPos pos = blockHitResult.getBlockPos();
                            BlockState state = level.getBlockState(pos);
                            Block block = state.getBlock();
                            BlockEntity blockEntity = level.getBlockEntity(pos);
                            
                            // 为方块添加灵魂效果（示例：添加发光效果）
                            addSoulEffectToBlock(player, level, pos, state, block, blockEntity);
                            
                            // 显示成功消息
                            player.displayClientMessage(Component.translatable("message.ghostly_ufo_descent.soul_transform_block_success"), false);
                        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                            // 处理实体目标（检查是否为物品实体）
                            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                            if (entityHitResult.getEntity() instanceof ItemEntity itemEntity) {
                                // 为物品添加灵魂效果
                                addSoulEffectToItem(player, level, itemEntity);
                                
                                // 显示成功消息
                                player.displayClientMessage(Component.translatable("message.ghostly_ufo_descent.soul_applied_entity"), false);
                            }
                        } else {
                            // 没有命中任何目标
                            player.displayClientMessage(Component.literal("请在主手持有物品，或瞄准一个物品/方块来使用覆灵技能。"), true);
                        }
                    }
                } else {
                    // 耐久度不足
                    player.displayClientMessage(Component.literal("鬼怪卷轴耐久度不足！"), true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }

            player.startUsingItem(hand);
            return InteractionResult.SUCCESS;
        } else {
            // 主手使用时返回默认行为
            return InteractionResult.PASS;
        }
    }
    
    /**
      * 为方块添加灵魂效果并实现Transformation机制
      */
     private void addSoulEffectToBlock(Player player, Level level, BlockPos pos, BlockState state, Block block, BlockEntity blockEntity) {
         if (level instanceof ServerLevel serverLevel) {
             // 创建粒子效果
             RandomSource random = level.random;
             for (int i = 0; i < 15; i++) {
                 double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.0;
                 double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 1.0;
                 double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.0;
                 serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0.0, 0.0, 0.0, 0.01);
             }
              
             // 播放化形声音
                    level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.BLOCKS, 1.0F, 1.0F);
             
             // Transformation机制：方块化形为玩家形象
             try {
                 // 获取基于方块类型的眼睛颜色
                 byte eyeColor = getEyeColorFromBlock(block);
                 
                 // 创建玩家装备的副本
                 EnumMap<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
                 for (EquipmentSlot slot : EquipmentSlot.values()) {
                     equipment.put(slot, player.getItemBySlot(slot).copy());
                 }
                 
                 // 创建化形实体（使用SpiritPossessor类）
                 SpiritPossessor transformedEntity = SpiritPossessor.createWithPlayer(level, player);
                 
                 // 设置位置（方块中心）
                 transformedEntity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                 transformedEntity.setYRot(player.getYRot());
                 
                 // 设置眼睛颜色
                 transformedEntity.setEyeColorType(eyeColor);
                 
                 // 存储化形相关信息
                 transformedEntity.getPersistentData().putString("transformation_type", "block");
                 transformedEntity.getPersistentData().putString("original_block", block.getName().toString());
                 
                 // 生成实体
                 level.addFreshEntity(transformedEntity);
                 
                 // 移除原方块
                 level.removeBlock(pos, false);
                 
             } catch (Exception e) {
                 // 如果化形失败，尝试添加简单的灵魂效果
                 try {
                     // 尝试为方块添加发光效果 - 使用更安全的方式
                     BooleanProperty litProperty = BooleanProperty.create("lit");
                     BooleanProperty poweredProperty = BooleanProperty.create("powered");
                     
                     if (state.hasProperty(litProperty)) {
                         level.setBlock(pos, state.setValue(litProperty, true), 3);
                     } else if (state.hasProperty(poweredProperty)) {
                         level.setBlock(pos, state.setValue(poweredProperty, true), 3);
                     } else {
                         // 如果方块没有这些属性，可以考虑其他方式添加效果
                         if (blockEntity != null) {
                             // 为方块实体添加灵魂标记
                             blockEntity.getPersistentData().putBoolean("has_soul", true);
                             blockEntity.setChanged();
                         }
                     }
                 } catch (Exception ex) {
                     // 忽略可能的错误
                 }
             }
         }
     }
     
     /**
      * 根据方块类型获取眼睛颜色
      */
     private byte getEyeColorFromBlock(Block block) {
         // 实现7种颜色的眼睛瞳孔皮肤层
         String blockName = block.getName().toString().toLowerCase();
         
         if (blockName.contains("grass") || blockName.contains("green")) {
             return 0; // 绿色
         } else if (blockName.contains("red") || blockName.contains("rose") || blockName.contains("nether") || blockName.contains("lava")) {
             return 1; // 红色
         } else if (blockName.contains("blue") || blockName.contains("water") || blockName.contains("ocean")) {
             return 2; // 蓝色
         } else if (blockName.contains("yellow") || blockName.contains("gold") || blockName.contains("sun")) {
             return 3; // 黄色
         } else if (blockName.contains("purple") || blockName.contains("amethyst") || blockName.contains("ender")) {
             return 4; // 紫色
         } else if (blockName.contains("orange") || blockName.contains("pumpkin")) {
             return 5; // 橙色
         } else {
             return 6; // 默认颜色（黑色或灰色）
         }
     }
    
    /**
      * 为物品添加灵魂效果并实现Transformation机制
      */
     private void addSoulEffectToItem(Player player, Level level, ItemEntity itemEntity) {
         // 使用化形管理器处理物品化形
         boolean success = TransformationManager.transformItem(player, level, itemEntity);
          
         // 发送系统消息
            if (success) {
                player.displayClientMessage(Component.translatable("message.ghostly_ufo_descent.soul_transform_item_success"), false);
            } else {
                player.displayClientMessage(Component.translatable("message.ghostly_ufo_descent.soul_transform_item_failed"), false);
            }
     }
    
    // 移除了复杂的方块属性操作方法，使用更安全的方式

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
