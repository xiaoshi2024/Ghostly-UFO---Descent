package com.xiaoshi2022.ghostly_ufo_descent.entities;

import com.mojang.logging.LogUtils;
import com.xiaoshi2022.ghostly_ufo_descent.registry.SoundRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 极噬者UFO实体类
 * 作为模组中的Boss级实体，代表着高级外星文明的终极武器
 * 具有强大的攻击能力和特殊技能
 */
public class UfoPangenas extends PathfinderMob implements GeoEntity {
    // GeckoLib动画缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 动画常量 - 使用RawAnimation
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation OUT = RawAnimation.begin().thenPlay("out");

    // 日志记录器
    private static final Logger LOGGER = LogUtils.getLogger();

    // 是否正在攻击的标志
    private boolean isAttacking = false;
    private int attackLockTicks = 0;
    private boolean isDead = false;

    // 调试标志
    private static final boolean DEBUG_UFO = false;
    
    // 音效相关计时器
    private int soundCooldown = 0;
    // 音效音量控制
    private static final float BASE_VOLUME = 0.5F;
    private static final float MAX_VOLUME = 0.8F;

    /**
     * 标准构造函数
     */
    public UfoPangenas(EntityType<? extends UfoPangenas> type, Level level) {
        super(type, level);
        this.setHealth(this.getMaxHealth());
    }

    /**
     * 初始化AI目标
     */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        // 添加攻击目标
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));

        // 添加主动追踪玩家的目标 - 使用自定义的飞行移动逻辑
        this.goalSelector.addGoal(2, new FollowPlayerGoal(this, 1.2D, 10.0F, 32.0F));

        // 添加随机行走目标
        this.goalSelector.addGoal(4, new RandomWalkGoal(this, 1.0D, 15));

        // 添加观察目标
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // 添加被攻击时的反击目标选择器
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));

        // 添加攻击玩家目标
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * 每个tick更新
     */
    @Override
    public void tick() {
        super.tick();

        // 确保UFO始终在飞行状态
        this.setNoGravity(true);

        // 在所有端处理攻击动画计时器
        if (attackLockTicks > 0) {
            attackLockTicks--;
            if (attackLockTicks <= 0) {
                isAttacking = false;
            }
        }
        
        // 处理音效冷却
        if (soundCooldown > 0) {
            soundCooldown--;
        }
        
        // 客户端音效处理
        if (this.level().isClientSide()) {
            // 播放飞行/移动音效
            if (!isAttacking && this.getDeltaMovement().horizontalDistanceSqr() > 0.001D && soundCooldown <= 0) {
                // 根据移动速度调整音效冷却时间
                double speed = this.getDeltaMovement().horizontalDistance();
                int cooldown = (int)(20 - (speed * 15)); // 速度越快，冷却越短
                cooldown = Math.max(10, cooldown); // 最小冷却时间
                
                this.playUfoSound(speed);
                soundCooldown = cooldown;
            }
        }

        if (!this.level().isClientSide()) {
            // 服务器端：在攻击目标时触发攻击动画
            LivingEntity target = this.getTarget();
            if (target != null && this.isWithinMeleeAttackRange(target)) {
                // 确保攻击动画不会过于频繁触发
                if (!isAttacking || attackLockTicks <= 20) {
                    this.triggerAttack();
                }
            }
        }
    }

    /**
     * 检查是否在近战攻击范围内
     */
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        double distance = this.distanceToSqr(target);
        return distance <= this.getMeleeAttackRangeSqr(target);
    }

    /**
     * 获取近战攻击范围平方
     */
    private double getMeleeAttackRangeSqr(LivingEntity target) {
        double attackRange = this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + target.getBbWidth();
        return attackRange * attackRange;
    }

    /**
     * 实际执行攻击
     */
    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {

        if (!(entity instanceof LivingEntity)) {
            return false;
        }

        // 触发攻击动画
        this.triggerAttack();

        return super.doHurtTarget(serverLevel, entity);
    }

    /**
     * 处理死亡
     */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        isDead = true;
        dbg("Boss has been defeated!");
        
        // 播放死亡音效
        if (this.level().isClientSide()) {
            // 死亡音效使用最大音量和较低音调
            this.playSound(SoundRegistry.UFO_P.get(), 1.0F, 0.7F + (this.random.nextFloat() * 0.1F));
        }
        
        // 可以在这里添加死亡掉落或特殊事件的逻辑
    }

    /**
     * 注册动画控制器
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 主要动画控制器 - 处理空闲、飞行、攻击和死亡动画
        controllers.add(new AnimationController<>("main", 5, state -> {
            // 结束动画优先级最高
            if (this.isDead || this.getHealth() <= 0) {
                state.setAnimation(OUT);
                dbg("Playing out animation");
                return PlayState.CONTINUE;
            }

            // 优先处理攻击动画
            if (this.isAttacking || this.swinging) {
                state.setAnimation(ATTACK);
                dbg("Playing attack animation");
                return PlayState.CONTINUE;
            }

            // 检查实体是否正在移动
            final boolean pathing = !this.getNavigation().isDone() && this.getNavigation().getPath() != null;
            final double horizVel2 = this.getDeltaMovement().horizontalDistanceSqr();
            final boolean moving = pathing || horizVel2 > 0.0001D;

            if (moving && state.isMoving()) {
                state.setAnimation(WALK);
                dbg("Playing walk animation");
            } else {
                state.setAnimation(IDLE);
                dbg("Playing idle animation");
            }

            return PlayState.CONTINUE;
        }));
    }

    /**
     * 获取GeckoLib动画缓存
     */
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /**
     * 触发攻击动画
     */
    public void triggerAttack() {
        isAttacking = true;
        attackLockTicks = 40; // 假设攻击动画持续约2秒（40 ticks）
        dbg("Attack triggered, lock for {} ticks", attackLockTicks);

        // 设置 swinging 状态以同步到客户端
        this.swinging = true;
        
        // 播放攻击音效
        if (this.level().isClientSide()) {
            this.playAttackSound();
        }
    }

    /**
     * 获取攻击状态
     */
    public boolean isAttacking() {
        return isAttacking;
    }

    /**
     * 调试日志
     */
    private void dbg(String fmt, Object... args) {
        if (!DEBUG_UFO) return;
        String sfmt = fmt.replace("{}", "%s");
        LOGGER.info("[ufo id={} attacking={} health={}] {}",
                this.getId(), this.isAttacking, this.getHealth(),
                String.format(sfmt, args));
    }

    /**
     * 保存额外数据
     */
    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putBoolean("IsAttacking", this.isAttacking);
        out.putInt("AttackLockTicks", this.attackLockTicks);
        out.putBoolean("IsDead", this.isDead);
        dbg("save: IsAttacking={}, AttackLockTicks={}, IsDead={}",
                this.isAttacking, this.attackLockTicks, this.isDead);
    }

    /**
     * 读取额外数据
     */
    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        this.isAttacking = in.getBooleanOr("IsAttacking", false);
        this.attackLockTicks = in.getIntOr("AttackLockTicks", 0);
        this.isDead = in.getBooleanOr("IsDead", false);
        dbg("load: IsAttacking={}, AttackLockTicks={}, IsDead={}",
                this.isAttacking, this.attackLockTicks, this.isDead);
    }
    
    /**
     * 播放UFO飞行音效
     */
    private void playUfoSound(double speed) {
        // 根据速度调整音量和音调
        float volume = BASE_VOLUME + (float)speed * 0.3F;
        volume = Math.min(MAX_VOLUME, volume); // 限制最大音量
        
        // 随机调整音调，避免音效重复感
        float pitch = 0.9F + (this.random.nextFloat() * 0.2F);
        
        // 添加距离衰减效果 - 这里使用标准的playSound方法，它已经内置了距离衰减
        this.playSound(SoundRegistry.UFO_P.get(), volume, pitch);
        dbg("Playing UFO sound at speed {}, volume {}, pitch {}", speed, volume, pitch);
    }
    
    /**
     * 播放攻击音效
     */
    public void playAttackSound() {
        // 攻击音效使用更高的音量和变化的音调
        float volume = 0.8F + (this.random.nextFloat() * 0.2F);
        float pitch = 1.1F + (this.random.nextFloat() * 0.3F);
        
        this.playSound(SoundRegistry.UFO_P.get(), volume, pitch);
        dbg("Playing attack sound with volume {}, pitch {}", volume, pitch);
    }

    /**
     * 创建属性
     */
    public static AttributeSupplier createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)       // Boss级生命值
                .add(Attributes.MOVEMENT_SPEED, 0.3D)     // 较快的移动速度
                .add(Attributes.ATTACK_DAMAGE, 8.0D)      // 高攻击力
                .add(Attributes.FOLLOW_RANGE, 32.0D)      // 大跟随范围
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D).build(); // 高击退抗性
    }

    /**
     * 自定义飞行实体追踪玩家目标
     * 解决无重力状态下只旋转不前进的问题
     */
    static class FollowPlayerGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final UfoPangenas ufo;
        private final double speedModifier;
        private final float stopDistance;
        private final float followDistance;
        private LivingEntity target;
        private int cooldown;

        public FollowPlayerGoal(UfoPangenas ufo, double speedModifier, float stopDistance, float followDistance) {
            this.ufo = ufo;
            this.speedModifier = speedModifier;
            this.stopDistance = stopDistance;
            this.followDistance = followDistance;
        }

        @Override
        public boolean canUse() {
            this.target = this.ufo.getTarget();
            // 只有在有目标且目标是玩家且距离在跟随范围内才使用此目标
            return this.target instanceof Player && this.ufo.distanceToSqr(this.target) <= (double)(this.followDistance * this.followDistance);
        }

        @Override
        public boolean canContinueToUse() {
            // 确保目标仍然有效且距离在跟随范围内，但不在停止距离内
            return this.target != null && this.target.isAlive() && this.ufo.distanceToSqr(this.target) > (double)(this.stopDistance * this.stopDistance) && this.ufo.distanceToSqr(this.target) <= (double)(this.followDistance * this.followDistance);
        }

        @Override
        public void start() {
            this.cooldown = 0;
        }

        @Override
        public void tick() {
            if (this.target == null) return;

            // 减少冷却时间
            if (this.cooldown > 0) {
                --this.cooldown;
                return;
            }

            // 计算朝向目标的方向向量
            double dx = this.target.getX() - this.ufo.getX();
            double dy = this.target.getY() - this.ufo.getY();
            double dz = this.target.getZ() - this.ufo.getZ();
            
            // 计算距离
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            
            if (distance > 1.0D) {
                // 归一化方向向量并应用速度
                double speed = this.speedModifier * this.ufo.getAttributeValue(Attributes.MOVEMENT_SPEED);
                dx = dx / distance * speed;
                dy = dy / distance * speed;
                dz = dz / distance * speed;
                
                // 设置实体的移动方向，确保在无重力状态下能够实际前进
                this.ufo.setDeltaMovement(dx, dy, dz);
                
                // 确保实体朝向目标
                this.ufo.lookAt(this.target, 30.0F, 30.0F);
            }
            
            // 设置冷却时间，避免过于频繁更新
            this.cooldown = 10;
        }
    }

    /**
     * 随机行走目标
     */
    static class RandomWalkGoal extends RandomStrollGoal {
        private final UfoPangenas ufo;

        public RandomWalkGoal(UfoPangenas ufo, double speedModifier, int interval) {
            super(ufo, speedModifier, interval);
            this.ufo = ufo;
        }

        @Override
        public boolean canUse() {
            // 确保实体有AI且不在攻击状态
            return super.canUse() && !this.ufo.isAttacking() && this.ufo.getTarget() == null;
        }
    }
}