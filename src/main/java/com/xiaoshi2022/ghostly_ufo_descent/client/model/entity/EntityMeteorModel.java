package com.xiaoshi2022.ghostly_ufo_descent.client.model.entity;

import com.xiaoshi2022.ghostly_ufo_descent.meteor.entity.EntityMeteor;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.MODID;

public class EntityMeteorModel extends DefaultedEntityGeoModel<EntityMeteor> {

    public EntityMeteorModel() {
        super(Identifier.fromNamespaceAndPath(MODID, "meteor"));
    }

}