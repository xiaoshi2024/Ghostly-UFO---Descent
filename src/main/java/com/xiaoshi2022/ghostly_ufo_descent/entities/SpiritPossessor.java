package com.xiaoshi2022.ghostly_ufo_descent.entities;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import com.xiaoshi2022.ghostly_ufo_descent.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SpiritPossessor extends Zombie {

    private static final EntityDataAccessor<UUID> PLAYER_UUID = SynchedEntityData.defineId(SpiritPossessor.class, GhostlyUFODescent.UUID_SERIALIZER.get());
    private static final EntityDataAccessor<String> PLAYER_NAME = SynchedEntityData.defineId(SpiritPossessor.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> EYE_COLOR_TYPE = SynchedEntityData.defineId(SpiritPossessor.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> IS_GLOWING = SynchedEntityData.defineId(SpiritPossessor.class, EntityDataSerializers.BOOLEAN);

    public SpiritPossessor(EntityType<? extends SpiritPossessor> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(false);
        this.setPersistenceRequired();
    }

    // 移除第二个构造函数，因为它在实体注册时会造成混淆
    public static SpiritPossessor createWithPlayer(Level level, Player player) {
        SpiritPossessor spiritPossessor = new SpiritPossessor(EntityRegistry.SPIRIT_POSSESSOR.get(), level);
        spiritPossessor.setPlayerData(player);
        spiritPossessor.setEyeColorType((byte) (level.random.nextInt(7))); // 随机眼睛颜色类型 (0-6)
        return spiritPossessor;
    }
    
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // 覆灵者不应该装备任何物品，所以覆盖此方法为空实现
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PLAYER_UUID, UUID.randomUUID());
        builder.define(PLAYER_NAME, "Unknown");
        builder.define(EYE_COLOR_TYPE, (byte) 0);
        builder.define(IS_GLOWING, true);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        // 添加更多自然的行为目标
        this.goalSelector.addGoal(1, new FloatGoal(this)); // 游泳目标
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    public void setPlayerData(Player player) {
        this.entityData.set(PLAYER_UUID, player.getUUID());
        this.entityData.set(PLAYER_NAME, player.getName().getString());
    }

    public void setPlayerData(UUID uuid, String name) {
        this.entityData.set(PLAYER_UUID, uuid);
        this.entityData.set(PLAYER_NAME, name);
    }

    public UUID getPlayerUuid() {
        return this.entityData.get(PLAYER_UUID);
    }

    public String getPlayerName() {
        return this.entityData.get(PLAYER_NAME);
    }

    public byte getEyeColorType() {
        return this.entityData.get(EYE_COLOR_TYPE);
    }

    public void setEyeColorType(byte type) {
        this.entityData.set(EYE_COLOR_TYPE, type);
    }

    public boolean isGlowing() {
        return this.entityData.get(IS_GLOWING);
    }

    public void setIsGlowing(boolean glowing) {
        this.entityData.set(IS_GLOWING, glowing);
    }

    @Override
    public void tick() {
        super.tick();

        // 发光效果
        if (this.isGlowing()) {
            this.setGlowingTag(true);
        } else {
            this.setGlowingTag(false);
        }

        // 随机移动逻辑
        if (this.level().isClientSide()) {
            // 粒子效果
            if (random.nextDouble() < 0.1) {
                Vec3 particlePos = new Vec3(
                        getX() + (random.nextDouble() - 0.5) * getBbWidth(),
                        getY() + (random.nextDouble() - 0.5) * getBbHeight(),
                        getZ() + (random.nextDouble() - 0.5) * getBbWidth()
                );
                level().addParticle(ParticleTypes.SOUL, particlePos.x, particlePos.y, particlePos.z, 0, 0.01, 0);
            }
        } else {
            // 每100刻（5秒）随机改变眼睛颜色
            if (this.tickCount % 100 == 0) {
                this.setEyeColorType((byte) (random.nextInt(7)));
            }
        }
    }
    
    @Override
    public void aiStep() {
        super.aiStep();
        
        // 添加更多的灵魂粒子效果
        if (this.level().isClientSide() && this.isGlowing()) {
            if (random.nextDouble() < 0.2) {
                Vec3 particlePos = new Vec3(
                        getX() + (random.nextDouble() - 0.5) * getBbWidth(),
                        getY() + (random.nextDouble() - 0.5) * getBbHeight(),
                        getZ() + (random.nextDouble() - 0.5) * getBbWidth()
                );
                level().addParticle(ParticleTypes.SOUL, particlePos.x, particlePos.y, particlePos.z, 0, 0.02, 0);
            }
        }
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return super.canCollideWith(entity) && !(entity instanceof Player);
    }

    // 我们不需要自定义的hurt方法，让它使用父类的默认实现

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            // 与玩家交互时改变眼睛颜色
            this.setEyeColorType((byte) ((this.getEyeColorType() + 1) % 7));
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal("覆灵者的眼睛颜色改变了！"));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
    
    @Override
    protected boolean convertsInWater() {
        // 覆灵者不会在水中转化为溺尸
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putString("PlayerUUID", this.getPlayerUuid().toString());
        out.putString("PlayerName", this.getPlayerName());
        out.putByte("EyeColorType", this.getEyeColorType());
        out.putBoolean("IsGlowing", this.isGlowing());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        this.entityData.set(PLAYER_UUID, UUID.fromString(in.getStringOr("PlayerUUID", UUID.randomUUID().toString())));
        this.entityData.set(PLAYER_NAME, in.getStringOr("PlayerName", "Unknown"));
        this.entityData.set(EYE_COLOR_TYPE, in.getByteOr("EyeColorType", (byte)0));
        this.entityData.set(IS_GLOWING, in.getBooleanOr("IsGlowing", false));
    }

    // 移除这两个方法，因为它们在父类中是final的
    // 实体尺寸和眼睛高度已经在EntityRegistry中通过.sized(0.6f, 1.8f)和.eyeHeight(1.62f)设置了


}