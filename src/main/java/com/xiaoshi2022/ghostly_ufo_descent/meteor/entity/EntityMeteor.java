package com.xiaoshi2022.ghostly_ufo_descent.meteor.entity;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import com.xiaoshi2022.ghostly_ufo_descent.advancement.trigger.ModTriggers;
import com.xiaoshi2022.ghostly_ufo_descent.registry.BlockRegistry;
import com.xiaoshi2022.ghostly_ufo_descent.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.Random;

public class EntityMeteor extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(EntityMeteor.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SIZE_ID = SynchedEntityData.defineId(EntityMeteor.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation UFO = RawAnimation.begin().thenPlayAndHold("ufo");


    private Vec3 motion = new Vec3(0, 0, 0);
    private boolean inGround = false;

    public EntityMeteor(EntityType<? extends EntityMeteor> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public EntityMeteor(Level level, double x, double y, double z, Vec3 motion, int size) {
        super(EntityRegistry.METEOR.get(), level);
        this.setPos(x, y, z);
        this.motion = motion;
        this.setSize(size);
        this.setFuse(80);
        this.blocksBuilding = true;
    }

    public static EntityMeteor create(Level level, double x, double y, double z, Vec3 motion, int size) {
        return new EntityMeteor(level, x, y, z, motion, size);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FUSE_ID, 80);
        builder.define(DATA_SIZE_ID, 1);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        this.setFuse(in.getIntOr("Fuse", 80));
        this.setSize(in.getIntOr("Size", 1));
        this.inGround = in.getBooleanOr("InGround", false);

        // 读取运动数据
        this.motion = new Vec3(
                in.getDoubleOr("MotionX", 0),
                in.getDoubleOr("MotionY", 0),
                in.getDoubleOr("MotionZ", 0)
        );
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        out.putInt("Fuse", this.getFuse());
        out.putInt("Size", this.getSize());
        out.putBoolean("InGround", this.inGround);
        out.putDouble("MotionX", this.motion.x);
        out.putDouble("MotionY", this.motion.y);
        out.putDouble("MotionZ", this.motion.z);
    }

    public void setFuse(int fuse) {
        this.entityData.set(DATA_FUSE_ID, fuse);
    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE_ID);
    }

    public void setSize(int size) {
        this.entityData.set(DATA_SIZE_ID, Math.max(1, Math.min(3, size)));
    }

    public int getSize() {
        return this.entityData.get(DATA_SIZE_ID);
    }

    @Override
    public void tick() {
        super.tick();

        // 无论是否在地面，都要减少fuse时间，确保最终会爆炸
        int currentFuse = this.getFuse();
        if (currentFuse > 0) {
            this.setFuse(currentFuse - 1);
            if (this.getFuse() <= 0) {
                this.explode();
            }
        }

        if (!this.inGround) {
            Vec3 oldPos = this.position();

            // 检测碰撞
            Vec3 newPos = oldPos.add(this.motion);
            HitResult hitResult = this.level().clip(new ClipContext(oldPos, newPos,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onHit(hitResult);
            }

            // 更新位置
            this.setDeltaMovement(this.motion);
            this.move(MoverType.SELF, this.getDeltaMovement());

            // 检测与实体的碰撞 - 让Minecraft的成就系统自动检测伤害

            // 应用重力
            this.motion = this.motion.add(0, -0.04, 0);
        }

        // 生成粒子效果
        if (this.level().isClientSide()) {
            for (int i = 0; i < 5 * this.getSize(); i++) {
                this.level().addParticle(ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5) * 2,
                        this.getY() + this.random.nextDouble() * 2,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 2,
                        0, 0, 0);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
    }

    protected void onHit(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult) hitResult).getBlockPos();
            this.inGround = true;
            this.setPos(hitPos.getX() + 0.5, hitPos.getY() + 1, hitPos.getZ() + 0.5);
            this.motion = Vec3.ZERO;

            // 在碰撞位置放置火
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos pos = hitPos.offset(x, y, z);
                        if (this.level().getBlockState(pos).isAir()) {
                            this.level().setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                        }
                    }
                }
            }
        }
    }



    private void explode() {
        if (!this.level().isClientSide()) {
            GhostlyUFODescent.LOGGER.info("Meteor exploding at position: {}", this.blockPosition());

            // 生成陨石坑
            generateCrater();

            // 触发"流星撞击"成就
            triggerMeteorImpactAdvancement();

            float explosionPower = 2.0F * this.getSize();
            this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                    explosionPower, Level.ExplosionInteraction.TNT);

            this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.BLOCKS, 6.0F, (1.0F + (this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.2F) * 0.7F);

            this.discard();
        }
    }

    // 触发流星撞击成就
    private void triggerMeteorImpactAdvancement() {
        GhostlyUFODescent.LOGGER.info("开始尝试触发流星撞击成就");
        if (this.level() instanceof ServerLevel serverLevel) {
            // 获取爆炸位置周围的玩家
            AABB area = new AABB(this.blockPosition()).inflate(32.0); // 32格范围内的玩家
            var players = serverLevel.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, area);
            
            GhostlyUFODescent.LOGGER.info("在32格范围内找到 {} 名玩家", players.size());
            
            for (var player : players) {
                try {
                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        // 直接使用触发器，DeferredHolder.get()总是返回已注册的值
                        GhostlyUFODescent.LOGGER.info("尝试授予玩家 {} 流星撞击成就", player.getName().getString());
                        // 使用自定义触发器触发成就
                        ModTriggers.METEOR_IMPACT_TRIGGER.get().trigger(serverPlayer);
                        GhostlyUFODescent.LOGGER.info("成功授予玩家 {} 流星撞击成就", player.getName().getString());
                    }
                } catch (Exception e) {
                    GhostlyUFODescent.LOGGER.error("触发成就时出错: {}", e.getMessage());
                    e.printStackTrace(); // 打印完整堆栈跟踪
                }
            }
        } else {
            GhostlyUFODescent.LOGGER.warn("不在服务端，跳过成就触发");
        }
    }

    private void generateCrater() {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            // 增加陨石坑大小，使其更加壮观
            int baseSize = this.getSize() + 2;
            int craterSize = baseSize * 2; // 陨石坑大小翻倍
            BlockPos center = this.blockPosition();
            RandomSource random = this.random;

            // 创建主要陨石坑
            for (int x = -craterSize; x <= craterSize; x++) {
                for (int z = -craterSize; z <= craterSize; z++) {
                    // 计算水平距离
                    double horizontalDistance = Math.sqrt(x*x + z*z);
                    
                    // 根据距离计算深度，中心更深，边缘逐渐变浅
                    int maxDepth = (int) (baseSize * 1.5);
                    if (horizontalDistance < craterSize * 0.3) {
                        // 中心区域最深
                        maxDepth = (int) (baseSize * 2.5);
                    } else if (horizontalDistance < craterSize * 0.7) {
                        // 中间区域中等深度
                        maxDepth = (int) (baseSize * 1.8);
                    }
                    
                    // 根据距离计算应该破坏的方块范围
                    for (int y = 0; y > -maxDepth; y--) {
                        BlockPos pos = center.offset(x, y, z);
                        
                        // 使用二次函数使陨石坑形状更加自然
                        double effectiveDistance = Math.sqrt(x*x + z*z + (y * 2)*(y * 2));
                        double radiusFactor = (craterSize * 0.8) + (random.nextDouble() - 0.5) * craterSize * 0.4;
                        
                        if (effectiveDistance <= radiusFactor && this.level().isLoaded(pos)) {
                            // 70%的几率直接破坏方块
                            if (random.nextDouble() < 0.7) {
                                this.level().destroyBlock(pos, false);
                            } 
                            // 15%的几率放置岩浆
                            else if (random.nextDouble() < 0.214) { // 15% of 70% remaining
                                this.level().setBlockAndUpdate(pos, Blocks.MAGMA_BLOCK.defaultBlockState());
                            }
                            // 15%的几率放置下界岩
                            else {
                                this.level().setBlockAndUpdate(pos, Blocks.NETHERRACK.defaultBlockState());
                            }
                        }
                    }
                }
            }
            
            // 创建陨石坑边缘的隆起
            for (int x = -craterSize; x <= craterSize; x++) {
                for (int z = -craterSize; z <= craterSize; z++) {
                    double horizontalDistance = Math.sqrt(x*x + z*z);
                    
                    // 在陨石坑边缘区域创建隆起
                    if (horizontalDistance > craterSize * 0.7 && horizontalDistance < craterSize * 1.3) {
                        // 在边缘上方放置一些方块作为隆起
                        for (int y = 1; y <= 2; y++) {
                            BlockPos pos = center.offset(x, y, z);
                            
                            // 20%的几率在边缘形成隆起
                            if (random.nextDouble() < 0.2 && this.level().isLoaded(pos) && this.level().getBlockState(pos).isAir()) {
                                // 使用周围的方块或石头来形成自然的隆起
                                if (random.nextDouble() < 0.5) {
                                    this.level().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                                } else {
                                    this.level().setBlockAndUpdate(pos, Blocks.GRAVEL.defaultBlockState());
                                }
                            }
                        }
                    }
                }
            }
            
            // 在陨石坑底部和边缘增加更多岩浆池，同时尝试生成单个石棺
            boolean sarcophagusSpawned = false;
            
            for (int i = 0; i < craterSize * 2; i++) {
                int x = random.nextInt(craterSize * 2) - craterSize;
                int z = random.nextInt(craterSize * 2) - craterSize;
                int y = -random.nextInt((int)(baseSize * 2));
                
                BlockPos pos = center.offset(x, y, z);
                if (this.level().isLoaded(pos)) {
                    // 放置岩浆池，较大的陨石坑有更大的岩浆池
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos lavaPos = pos.offset(dx, 0, dz);
                            if (this.level().isLoaded(lavaPos) && this.level().getBlockState(lavaPos).isAir()) {
                                this.level().setBlockAndUpdate(lavaPos, Blocks.MAGMA_BLOCK.defaultBlockState());
                            }
                        }
                    }
                    
                    // 在生成岩浆池的同时，尝试在合适位置生成单个石棺
                    if (!sarcophagusSpawned && i == craterSize && this.level().isLoaded(pos) && 
                        this.level().isLoaded(pos.below()) && this.level().getBlockState(pos.below()).isSolid() && 
                        this.level().getBlockState(pos).isAir() && this.level().getBlockState(pos.above()).isAir()) {
                        
                        try {
                            GhostlyUFODescent.LOGGER.info("Attempting to spawn sarcophagus block during crater generation at: {}", pos);
                            
                            // 使用注册表API获取方块
                            ResourceLocation blockRL = ResourceLocation.fromNamespaceAndPath("ghostly_ufo_descent", "ghostly_sarcophagus_block");
                            GhostlyUFODescent.LOGGER.info("Looking up block registry: {}", blockRL);
                            Optional<net.minecraft.world.level.block.Block> sarcophagusBlockOpt = BuiltInRegistries.BLOCK.getOptional(blockRL);

                            if (sarcophagusBlockOpt.isPresent()) {
                                var sarcophagusBlock = sarcophagusBlockOpt.get();
                                GhostlyUFODescent.LOGGER.info("Found sarcophagus block in registry");
                                // 放置方块
                                serverLevel.setBlockAndUpdate(pos, sarcophagusBlock.defaultBlockState());
                                GhostlyUFODescent.LOGGER.info("Spawned GhostlySarcophagus_block at {}", pos);
                                sarcophagusSpawned = true;
                            } else {
                                GhostlyUFODescent.LOGGER.warn("GhostlySarcophagus_block not found in registry");
                            }
                        } catch (Exception e) {
                            GhostlyUFODescent.LOGGER.error("Failed to spawn GhostlySarcophagus_block during crater generation: {}", e.getMessage());
                        }
                    }
                }
            }
            
            // 如果在岩浆池生成过程中没有成功放置石棺，使用备用位置
            if (!sarcophagusSpawned) {
                GhostlyUFODescent.LOGGER.info("No sarcophagus spawned during crater generation, using emergency position");
                
                // 尝试在陨石坑底部找到一个合适的位置
                for (int y = 0; y > -15; y--) {
                    BlockPos emergencyPos = center.offset(0, y, 0);
                    if (serverLevel.isLoaded(emergencyPos) && 
                        serverLevel.isLoaded(emergencyPos.below()) && 
                        serverLevel.getBlockState(emergencyPos.below()).isSolid()) {
                        
                        // 清除上方方块以确保放置成功
                        for (int dy = 0; dy <= 1; dy++) {
                            BlockPos clearPos = emergencyPos.offset(0, dy, 0);
                            if (serverLevel.isLoaded(clearPos)) {
                                serverLevel.removeBlock(clearPos, false);
                            }
                        }
                        
                        try {
                            // 使用BlockRegistry中定义的Supplier获取方块
                            var sarcophagusBlock = BlockRegistry.GHOSTLY_SARCOPHAGUS_BLOCK.get();
                            serverLevel.setBlockAndUpdate(emergencyPos, sarcophagusBlock.defaultBlockState());
                            GhostlyUFODescent.LOGGER.info("Spawned GhostlySarcophagus_block at emergency position: {}", emergencyPos);
                            sarcophagusSpawned = true;
                            break;
                        } catch (Exception e) {
                            GhostlyUFODescent.LOGGER.error("Failed to spawn sarcophagus at emergency position: {}", e.getMessage());
                        }
                    }
                }
            }

            // 根据概率生成实体
            // 40%概率生成UfoPangenas
            if (random.nextDouble() < 0.4) {
                spawnUfoPangenas(center, serverLevel);
            }
            // 50%概率生成SporeStarPerson
            if (random.nextDouble() < 0.5) {
                spawnSporeStarPerson(center, serverLevel);
            }
        }
    }

//    private void spawnSarcophagusBlock(BlockPos center, ServerLevel serverLevel) {
//        try {
//            GhostlyUFODescent.LOGGER.info("Attempting to spawn sarcophagus block at crater center: {}", center);
//
//            // 查找合适的放置位置
//            BlockPos spawnPos = findValidSpawnPos(center, serverLevel);
//
//            if (spawnPos != null) {
//                GhostlyUFODescent.LOGGER.info("Found valid spawn position: {}", spawnPos);
//                try {
//                    // 使用BlockRegistry中定义的Supplier获取方块
//                    var sarcophagusBlock = BlockRegistry.GHOSTLY_SARCOPHAGUS_BLOCK.get();
//                    GhostlyUFODescent.LOGGER.info("Successfully retrieved sarcophagus block from registry");
//                    // 放置方块
//                    serverLevel.setBlockAndUpdate(spawnPos, sarcophagusBlock.defaultBlockState());
//                    GhostlyUFODescent.LOGGER.info("Spawned GhostlySarcophagus_block at {}", spawnPos);
//                } catch (Exception e) {
//                    GhostlyUFODescent.LOGGER.error("Failed to retrieve or place sarcophagus block: {}", e.getMessage());
//                    e.printStackTrace();
//                }
//            } else {
//                GhostlyUFODescent.LOGGER.warn("No valid spawn position found for sarcophagus within search range");
//                // 调试陨石坑底部情况
//                for (int y = 0; y > -10; y--) {
//                    BlockPos testPos = center.offset(0, y, 0);
//                    if (serverLevel.isLoaded(testPos)) {
//                        GhostlyUFODescent.LOGGER.warn("Block at {}: {}, Solid: {}", testPos, serverLevel.getBlockState(testPos).getBlock(), serverLevel.getBlockState(testPos).isSolid());
//                    }
//                }
//            }
//        } catch (Exception e) {
//            GhostlyUFODescent.LOGGER.error("Failed to spawn GhostlySarcophagus_block: {}", e.getMessage());
//            e.printStackTrace();
//        }
//    }

    private void spawnUfoPangenas(BlockPos center, ServerLevel serverLevel) {
        try {
            // 查找合适的生成位置
            BlockPos spawnPos = findValidSpawnPos(center, serverLevel);
            if (spawnPos != null) {
                // 使用EntityRegistry中定义的Supplier获取实体类型
                var ufoType = EntityRegistry.UFO_PANGENAS.get();
                // 直接使用 spawn 方法生成实体
                net.minecraft.world.entity.Entity ufo = ufoType.spawn(serverLevel, spawnPos, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                if (ufo != null) {
                    GhostlyUFODescent.LOGGER.info("Spawned UfoPangenas at {}", spawnPos);
                } else {
                    GhostlyUFODescent.LOGGER.warn("Failed to spawn UfoPangenas at {}", spawnPos);
                }
            }
        } catch (Exception e) {
            GhostlyUFODescent.LOGGER.error("Failed to spawn UfoPangenas: {}", e.getMessage());
        }
    }

    private void spawnSporeStarPerson(BlockPos center, ServerLevel serverLevel) {
        try {
            // 查找合适的生成位置
            BlockPos spawnPos = findValidSpawnPos(center, serverLevel);
            if (spawnPos != null) {
                // 使用EntityRegistry中定义的Supplier获取实体类型
                var sporeType = EntityRegistry.SPORE_STAR_PERSON.get();
                // 直接使用 spawn 方法生成实体
                net.minecraft.world.entity.Entity spore = sporeType.spawn(serverLevel, spawnPos, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                if (spore != null) {
                    GhostlyUFODescent.LOGGER.info("Spawned SporeStarPerson at {}", spawnPos);
                } else {
                    GhostlyUFODescent.LOGGER.warn("Failed to spawn SporeStarPerson at {}", spawnPos);
                }
            }
        } catch (Exception e) {
            GhostlyUFODescent.LOGGER.error("Failed to spawn SporeStarPerson: {}", e.getMessage());
        }
    }

    private BlockPos findValidSpawnPos(BlockPos center, ServerLevel serverLevel) {
        GhostlyUFODescent.LOGGER.info("Searching for valid spawn position around center: {}", center);
        
        // 检查中心位置是否有效
        if (isValidSpawnPos(center, serverLevel)) {
            GhostlyUFODescent.LOGGER.info("Center position is valid: {}", center);
            return center;
        }

        // 搜索周围10格范围内的有效位置
        int attempts = 0;
        int validChecks = 0;
        
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                for (int y = -5; y <= 5; y++) {
                    attempts++;
                    BlockPos pos = center.offset(x, y, z);
                    if (isValidSpawnPos(pos, serverLevel)) {
                        validChecks++;
                        GhostlyUFODescent.LOGGER.info("Found valid spawn position at: {}", pos);
                        return pos;
                    }
                }
            }
        }
        
        GhostlyUFODescent.LOGGER.warn("No valid spawn position found after {} attempts. {} positions passed validation checks.", attempts, validChecks);
        return null;
    }

    private boolean isValidSpawnPos(BlockPos pos, ServerLevel serverLevel) {
        // 检查位置是否加载
        if (!serverLevel.isLoaded(pos)) {
            return false;
        }

        // 检查脚下是否有方块
        BlockPos below = pos.below();
        boolean hasSolidBelow = serverLevel.getBlockState(below).isSolid();
        
        // 检查自身和上方是否有足够空间
        boolean hasAirAtPos = serverLevel.getBlockState(pos).isAir();
        boolean hasAirAbove = serverLevel.getBlockState(pos.above()).isAir();
        
        boolean isValid = hasSolidBelow && hasAirAtPos && hasAirAbove;
        
        // 每50次检查输出一次详细信息，避免日志过多
        if (new Random().nextInt(50) == 0) {
            GhostlyUFODescent.LOGGER.debug("Checking position {}: solid_below={}, air_pos={}, air_above={}, result={}", 
                pos, hasSolidBelow, hasAirAtPos, hasAirAbove, isValid);
        }
        
        return isValid;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>( "ufos_controller", 20, state -> {
            // 自动循环播放ufo动画
            state.setAnimation(UFO);
            return PlayState.CONTINUE;
        }).triggerableAnim("ufo", UFO));
        // 自动播放并同时保留可手动触发的功能
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // Using default bounding box implementation from Entity class


}