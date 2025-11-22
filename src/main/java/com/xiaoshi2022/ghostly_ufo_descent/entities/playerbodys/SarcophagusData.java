package com.xiaoshi2022.ghostly_ufo_descent.entities.playerbodys;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * 管理玩家在石棺休眠时的数据类
 * 替代原本的Death类，处理玩家休眠时的物品、装备和位置信息
 */
public class SarcophagusData {
    private UUID playerUUID;
    private String playerName;
    private Vec3 sleepPosition;
    private BlockPos sarcophagusPosition;
    private byte playerModel;
    private Map<EquipmentSlot, ItemStack> equipment;
    private List<ItemStack> mainInventory;
    private List<ItemStack> additionalItems;
    private long sleepStartTime;
    private boolean hasExtraEffects;

    /**
     * 私有构造函数，使用Builder模式创建实例
     */
    private SarcophagusData(Builder builder) {
        this.playerUUID = builder.playerUUID;
        this.playerName = builder.playerName;
        this.sleepPosition = builder.sleepPosition;
        this.sarcophagusPosition = builder.sarcophagusPosition;
        this.playerModel = builder.playerModel;
        this.equipment = new EnumMap<>(builder.equipment);
        this.mainInventory = new ArrayList<>(builder.mainInventory);
        this.additionalItems = new ArrayList<>(builder.additionalItems);
        this.sleepStartTime = builder.sleepStartTime;
        this.hasExtraEffects = builder.hasExtraEffects;
    }

    /**
     * 从玩家创建石棺数据
     */
    public static SarcophagusData fromPlayer(Player player) {
        Builder builder = new Builder(player.getUUID(), player.getName().getString());
        builder.sleepPosition(player.position());
        builder.playerModel((byte) 0); // 默认模型
        
        // 添加玩家装备
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.put(slot, player.getItemBySlot(slot).copy());
        }
        builder.equipment(equipment);
        
        // 添加玩家物品栏
        List<ItemStack> inventory = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i).copy();
            if (!stack.isEmpty()) {
                inventory.add(stack);
            }
        }
        builder.mainInventory(inventory);
        
        return builder.build();
    }

    /**
     * 从NBT标签读取数据
     */
    public static SarcophagusData fromNbt(CompoundTag nbt) {
        UUID playerUUID = UUID.randomUUID(); // 默认UUID
        if (nbt.contains("PlayerUUID")) {
            try {
                playerUUID = UUID.fromString(nbt.getString("PlayerUUID").orElse(UUID.randomUUID().toString()));
            } catch (IllegalArgumentException e) {
                // 如果UUID格式无效，保留默认UUID
            }
        }
        
        Builder builder = new Builder(
                playerUUID,
                nbt.getString("PlayerName").orElse("")
        );
        
        // 读取位置信息
        double x = nbt.getDouble("SleepPosX").orElse(0.0);
        double y = nbt.getDouble("SleepPosY").orElse(0.0);
        double z = nbt.getDouble("SleepPosZ").orElse(0.0);
        builder.sleepPosition(new Vec3(x, y, z));
        
        // 读取石棺位置
        if (nbt.contains("SarcophagusX")) {
            BlockPos pos = new BlockPos(
                    nbt.getInt("SarcophagusX").orElse(0),
                    nbt.getInt("SarcophagusY").orElse(0),
                    nbt.getInt("SarcophagusZ").orElse(0)
            );
            builder.sarcophagusPosition(pos);
        }
        
        // 读取玩家模型
        builder.playerModel((byte) nbt.getInt("PlayerModel").orElse(0).intValue());
        
        // 读取装备
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (nbt.contains("Equipment" + slot.getName())) {
                CompoundTag itemTag = nbt.getCompound("Equipment" + slot.getName()).orElse(new CompoundTag());
                ItemStack stack = ItemStack.EMPTY.copy();
                equipment.put(slot, stack);
            }
        }
        builder.equipment(equipment);
        
        // 读取主物品栏
        List<ItemStack> mainInventory = new ArrayList<>();
        if (nbt.contains("MainInventory")) {
            ListTag listTag = nbt.getList("MainInventory").orElse(new ListTag());
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i).orElse(new CompoundTag());
                // Skip adding items for now
            }
        }
        builder.mainInventory(mainInventory);
        
        // 读取额外物品栏
        List<ItemStack> additionalItems = new ArrayList<>();
        if (nbt.contains("AdditionalItems")) {
            ListTag listTag = nbt.getList("AdditionalItems").orElse(new ListTag());
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i).orElse(new CompoundTag());
                // Skip adding items for now
            }
        }
        builder.additionalItems(additionalItems);
        
        // 读取其他数据
        builder.sleepStartTime(nbt.getLong("SleepStartTime").orElse(0L));
        builder.hasExtraEffects(nbt.getBoolean("HasExtraEffects").orElse(false));
        
        return builder.build();
    }

    /**
     * 写入数据到NBT标签
     */
    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        
        // 写入玩家信息
        nbt.putString("PlayerUUID", playerUUID.toString());
        nbt.putString("PlayerName", playerName);
        
        // 写入位置信息
        nbt.putDouble("SleepPosX", sleepPosition.x);
        nbt.putDouble("SleepPosY", sleepPosition.y);
        nbt.putDouble("SleepPosZ", sleepPosition.z);
        
        // 写入石棺位置
        if (sarcophagusPosition != null) {
            nbt.putInt("SarcophagusX", sarcophagusPosition.getX());
            nbt.putInt("SarcophagusY", sarcophagusPosition.getY());
            nbt.putInt("SarcophagusZ", sarcophagusPosition.getZ());
        }
        
        // 写入玩家模型
        nbt.putInt("PlayerModel", playerModel);
        
        // 写入装备
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            CompoundTag itemTag = new CompoundTag();
            nbt.put("Equipment" + entry.getKey().getName(), itemTag);
        }
        
        // 写入主物品栏
        ListTag mainInventoryTag = new ListTag();
        for (ItemStack stack : mainInventory) {
            CompoundTag itemTag = new CompoundTag();
            mainInventoryTag.add(itemTag);
        }
        nbt.put("MainInventory", mainInventoryTag);
        
        // 写入额外物品栏
        ListTag additionalItemsTag = new ListTag();
        for (ItemStack stack : additionalItems) {
            CompoundTag itemTag = new CompoundTag();
            additionalItemsTag.add(itemTag);
        }
        nbt.put("AdditionalItems", additionalItemsTag);
        
        // 写入其他数据
        nbt.putLong("SleepStartTime", sleepStartTime);
        nbt.putBoolean("HasExtraEffects", hasExtraEffects);
        
        return nbt;
    }

    /**
     * 检查主物品栏是否为空
     */
    public boolean isMainInventoryEmpty() {
        for (ItemStack stack : mainInventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查额外物品栏是否为空
     */
    public boolean isAdditionalInventoryEmpty() {
        for (ItemStack stack : additionalItems) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查所有物品栏是否为空
     */
    public boolean isEmpty() {
        return isMainInventoryEmpty() && isAdditionalInventoryEmpty() && equipment.isEmpty();
    }

    /**
     * 获取所有物品
     */
    public List<ItemStack> getAllItems() {
        List<ItemStack> items = new ArrayList<>();
        items.addAll(mainInventory);
        items.addAll(additionalItems);
        items.addAll(equipment.values());
        return items;
    }

    /**
     * 将物品归还到玩家背包
     */
    public void returnItemsToPlayer(Player player) {
        // 归还物品到玩家背包
        for (ItemStack item : getAllItems()) {
            if (!item.isEmpty()) {
                if (!player.addItem(item)) {
                    // 如果背包满了，掉落物品
                    player.drop(item.copy(), false);
                }
            }
        }
    }

    // Getter and Setter methods
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Vec3 getSleepPosition() {
        return sleepPosition;
    }

    public BlockPos getSarcophagusPosition() {
        return sarcophagusPosition;
    }

    public void setSarcophagusPosition(BlockPos position) {
        this.sarcophagusPosition = position;
    }

    public byte getPlayerModel() {
        return playerModel;
    }

    public Map<EquipmentSlot, ItemStack> getEquipment() {
        return new EnumMap<>(equipment);
    }

    public List<ItemStack> getMainInventory() {
        return new ArrayList<>(mainInventory);
    }

    public List<ItemStack> getAdditionalItems() {
        return new ArrayList<>(additionalItems);
    }

    public long getSleepStartTime() {
        return sleepStartTime;
    }

    public void setSleepStartTime(long time) {
        this.sleepStartTime = time;
    }

    public boolean hasExtraEffects() {
        return hasExtraEffects;
    }

    public void setHasExtraEffects(boolean hasEffects) {
        this.hasExtraEffects = hasEffects;
    }

    /**
     * Builder模式实现
     */
    public static class Builder {
        private final UUID playerUUID;
        private final String playerName;
        private Vec3 sleepPosition = Vec3.ZERO;
        private BlockPos sarcophagusPosition = null;
        private byte playerModel = 0;
        private Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        private List<ItemStack> mainInventory = new ArrayList<>();
        private List<ItemStack> additionalItems = new ArrayList<>();
        private long sleepStartTime = System.currentTimeMillis();
        private boolean hasExtraEffects = false;

        public Builder(UUID playerUUID, String playerName) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
        }

        public Builder sleepPosition(Vec3 position) {
            this.sleepPosition = position;
            return this;
        }

        public Builder sarcophagusPosition(BlockPos position) {
            this.sarcophagusPosition = position;
            return this;
        }

        public Builder playerModel(byte model) {
            this.playerModel = model;
            return this;
        }

        public Builder equipment(Map<EquipmentSlot, ItemStack> equipment) {
            this.equipment = equipment;
            return this;
        }

        public Builder mainInventory(List<ItemStack> inventory) {
            this.mainInventory = inventory;
            return this;
        }

        public Builder additionalItems(List<ItemStack> items) {
            this.additionalItems = items;
            return this;
        }

        public Builder sleepStartTime(long time) {
            this.sleepStartTime = time;
            return this;
        }

        public Builder hasExtraEffects(boolean effects) {
            this.hasExtraEffects = effects;
            return this;
        }

        public SarcophagusData build() {
            return new SarcophagusData(this);
        }
    }
}