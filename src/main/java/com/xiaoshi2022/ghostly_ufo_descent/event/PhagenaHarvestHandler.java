package com.xiaoshi2022.ghostly_ufo_descent.event;

import com.xiaoshi2022.ghostly_ufo_descent.block.custom.Phagenabotanys;
import com.xiaoshi2022.ghostly_ufo_descent.entities.SporeStarPerson;
import com.xiaoshi2022.ghostly_ufo_descent.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = "ghostly_ufo_descent")
public class PhagenaHarvestHandler {
    
    // 用于跟踪每个维度中成熟的极噬种子位置
    private static final Map<String, Set<BlockPos>> MATURE_CROPS = new HashMap<>();
    private static final int CHECK_INTERVAL = 20; // 每秒检查一次
    private static final int AUTO_SPAWN_TICKS = 600; // 30秒后尝试自动生成
    private static final double AUTO_SPAWN_CHANCE = 0.3; // 30%几率自动生成

    /**
     * 处理玩家右键成熟极噬种子事件
     */
    @SubscribeEvent
    public static void onRightClickMatureCrop(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        // 检查是否是成熟的极噬种子
        if (state.getBlock() instanceof Phagenabotanys crop && state.getValue(crop.getAgeProperty()) >= crop.getMaxAge()) {
            if (!level.isClientSide()) {
                // 生成孢子星人
                if (level instanceof ServerLevel serverLevel) {
                    spawnSporeStarPerson(serverLevel, pos);
                    
                    // 移除成熟的作物
                    level.removeBlock(pos, false);
                    
                    // 从跟踪集合中移除
                    String levelId = level.dimension().location().toString();
                    if (MATURE_CROPS.containsKey(levelId)) {
                        MATURE_CROPS.get(levelId).remove(pos);
                    }
                }
            }
            
            event.setCanceled(true);
        }
    }

    /**
     * 在指定位置生成孢子星人
     */
    private static void spawnSporeStarPerson(ServerLevel level, BlockPos pos) {
        // 在作物上方生成孢子星人
        BlockPos spawnPos = pos.above(1);

        // 创建孢子星人实体
        SporeStarPerson sporeStarPerson = new SporeStarPerson(EntityRegistry.SPORE_STAR_PERSON.get(), level);
        
        // 玩家种植的孢子星人有60%概率成为族长
        if (level.getRandom().nextDouble() < 0.6) {
            sporeStarPerson.setElder(true);
        }
        
        sporeStarPerson.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);

        // 生成实体
        level.addFreshEntity(sporeStarPerson);

        // 播放生成音效
        level.playSound(null, spawnPos,
                net.minecraft.sounds.SoundEvents.ZOMBIE_INFECT,
                net.minecraft.sounds.SoundSource.HOSTILE,
                1.0F, 1.0F);
    }

    /**
     * 当方块状态改变时，检查是否有新的成熟极噬种子
     */
    @SubscribeEvent
    public static void onBlockStateChange(BlockEvent.EntityPlaceEvent event) {
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        
        // 检查是否是成熟的极噬种子
        if (state.getBlock() instanceof Phagenabotanys crop && state.getValue(crop.getAgeProperty()) >= crop.getMaxAge()) {
            String levelId = level.dimension().location().toString();
            MATURE_CROPS.computeIfAbsent(levelId, k -> new HashSet<>()).add(pos);
        }
    }

    /**
     * 监听方块邻居通知事件，跟踪成熟的极噬种子
     */
    @SubscribeEvent
    public static void onBlockUpdate(BlockEvent.NeighborNotifyEvent event) {
        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        String levelId = level.dimension().location().toString();
        
        // 检查是否是成熟的极噬种子
        if (state.getBlock() instanceof Phagenabotanys crop && state.getValue(crop.getAgeProperty()) >= crop.getMaxAge()) {
            MATURE_CROPS.computeIfAbsent(levelId, k -> new HashSet<>()).add(pos);
        } else {
            // 如果不是成熟的作物，从跟踪集合中移除
            if (MATURE_CROPS.containsKey(levelId)) {
                MATURE_CROPS.get(levelId).remove(pos);
            }
        }
    }

    /**
     * 处理世界tick事件，实现作物自动生成孢子星人的功能
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        
        String levelId = level.dimension().location().toString();
        
        // 每20个tick（1秒）检查一次
        if (level.getGameTime() % CHECK_INTERVAL == 0) {
            // 获取当前维度的成熟作物集合
            Set<BlockPos> matureCrops = MATURE_CROPS.getOrDefault(levelId, new HashSet<>());
            
            // 创建一个临时集合来存储需要移除的作物位置
            Set<BlockPos> toRemove = new HashSet<>();
            
            // 遍历所有成熟的作物
            for (BlockPos pos : matureCrops) {
                // 检查方块是否仍然存在且成熟
                if (level.isLoaded(pos)) {
                    BlockState state = level.getBlockState(pos);
                    
                    if (state.getBlock() instanceof Phagenabotanys crop && state.getValue(crop.getAgeProperty()) >= crop.getMaxAge()) {
                        // 计算方块的游戏时间，用于确定是否应该尝试生成
                        long blockAge = level.getGameTime() - (pos.hashCode() & 0xFFFF); // 使用位置哈希作为种子
                        
                        // 每30秒尝试一次生成
                        if (blockAge % AUTO_SPAWN_TICKS == 0) {
                            RandomSource random = level.getRandom();
                            if (random.nextDouble() < AUTO_SPAWN_CHANCE && level instanceof ServerLevel serverLevel) {
                                // 确保在服务端生成实体
                                spawnSporeStarPerson(serverLevel, pos);
                                level.removeBlock(pos, false);
                                toRemove.add(pos);
                            }
                        }
                    } else {
                        // 如果方块不再是成熟的极噬种子，从跟踪集合中移除
                        toRemove.add(pos);
                    }
                } else {
                    // 如果方块不在已加载的区块中，从跟踪集合中移除
                    toRemove.add(pos);
                }
            }
            
            // 移除不需要跟踪的作物
            matureCrops.removeAll(toRemove);
            
            // 如果没有成熟作物，移除整个维度的映射
            if (matureCrops.isEmpty()) {
                MATURE_CROPS.remove(levelId);
            }
        }
    }
}