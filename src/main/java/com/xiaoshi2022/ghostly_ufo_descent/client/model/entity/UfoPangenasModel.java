package com.xiaoshi2022.ghostly_ufo_descent.client.model.entity;

import com.xiaoshi2022.ghostly_ufo_descent.entities.UfoPangenas;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.MODID;

/**
 * 极噬者UFO的模型类
 * 负责加载和管理UFO的3D模型和动画
 */
public class UfoPangenasModel extends DefaultedEntityGeoModel<UfoPangenas> {

    public UfoPangenasModel() {
        super(Identifier.fromNamespaceAndPath(MODID, "ufo_pangenas"));
    }

}