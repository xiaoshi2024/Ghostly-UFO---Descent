package com.xiaoshi2022.ghostly_ufo_descent.api.player;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;

import static com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent.INSTANCE;

public class PlayerUtils {

    /**
     * Returns the model flags for the player skin
     *
     * @param player the player
     * @return the model flags
     */
    public static byte getModel(Player player) {
        try {
            Field dataPlayerModeCustomisation = Player.class.getDeclaredField("DATA_PLAYER_MODE_CUSTOMISATION");
            dataPlayerModeCustomisation.setAccessible(true);
            return player.getEntityData().get((EntityDataAccessor<Byte>) dataPlayerModeCustomisation.get(null));
        } catch (Exception e) {
            INSTANCE.error("Error getting player model", e);
            return 0;
        }
    }

}