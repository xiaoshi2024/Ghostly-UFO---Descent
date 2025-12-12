package com.xiaoshi2022.ghostly_ufo_descent.client.model.block;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import com.xiaoshi2022.ghostly_ufo_descent.block.entity.PhagenaCropBlockEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;


public class PhagenaCropModel extends DefaultedBlockGeoModel<PhagenaCropBlockEntity> {
    public PhagenaCropModel() {
        super(Identifier.fromNamespaceAndPath(GhostlyUFODescent.MODID, "phagena_crop"));
    }

    // 添加作物生长阶段数据到渲染状态
    @Override
    public void addAdditionalStateData(PhagenaCropBlockEntity animatable, Object relatedObject,GeoRenderState renderState) {
        if (animatable.getLevel() != null) {
            int age = animatable.getBlockState().getValue(com.xiaoshi2022.ghostly_ufo_descent.block.custom.Phagenabotanys.AGE);
            // 添加生长阶段数据，可以在着色器中使用
            renderState.addGeckolibData(PhagenaCropBlockEntity.CROP_AGE, age);
            // 添加时间数据用于动画效果
            renderState.addGeckolibData(PhagenaCropBlockEntity.DAY_TIME, animatable.getLevel().getDayTime());
        }
    }
}