package com.xiaoshi2022.ghostly_ufo_descent.registry;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.MODID;

public final class SoundRegistry {
	public static void init() {}

	public static final DeferredRegister<SoundEvent> SOUND_EVENT = DeferredRegister.create(Registries.SOUND_EVENT, MODID);


	public static Supplier<SoundEvent> UFO_P = SOUND_EVENT.register("ufo_p",
			() -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "ufo_p")));

}
