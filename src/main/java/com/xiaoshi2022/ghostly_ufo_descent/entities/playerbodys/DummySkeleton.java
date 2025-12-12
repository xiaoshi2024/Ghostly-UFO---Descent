package com.xiaoshi2022.ghostly_ufo_descent.entities.playerbodys;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.EnumMap;

public class DummySkeleton extends Skeleton {

    public DummySkeleton(Level world, EnumMap<EquipmentSlot, ItemStack> equipment) {
        super(EntityType.SKELETON, world);
        // 始终渲染装备，符合石棺休眠机制中保存玩家装备的需求
        for (EnumMap.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            setItemSlot(entry.getKey(), entry.getValue());
        }
    }

}