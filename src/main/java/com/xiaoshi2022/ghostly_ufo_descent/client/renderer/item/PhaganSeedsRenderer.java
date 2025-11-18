package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.item;

import com.xiaoshi2022.ghostly_ufo_descent.item.PhaganSeeds;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.MODID;

public class PhaganSeedsRenderer extends GeoItemRenderer<PhaganSeeds> {
    public PhaganSeedsRenderer() {
        super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(MODID, "phagan_seeds")));
    }
}

