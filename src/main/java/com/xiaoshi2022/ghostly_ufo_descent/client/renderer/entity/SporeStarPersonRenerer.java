package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity;

import com.xiaoshi2022.ghostly_ufo_descent.client.model.entity.SporeStarPersonModel;
import com.xiaoshi2022.ghostly_ufo_descent.entities.SporeStarPerson;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class SporeStarPersonRenerer <R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<SporeStarPerson, R> {

    /**
     * @param //来自客户端引导器的上下文渲染提供者
     */

    public SporeStarPersonRenerer(EntityRendererProvider.Context context) {
        super(context, new SporeStarPersonModel());
        this.shadowRadius = 0.3f;
    }

}