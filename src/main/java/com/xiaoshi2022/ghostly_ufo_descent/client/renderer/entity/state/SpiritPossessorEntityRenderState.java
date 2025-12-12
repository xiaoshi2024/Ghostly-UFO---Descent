package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public class SpiritPossessorEntityRenderState extends ZombieRenderState {
    public UUID playerUUID = null;
    public String playerName = "Unknown";
    public byte eyeColorType = 0;
    public boolean isGlowing = true;
    public Identifier skinTexture = Identifier.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
}