package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.block;

import com.xiaoshi2022.ghostly_ufo_descent.block.entity.UfoL_blockentity;
import com.xiaoshi2022.ghostly_ufo_descent.client.model.block.UfoL_blockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class UfoL_blockentityRenderer <R extends BlockEntityRenderState & GeoRenderState> extends GeoBlockRenderer<UfoL_blockentity, R> {
    public UfoL_blockentityRenderer(BlockEntityRendererProvider.Context context) {
        super(new UfoL_blockModel());
    }
}
