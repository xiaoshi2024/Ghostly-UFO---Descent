package com.xiaoshi2022.ghostly_ufo_descent.registry;

import com.xiaoshi2022.ghostly_ufo_descent.entities.SporeStarPerson;
import com.xiaoshi2022.ghostly_ufo_descent.entities.UfoPangenas;
import com.xiaoshi2022.ghostly_ufo_descent.entities.playerbodys.CorpseEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class EntityRegistry {
	public static void init() {}

	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
			DeferredRegister.create(Registries.ENTITY_TYPE, "ghostly_ufo_descent");

	public static final Supplier<EntityType<SporeStarPerson>> SPORE_STAR_PERSON = ENTITY_TYPES.register("spore_star_person",
			() -> EntityType.Builder.of(SporeStarPerson::new, MobCategory.CREATURE)
					.sized(0.6f, 0.6f)
					.eyeHeight(1.62f)
					.clientTrackingRange(8)
					.build(ResourceKey.create(Registries.ENTITY_TYPE,
							ResourceLocation.fromNamespaceAndPath("ghostly_ufo_descent", "spore_star_person"))));

	// 注册尸体实体
	public static final Supplier<EntityType<CorpseEntity>> CORPSE_ENTITY = ENTITY_TYPES.register("corpse_entity",
			() -> EntityType.Builder.<CorpseEntity>of(CorpseEntity::new, MobCategory.MISC)
					.sized(2.0f, 0.5f)
					.eyeHeight(0.25f)
					.clientTrackingRange(8)
					.build(ResourceKey.create(Registries.ENTITY_TYPE,
							ResourceLocation.fromNamespaceAndPath("ghostly_ufo_descent", "corpse_entity"))));

	// 注册极噬者UFO实体
	public static final Supplier<EntityType<UfoPangenas>> UFO_PANGENAS = ENTITY_TYPES.register("ufo_pangenas",
			() -> EntityType.Builder.of(UfoPangenas::new, MobCategory.MONSTER)
					.sized(0.6f, 2.1f)  // 大型Boss尺寸
					.eyeHeight(2.0f)
					.clientTrackingRange(16)  // 增加跟踪范围以确保远距离可见
					.build(ResourceKey.create(Registries.ENTITY_TYPE,
							ResourceLocation.fromNamespaceAndPath("ghostly_ufo_descent", "ufo_pangenas"))));


}
