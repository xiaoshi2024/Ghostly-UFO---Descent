package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.item;

import com.xiaoshi2022.ghostly_ufo_descent.item.PeachWoodSword;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.MODID;

public class PeachWoodSwordRenerer  extends GeoItemRenderer<PeachWoodSword> {
    public PeachWoodSwordRenerer() {
        super(new DefaultedItemGeoModel<>(Identifier.fromNamespaceAndPath(MODID, "peach_wood_sword")));
    }
}

