package com.xiaoshi2022.ghostly_ufo_descent.client.model.entity;

import com.xiaoshi2022.ghostly_ufo_descent.entities.SporeStarPerson;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.MODID;

public class SporeStarPersonModel  extends DefaultedEntityGeoModel<SporeStarPerson> {
    public SporeStarPersonModel() {
        super(Identifier.fromNamespaceAndPath(MODID, "spore_star_person"));
    }

}
