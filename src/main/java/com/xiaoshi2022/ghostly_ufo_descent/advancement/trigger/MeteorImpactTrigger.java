package com.xiaoshi2022.ghostly_ufo_descent.advancement.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class MeteorImpactTrigger extends SimpleCriterionTrigger<MeteorImpactTrigger.Instance> {
    
    
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
