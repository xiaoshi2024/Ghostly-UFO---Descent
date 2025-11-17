package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.block;

import com.xiaoshi2022.ghostly_ufo_descent.block.entity.GhostlySarcophagus;
import com.xiaoshi2022.ghostly_ufo_descent.client.model.block.GhostlySarcophagusModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class GhostlySarcophagusRenderer<R extends BlockEntityRenderState & GeoRenderState> extends GeoBlockRenderer<GhostlySarcophagus, R> {
    public GhostlySarcophagusRenderer(BlockEntityRendererProvider.Context context) {
        super(new GhostlySarcophagusModel());
    }
}
