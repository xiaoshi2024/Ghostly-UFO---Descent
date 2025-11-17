package com.xiaoshi2022.ghostly_ufo_descent.client.model.block;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import com.xiaoshi2022.ghostly_ufo_descent.block.entity.GhostlySarcophagus;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import javax.annotation.Nullable;

public class GhostlySarcophagusModel extends DefaultedBlockGeoModel<GhostlySarcophagus> {
    public GhostlySarcophagusModel() {
        super(ResourceLocation.fromNamespaceAndPath(GhostlyUFODescent.MODID, "ghostly_sarcophagus"));
    }

    // We add the `examplemod_day_time` DataTicket so it can be used later
    @Override
    public void addAdditionalStateData(GhostlySarcophagus animatable, GeoRenderState renderState) {
        renderState.addGeckolibData(GhostlySarcophagus.DAY_TIME, animatable.getLevel().dayTime());
    }

    @Nullable
    @Override
    public RenderType getRenderType(GeoRenderState renderState, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}