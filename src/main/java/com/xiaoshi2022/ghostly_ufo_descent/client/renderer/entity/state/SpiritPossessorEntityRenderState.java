package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class SpiritPossessorEntityRenderState extends ZombieRenderState {
    public UUID playerUUID = null;
    public String playerName = "Unknown";
    public byte eyeColorType = 0;
    public boolean isGlowing = true;
    public ResourceLocation skinTexture = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
}