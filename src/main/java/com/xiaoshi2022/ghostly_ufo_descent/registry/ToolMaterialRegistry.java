package com.xiaoshi2022.ghostly_ufo_descent.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public final class ToolMaterialRegistry {
    public static void init() {}

    // 定义桃木工具材料，属性介于木材和石头之间
    public static final ToolMaterial PEACH_WOOD_MATERIAL = new ToolMaterial(
            // 不能用此工具开采的方块标签（使用木材工具的标签）
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            // 耐久度（木材是59，石头是131）
            100,
            // 挖掘速度（木材是2.0f，石头是4.0f）
            3.0f,
            // 攻击伤害加成（木材是0.0f，石头是1.0f）
            0.5f,
            // 附魔能力（木材是15，石头是5）
            10,
            // 可用于修复的物品标签（使用木板物品标签）
            ItemTags.PLANKS
    );
}
