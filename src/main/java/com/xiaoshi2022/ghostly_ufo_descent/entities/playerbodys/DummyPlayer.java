package com.xiaoshi2022.ghostly_ufo_descent.entities.playerbodys;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.EnumMap;

public class DummyPlayer extends RemotePlayer {

    private final byte model;

    public DummyPlayer(ClientLevel world, GameProfile gameProfile, EnumMap<EquipmentSlot, ItemStack> equipment, byte model) {
        super(world, gameProfile);
        this.model = model;
        // 始终渲染装备，符合石棺休眠机制中保存玩家装备的需求
        for (EnumMap.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            setItemSlot(entry.getKey(), entry.getValue());
        }
        AttributeInstance attribute = getAttributes().getInstance(NeoForgeMod.NAMETAG_DISTANCE);
        if (attribute != null) {
            attribute.setBaseValue(0D);
        }

        setPos(0D, 0D, 0D);
        xo = 0D;
        yo = 0D;
        zo = 0D;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return (model & part.getMask()) == part.getMask();
    }
}