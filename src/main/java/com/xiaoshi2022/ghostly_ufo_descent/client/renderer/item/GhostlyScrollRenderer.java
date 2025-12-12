package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.item;

import com.xiaoshi2022.ghostly_ufo_descent.item.GhostlyScroll;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.MODID;

public class GhostlyScrollRenderer extends GeoItemRenderer<GhostlyScroll> {
    public GhostlyScrollRenderer() {
        super(new DefaultedItemGeoModel<>(Identifier.fromNamespaceAndPath(MODID, "ghostly_scroll")));
    }
}
