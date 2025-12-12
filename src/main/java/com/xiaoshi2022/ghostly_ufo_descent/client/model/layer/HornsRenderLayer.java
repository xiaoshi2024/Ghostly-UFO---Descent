package com.xiaoshi2022.ghostly_ufo_descent.client.model.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xiaoshi2022.ghostly_ufo_descent.Config;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

import java.util.Random;

public class HornsRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    // 默认角贴图位置
    private static final Identifier DEFAULT_HORNS_TEXTURE = Identifier.fromNamespaceAndPath("ghostly_ufo_descent", "textures/entity/horns.png");
    // 不同颜色的角贴图位置
    private static final Identifier RED_HORNS_TEXTURE = Identifier.fromNamespaceAndPath("ghostly_ufo_descent", "textures/entity/horns_red.png");
    private static final Identifier BLUE_HORNS_TEXTURE = Identifier.fromNamespaceAndPath("ghostly_ufo_descent", "textures/entity/horns_blue.png");
    private static final Identifier GREEN_HORNS_TEXTURE = Identifier.fromNamespaceAndPath("ghostly_ufo_descent", "textures/entity/horns_green.png");
    private static final Identifier PURPLE_HORNS_TEXTURE = Identifier.fromNamespaceAndPath("ghostly_ufo_descent", "textures/entity/horns_purple.png");
    private static final Identifier GOLD_HORNS_TEXTURE = Identifier.fromNamespaceAndPath("ghostly_ufo_descent", "textures/entity/horns_gold.png");
    
    private final ModelPart horns;
    private final Random random = new Random();

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

        // 从配置中获取角间距
        float hornSpacing = (float) Config.HORNS_SPACING.get().doubleValue();

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

        // 根据配置获取当前选择的角贴图
        Identifier currentHornsTexture = getCurrentHornsTexture();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypes.entityCutoutNoCull(currentHornsTexture));
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
        // 检查是否启用了角渲染功能
        if (!Config.ENABLE_HORNS_RENDER.get()) {
            return false;
        }
        
        // 检查是否启用了鬼怪玩家特征（作为总开关）
        if (!Config.ENABLE_GHOSTLY_PLAYER_FEATURES.get()) {
            return false;
        }
        
        // 根据配置的概率决定是否渲染角
        return random.nextDouble() < Config.HORNS_RENDER_CHANCE.get();
    }
    
    /**
     * 根据配置获取当前选择的角贴图资源位置
     */
    private Identifier getCurrentHornsTexture() {
        String color = Config.HORNS_COLOR.get().toLowerCase();
        switch (color) {
            case "red":
                return RED_HORNS_TEXTURE;
            case "blue":
                return BLUE_HORNS_TEXTURE;
            case "green":
                return GREEN_HORNS_TEXTURE;
            case "purple":
                return PURPLE_HORNS_TEXTURE;
            case "gold":
                return GOLD_HORNS_TEXTURE;
            case "default":
            default:
                return DEFAULT_HORNS_TEXTURE;
        }
    }
}