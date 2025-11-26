package com.xiaoshi2022.ghostly_ufo_descent.entities.playerbodys;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public class CorpseRenderState extends EntityRenderState {

    public float yRot;
    public AvatarRenderState playerRenderState = new AvatarRenderState();
    public SkeletonRenderState skeletonRenderState = new SkeletonRenderState();
    public boolean skeleton;
    public Optional<Byte> soulEyeColor; // 存储灵魂眼睛颜色（0-6）
    public boolean hasSoulEyes; // 是否显示灵魂眼睛

    public CorpseRenderState() {
        skeletonRenderState.entityType = EntityType.SKELETON;
    }

}