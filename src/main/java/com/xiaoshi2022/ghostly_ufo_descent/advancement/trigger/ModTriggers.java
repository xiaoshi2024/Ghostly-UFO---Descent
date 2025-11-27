package com.xiaoshi2022.ghostly_ufo_descent.advancement.trigger;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 统一的触发器管理器，集中管理所有模组的成就触发器
 */
public class ModTriggers {
    
    // 创建统一的触发器延迟寄存器
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES = 
            DeferredRegister.create(Registries.TRIGGER_TYPE, GhostlyUFODescent.MODID);
    
    // 注册所有触发器
    public static final DeferredHolder<CriterionTrigger<?>, MeteorImpactTrigger> METEOR_IMPACT_TRIGGER = 
            TRIGGER_TYPES.register("meteor_impact", MeteorImpactTrigger::new);
    
    public static final DeferredHolder<CriterionTrigger<?>, GhostlyScrollUseTrigger> GHOSTLY_SCROLL_USE_TRIGGER = 
            TRIGGER_TYPES.register("ghostly_scroll_use", GhostlyScrollUseTrigger::new);
    
    /**
     * 私有构造函数，防止实例化
     */
    private ModTriggers() {
    }
}
