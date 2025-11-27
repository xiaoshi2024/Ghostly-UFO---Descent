package com.xiaoshi2022.ghostly_ufo_descent;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

//配置类，用于管理模组的所有配置项
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 示例配置项，已注释掉，因为我们不需要这些默认配置
    /*
    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);
    */
    
    // 陨石配置项
    public static final ModConfigSpec.IntValue METEOR_SPAWN_DAY = BUILDER
            .comment("The number of days before meteors can start spawning")
            .defineInRange("meteorSpawnDay", 4, 1, 30);

    public static final ModConfigSpec.DoubleValue METEOR_CHANCE = BUILDER
            .comment("The chance of a meteor spawning (0.0 to 1.0)")
            .defineInRange("meteorChance", 0.15, 0.0, 1.0);

    public static final ModConfigSpec.IntValue METEOR_CHECK_INTERVAL = BUILDER
            .comment("The interval (in ticks) between meteor spawn checks")
            .defineInRange("meteorCheckInterval", 20 * 30, 20, 20 * 60 * 60);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
