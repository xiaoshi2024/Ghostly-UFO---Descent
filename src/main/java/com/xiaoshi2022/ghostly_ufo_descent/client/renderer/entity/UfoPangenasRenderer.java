package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity;

import com.xiaoshi2022.ghostly_ufo_descent.client.model.entity.UfoPangenasModel;
import com.xiaoshi2022.ghostly_ufo_descent.entities.UfoPangenas;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * 极噬者UFO的渲染器类
 * 负责将UFO实体渲染到游戏世界中
 */
public class UfoPangenasRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<UfoPangenas, R> {

    /**
     * @param //来自客户端引导器的上下文渲染提供者
     */

    public UfoPangenasRenderer(EntityRendererProvider.Context context) {
        super(context, new UfoPangenasModel());
        this.shadowRadius = 0.3f;
    }
}