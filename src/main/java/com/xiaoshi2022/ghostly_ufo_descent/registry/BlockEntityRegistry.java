package com.xiaoshi2022.ghostly_ufo_descent.registry;

import com.xiaoshi2022.ghostly_ufo_descent.block.entity.GhostlySarcophagus;
import com.xiaoshi2022.ghostly_ufo_descent.block.entity.UfoL_blockentity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.MODID;

public final class BlockEntityRegistry {

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
			DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
	// 注册方块实体类型
	public static final Supplier<BlockEntityType<GhostlySarcophagus>> GHOSTLY_SARCOPHAGUS_BLOCK_ENTITY = BLOCK_ENTITIES.register("ghostly_sarcophagus",
			() -> new BlockEntityType<>(GhostlySarcophagus::new, Set.of(BlockRegistry.GHOSTLY_SARCOPHAGUS_BLOCK.get())));

	public static final Supplier<BlockEntityType<UfoL_blockentity>> UFO_L_BLOCK_ENTITY = BLOCK_ENTITIES.register("ufo_l",
			() -> new BlockEntityType<>(UfoL_blockentity::new, Set.of(BlockRegistry.UFO_L_BLOCK.get())));


	public static void init() {}
}
