package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity.state.SpiritPossessorEntityRenderState;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class SpiritPossessorEyesFeatureRenderer extends RenderLayer<SpiritPossessorEntityRenderState, ZombieModel<SpiritPossessorEntityRenderState>> {
    // 使用统一的眼睛纹理
    private static final ResourceLocation EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath(GhostlyUFODescent.MODID, "textures/entity/spirit_possessor_eyes.png");

    public SpiritPossessorEyesFeatureRenderer(RenderLayerParent<SpiritPossessorEntityRenderState, ZombieModel<SpiritPossessorEntityRenderState>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, SpiritPossessorEntityRenderState spiritPossessorEntityRenderState, float limbSwing, float limbSwingAmount) {
        if (spiritPossessorEntityRenderState.isGlowing) {
            // 根据眼睛颜色类型选择不同的颜色
            int color = getEyeColorByType(spiritPossessorEntityRenderState.eyeColorType);
            renderColoredCutoutModel(this.getParentModel(), EYES_TEXTURE, poseStack, submitNodeCollector, packedLight, spiritPossessorEntityRenderState, color, 1);
        }
    }

    // 根据类型获取眼睛颜色
    private int getEyeColorByType(byte type) {
        switch (type) {
            case 0: return 0xFF00FFFF; // 青色
            case 1: return 0xFF00FF00; // 绿色
            case 2: return 0xFFFF0000; // 红色
            case 3: return 0xFFFF00FF; // 紫色
            case 4: return 0xFFFFFF00; // 黄色
            case 5: return 0xFFFF8000; // 橙色
            case 6: return 0xFFFFFFFF; // 白色
            default: return 0xFF00FFFF; // 默认青色
        }
    }
}