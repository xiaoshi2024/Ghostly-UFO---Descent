package com.xiaoshi2022.ghostly_ufo_descent.registry;

import com.xiaoshi2022.ghostly_ufo_descent.item.GhostlyScroll;
import com.xiaoshi2022.ghostly_ufo_descent.item.GhostlyUfoDescentItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.MODID;
import static com.xiaoshi2022.ghostly_ufo_descent.registry.BlockRegistry.GHOSTLY_SARCOPHAGUS_BLOCK;
import static com.xiaoshi2022.ghostly_ufo_descent.registry.BlockRegistry.UFO_L_BLOCK;

public final class ItemRegistry {

	// Create a Deferred Register to hold Items which will all be registered under the "ghostly_ufo_descent" namespace
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

	public static final Supplier<GhostlyScroll> GHOSTLY_SCROLL = ITEMS.registerItem("ghostly_scroll", GhostlyScroll::new);

	// Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
	public static final DeferredItem<BlockItem> GHOSTLY_SARCOPHAGUS_ITEM = ITEMS.registerSimpleBlockItem("ghostly_sarcophagus", GHOSTLY_SARCOPHAGUS_BLOCK);

	public static final DeferredItem<BlockItem> UFO_L_ITEM = ITEMS.registerSimpleBlockItem("ufo_l", UFO_L_BLOCK);


	public static void init() {
	}

}
