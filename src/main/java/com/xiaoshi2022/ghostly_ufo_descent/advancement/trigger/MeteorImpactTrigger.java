package com.xiaoshi2022.ghostly_ufo_descent.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class MeteorImpactTrigger extends SimpleCriterionTrigger<MeteorImpactTrigger.Instance> {
    
    // 创建触发器实例
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES = 
            DeferredRegister.create(Registries.TRIGGER_TYPE, GhostlyUFODescent.MODID);
    
    public static final DeferredHolder<CriterionTrigger<?>, MeteorImpactTrigger> METEOR_IMPACT_TRIGGER = 
            TRIGGER_TYPES.register("meteor_impact", MeteorImpactTrigger::new);
    
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }
    
    // 触发方法
    public void trigger(ServerPlayer player) {
        this.trigger(player, triggerInstance -> triggerInstance.matches());
    }
    
    // 触发实例记录类
    public record Instance(Optional<ContextAwarePredicate> player) implements SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player)
        ).apply(instance, Instance::new));
        
        // 匹配条件检查
        public boolean matches() {
            return true; // 简单触发器，不需要额外条件
        }
    }
}
