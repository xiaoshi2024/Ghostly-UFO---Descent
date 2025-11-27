package com.xiaoshi2022.ghostly_ufo_descent.entities;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.xiaoshi2022.ghostly_ufo_descent.registry.ItemRegistry;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 孢子星人实体类
 * 在极噬种子中生长的外星生物，比较神秘的外星种族
 * 玩家需要与他们中的族长互动才能获得UFO方块的钥匙
 * 注意：目前为中立生物，后期将实现自定义交易功能
 */
public class SporeStarPerson extends Animal implements GeoEntity {
    // GeckoLib动画缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 动画常量 - 使用RawAnimation
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("attack");

    // 日志记录器
    private static final Logger LOGGER = LogUtils.getLogger();

    // 是否正在攻击的标志
    private boolean isAttacking = false;
    private int attackLockTicks = 0;

    // 标记是否是族长者
    private boolean isElder = false;

    // 调试标志
    private static final boolean DEBUG_SPORE = false;

    /**
     * 标准构造函数
     */
    public SporeStarPerson(EntityType<? extends SporeStarPerson> type, Level level) {
        super(type, level);
        this.isElder = random.nextBoolean(); // 随机生成是否是族长者
    }

    /**
     * 初始化AI目标
     */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        // 添加攻击目标 - 当被攻击时反击玩家
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
        
        // 添加被攻击时的反击目标选择器
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        
        // 添加随机行走和观察目标
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    /**
     * 处理与玩家的交互
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        final boolean client = level().isClientSide();

        // 如果是族长者，实现右键对话功能
        if (isElder) {
            if (!client) {
                // 显示对话信息给玩家
                player.displayClientMessage(Component.translatable("entity.ghostly_ufo_descent.spore_star_person.elder.greeting"), false);
                
                // 如果玩家持有下界之星，显示特殊对话
                if (itemstack.getItem() == Items.NETHER_STAR) {
                    player.displayClientMessage(Component.translatable("entity.ghostly_ufo_descent.spore_star_person.elder.nether_star"), false);
                    LOGGER.info("族长者与玩家 {} 互动，玩家持有下界之星", player.getName().getString());
                } else {
                    player.displayClientMessage(Component.translatable("entity.ghostly_ufo_descent.spore_star_person.elder.no_nether_star"), false);
                    LOGGER.info("族长者与玩家 {} 互动", player.getName().getString());
                }
            }
            return client ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }

        // 普通孢子星人互动
        if (!client) {
            player.displayClientMessage(Component.translatable("entity.ghostly_ufo_descent.spore_star_person.normal.sound"), false);
            LOGGER.info("孢子星人与玩家 {} 互动", player.getName().getString());
        }

        return super.mobInteract(player, hand);
    }

    // 注意：移除了直接重写hurt方法的代码，因为该方法在新版本中可能是final的
    // 我们依赖于AI目标系统和目标选择器来处理被动攻击行为

    /**
     * 每个tick更新
     */
    @Override
    public void tick() {
        super.tick();

        // 在所有端处理攻击动画计时器
        if (attackLockTicks > 0) {
            attackLockTicks--;
            if (attackLockTicks <= 0) {
                isAttacking = false;
            }
        }

        if (!this.level().isClientSide()) {
            // 服务器端：在攻击目标时触发攻击动画
            if (this.getTarget() != null && this.isWithinMeleeAttackRange(this.getTarget())) {
                // 确保攻击动画不会过于频繁触发
                if (!isAttacking || attackLockTicks <= 20) {
                    this.triggerAttack();
                }
            }
        }
    }

    /**
     * 获取繁殖后代
     * 孢子星人不需要繁殖功能，但Animal类要求实现此方法
     */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    /**
     * 检查是否可以被繁殖
     * 孢子星人不支持繁殖
     */
    @Override
    public boolean canBreed() {
        return false;
    }

    /**
     * 检查是否是食物
     */
    @Override
    public boolean isFood(ItemStack stack) {
        return false; // 孢子星人不吃普通食物
    }

    /**
     * 注册动画控制器
     */
    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        // 主要动画控制器 - 处理空闲、行走和攻击动画
        controllers.add(new AnimationController<>( "main", 5, state -> {
            // 优先处理攻击动画
            if (this.swinging) {
                state.setAndContinue(ATTACK);
                dbg("Playing attack animation");
                return PlayState.CONTINUE;
            }

            // 检查实体是否正在移动
            final boolean pathing = !this.getNavigation().isDone() && this.getNavigation().getPath() != null;
            final double horizVel2 = this.getDeltaMovement().horizontalDistanceSqr();
            final boolean moving = pathing || horizVel2 > 0.0001D;

            if (moving && state.isMoving()) {
                state.setAndContinue(WALK);
                dbg("Playing walk animation");
            } else {
                state.setAndContinue(IDLE);
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
     * 判断是否是族长者
     */
    public boolean isElder() {
        return isElder;
    }
    
    /**
     * 设置是否是族长者
     */
    public void setElder(boolean isElder) {
        this.isElder = isElder;
    }

    /**
     * 触发攻击动画
     */
    public void triggerAttack() {
        isAttacking = true;
        attackLockTicks = 40; // 假设攻击动画持续约2秒（40 ticks）
        dbg("Attack triggered, lock for {} ticks", attackLockTicks);
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
        if (!DEBUG_SPORE) return;
        String sfmt = fmt.replace("{}", "%s");
        LOGGER.info("[spore id={} elder={} attacking={}] {}",
                this.getId(), this.isElder(), this.isAttacking,
                String.format(sfmt, args));
    }

    /**
     * 保存额外数据
     */
    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        out.putBoolean("IsElder", this.isElder);
        out.putBoolean("IsAttacking", this.isAttacking);
        out.putInt("AttackLockTicks", this.attackLockTicks);
        dbg("save: IsElder={}, IsAttacking={}, AttackLockTicks={}", this.isElder, this.isAttacking, this.attackLockTicks);
    }

    /**
     * 读取额外数据
     */
    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        this.isElder = in.getBooleanOr("IsElder", false);
        this.isAttacking = in.getBooleanOr("IsAttacking", false);
        this.attackLockTicks = in.getIntOr("AttackLockTicks", 0);
        dbg("load: IsElder={}, IsAttacking={}, AttackLockTicks={}", this.isElder, this.isAttacking, this.attackLockTicks);
    }

    /**
     * 创建属性
     */
    public static AttributeSupplier createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)       // 中等生命值
                .add(Attributes.MOVEMENT_SPEED, 0.25D)   // 标准移动速度
                .add(Attributes.ATTACK_DAMAGE, 4.0D)     // 攻击伤害
                .add(Attributes.FOLLOW_RANGE, 16.0D).build();    // 跟随范围
    }

    // 掉落物通过JSON战利品表定义
}