package com.xiaoshi2022.ghostly_ufo_descent.entities.playerbodys;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.ghostly_ufo_descent.api.codec.CachedMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public class CorpseRenderer extends EntityRenderer<CorpseEntity, CorpseRenderState> {

    // 简单的缓存实现
    private final CachedMap<UUID, DummyPlayer> players;
    private final CachedMap<UUID, DummySkeleton> skeletons;

    public CorpseRenderer(EntityRendererProvider.Context renderer) {
        super(renderer);
        players = new CachedMap<>(10_000L);
        skeletons = new CachedMap<>(10_000L);
    }

    @Override
    public CorpseRenderState createRenderState() {
        return new CorpseRenderState();
    }

    @Override
    public void submit(CorpseRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        super.submit(state, stack, collector, cameraRenderState);

        stack.pushPose();

        stack.mulPose(Axis.YP.rotationDegrees(-state.yRot));

        // 默认为false，保持遗体在地面上
        boolean spawnOnFace = false;

        if (spawnOnFace) {
            stack.mulPose(Axis.XP.rotationDegrees(90F));
            stack.translate(0D, -1D, -2.01D / 16D);
        } else {
            stack.mulPose(Axis.XP.rotationDegrees(-90F));
            stack.translate(0D, -1D, 2.01D / 16D);
        }

        if (state.skeleton) {
            entityRenderDispatcher.getRenderer(state.skeletonRenderState).submit(state.skeletonRenderState, stack, collector, cameraRenderState);
        } else {
            entityRenderDispatcher.getRenderer(state.playerRenderState).submit(state.playerRenderState, stack, collector, cameraRenderState);
        }

        // 渲染灵魂眼睛覆盖层
        if (state.hasSoulEyes && !state.skeleton) {
            renderSoulEyes(state, stack, collector, cameraRenderState);
        }

        stack.popPose();
    }
    
    /**
     * 渲染灵魂眼睛覆盖层
     */
    private void renderSoulEyes(CorpseRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        // 根据soul_eye_color选择对应的眼睛纹理
        int eyeColorIndex = state.soulEyeColor.map(value -> Math.max(0, Math.min(6, (int) value))).orElse(0); // 确保索引在0-6范围内，如果不存在则使用默认值0
        
        // 构建眼睛纹理资源路径
        Identifier eyeTexture = Identifier.fromNamespaceAndPath("ghostly_ufo_descent", "entity/soul_eyes_" + eyeColorIndex);
        
        // 设置眼睛覆盖层的变换（基于玩家模型的眼睛位置）
        stack.pushPose();
        
        // 调整位置和大小以匹配玩家模型的眼睛
        // 移动到头部位置并调整缩放
        stack.translate(0, 0.5, 0); // 移动到头部位置
        stack.scale(0.25F, 0.25F, 0.25F); // 调整大小
        
        // 在实际项目中，你需要使用正确的MultiBufferSource和VertexConsumer API来绘制四边形
        // 以下是一个简化的实现框架，需要根据实际的渲染API进行调整
        
        // 左眼位置
        stack.pushPose();
        stack.translate(-0.5, 0, 0.5); // 左眼位置
        // 这里应该使用正确的API来渲染左眼四边形
        stack.popPose();
        
        // 右眼位置
        stack.pushPose();
        stack.translate(0.5, 0, 0.5); // 右眼位置
        // 这里应该使用正确的API来渲染右眼四边形
        stack.popPose();
        
        stack.popPose();
    }

    @Override
    public void extractRenderState(CorpseEntity corpse, CorpseRenderState state, float partialTicks) {
        super.extractRenderState(corpse, state, partialTicks);

        state.yRot = corpse.getYRot();
        state.skeleton = corpse.isSkeleton();
        
        // 提取灵魂眼睛颜色数据
        if (corpse.getPersistentData().contains("soul_eye_color")) {
            state.soulEyeColor = corpse.getPersistentData().getByte("soul_eye_color");
            state.hasSoulEyes = true;
        } else {
            state.hasSoulEyes = false;
        }
        
        if (corpse.isSkeleton()) {
            DummySkeleton skeleton = skeletons.get(corpse.getUUID(), () -> new DummySkeleton(corpse.level(), corpse.getEquipment()));
            ((SkeletonRenderer) entityRenderDispatcher.getRenderer(state.skeletonRenderState)).extractRenderState(skeleton, state.skeletonRenderState, 0F);
            state.skeletonRenderState.lightCoords = state.lightCoords;
        } else {
            DummyPlayer dummyPlayer = players.get(corpse.getUUID(), () -> new DummyPlayer((ClientLevel) corpse.level(), new GameProfile(corpse.getPlayerUuid(), corpse.getCorpseName()), corpse.getEquipment(), corpse.getCorpseModel()));
            ((AvatarRenderer) entityRenderDispatcher.getRenderer(state.playerRenderState)).extractRenderState(dummyPlayer, state.playerRenderState, 0F);
            state.playerRenderState.lightCoords = state.lightCoords;
        }
    }

}