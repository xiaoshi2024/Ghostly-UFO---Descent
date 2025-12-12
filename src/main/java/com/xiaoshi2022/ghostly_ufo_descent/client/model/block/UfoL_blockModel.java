package com.xiaoshi2022.ghostly_ufo_descent.client.model.block;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import com.xiaoshi2022.ghostly_ufo_descent.block.entity.UfoL_blockentity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class UfoL_blockModel  extends DefaultedBlockGeoModel<UfoL_blockentity> {
    public UfoL_blockModel() {
        super(Identifier.fromNamespaceAndPath(GhostlyUFODescent.MODID, "ufo_l"));
    }

    // We add the `examplemod_day_time` DataTicket so it can be used later
    @Override
    public void addAdditionalStateData(UfoL_blockentity animatable,Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(UfoL_blockentity.DAY_TIME, animatable.getLevel().getDayTime());
    }

}
