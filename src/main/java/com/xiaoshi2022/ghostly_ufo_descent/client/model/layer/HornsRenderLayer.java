package com.xiaoshi2022.ghostly_ufo_descent.client.model.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class HornsRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final ResourceLocation HORNS_TEXTURE = ResourceLocation.fromNamespaceAndPath("ghostly_ufo_descent", "textures/entity/horns.png");
    private final ModelPart horns;

    public HornsRenderLayer(AvatarRenderer<AbstractClientPlayer> playerRenderer, EntityModelSet entityModels) {
        super(playerRenderer);
        this.horns = createVisibleHornsModel().bakeRoot();
    }

    private static LayerDefinition createVisibleHornsModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 直接在根节点创建角模型
        PartDefinition hornsRoot = partdefinition.addOrReplaceChild("horns", CubeListBuilder.create(),
                PartPose.offset(0.0f, -8.0f, -4.0f)); // 调整到头部前方

        // 调整角的间距 - 根据需要选择合适的大小
        float hornSpacing = 2.1f; // 可以尝试不同的值

        // 左角 - 放在左边（负偏移）
        hornsRoot.addOrReplaceChild("left_horn_base", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.5f, -2.0f, -1.0f, 2.0f, 2.0f, 2.0f),
                PartPose.offset(-hornSpacing, 6.0f, 0.0f));

        hornsRoot.addOrReplaceChild("left_horn_mid", CubeListBuilder.create()
                        .texOffs(8, 0)
                        .addBox(-1.0f, -4.0f, -0.5f, 1.5f, 2.0f, 1.5f),
                PartPose.offset(-hornSpacing, 6.0f, 0.0f));

        hornsRoot.addOrReplaceChild("left_horn_tip", CubeListBuilder.create()
                        .texOffs(0, 4)
                        .addBox(-0.5f, -6.0f, 0.0f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(-hornSpacing, 6.0f, 0.0f));

        // 右角 - 放在右边（正偏移）
        hornsRoot.addOrReplaceChild("right_horn_base", CubeListBuilder.create()
                        .texOffs(0, 7)
                        .addBox(-0.5f, -2.0f, -1.0f, 2.0f, 2.0f, 2.0f),
                PartPose.offset(hornSpacing, 6.0f, 0.0f));

        hornsRoot.addOrReplaceChild("right_horn_mid", CubeListBuilder.create()
                        .texOffs(8, 4)
                        .addBox(-0.5f, -4.0f, -0.5f, 1.5f, 2.0f, 1.5f),
                PartPose.offset(hornSpacing, 6.0f, 0.0f));

        hornsRoot.addOrReplaceChild("right_horn_tip", CubeListBuilder.create()
                        .texOffs(4, 7)
                        .addBox(-0.5f, -6.0f, 0.0f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(hornSpacing, 6.0f, 0.0f));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AvatarRenderState renderState, float limbSwing, float limbSwingAmount, float partialTick) {

        if (!shouldRender(renderState)) {
            return;
        }

        poseStack.pushPose();

        // 获取玩家模型和头部
        PlayerModel model = this.getParentModel();
        ModelPart head = model.getHead();

        if (head != null) {
            // 应用头部变换
            head.translateAndRotate(poseStack);

            // 调整位置到头顶合适位置
            poseStack.translate(0.0F, -0.25F, 0.0F); // 稍微向上调整

            // 可以根据需要调整角度
            // poseStack.mulPose(Vector3f.XP.rotationDegrees(-10.0F)); // 稍微向前倾斜
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(HORNS_TEXTURE));
        horns.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    // 如果需要向后兼容，保留submit方法但使用传统渲染方式
    @Override
    public void submit(PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, int packedLight,
                       AvatarRenderState renderState, float limbSwing, float limbSwingAmount) {

        // 创建临时的MultiBufferSource来模拟传统渲染
        MultiBufferSource.BufferSource bufferSource = net.minecraft.client.Minecraft.getInstance().renderBuffers().bufferSource();

        render(poseStack, bufferSource, packedLight, renderState, limbSwing, limbSwingAmount, 0.0f);

        // 提交缓冲区
        bufferSource.endBatch();
    }

    private boolean shouldRender(AvatarRenderState renderState) {
        return true;
    }
}