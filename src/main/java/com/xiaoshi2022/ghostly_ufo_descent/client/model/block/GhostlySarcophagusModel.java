package com.xiaoshi2022.ghostly_ufo_descent.client.model.block;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import com.xiaoshi2022.ghostly_ufo_descent.block.entity.GhostlySarcophagus;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class GhostlySarcophagusModel extends DefaultedBlockGeoModel<GhostlySarcophagus> {
    public GhostlySarcophagusModel() {
        super(Identifier.fromNamespaceAndPath(GhostlyUFODescent.MODID, "ghostly_sarcophagus"));
    }

    // We add the `examplemod_day_time` DataTicket so it can be used later
    @Override
    public void addAdditionalStateData(GhostlySarcophagus animatable, Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(GhostlySarcophagus.DAY_TIME, animatable.getLevel().getDayTime());
    }

}