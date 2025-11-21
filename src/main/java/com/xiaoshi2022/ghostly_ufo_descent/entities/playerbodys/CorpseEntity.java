package com.xiaoshi2022.ghostly_ufo_descent.entities.playerbodys;

import com.xiaoshi2022.ghostly_ufo_descent.GhostlyUFODescent;
import com.xiaoshi2022.ghostly_ufo_descent.api.codec.CodecUtils;
import com.xiaoshi2022.ghostly_ufo_descent.api.codec.ValueInputOutputUtils;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import com.xiaoshi2022.ghostly_ufo_descent.registry.EntityRegistry;

import java.util.*;
import java.util.stream.Collectors;

public class CorpseEntity extends CorpseBoundingBoxBase {

    private static final EntityDataAccessor<UUID> ID = SynchedEntityData.defineId(CorpseEntity.class, GhostlyUFODescent.UUID_SERIALIZER.get());
    private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> SKELETON = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> MODEL = SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<EnumMap<EquipmentSlot, ItemStack>> EQUIPMENT = SynchedEntityData.defineId(CorpseEntity.class, GhostlyUFODescent.EQUIPMENT_SERIALIZER.get());

    private int age;
    private int emptyAge;


    // 存储玩家物品的集合
    private List<ItemStack> mainInventory;
    private List<ItemStack> armorInventory;
    private List<ItemStack> offHandInventory;
    private List<ItemStack> additionalItems; // 用于存储额外物品

    // 玩家休眠时的位置信息
    private Vec3 sleepPosition;

    public CorpseEntity(EntityType type, Level world) {
        super(type, world);
        blocksBuilding = true;
        emptyAge = -1;
        initializeInventories();
        sleepPosition = Vec3.ZERO;
    }

    public CorpseEntity(Level world) {
        // 使用注册的实体类型
        super(EntityRegistry.CORPSE_ENTITY.get(), world);
        blocksBuilding = true;
        emptyAge = -1;
        initializeInventories();
        sleepPosition = Vec3.ZERO;
    }
    
    public CorpseEntity(Level world, UUID playerUUID, String playerName, EnumMap<EquipmentSlot, ItemStack> equipment, byte model, boolean isSkeleton) {
        super(EntityRegistry.CORPSE_ENTITY.get(), world);
        blocksBuilding = true;
        emptyAge = -1;
        initializeInventories();
        sleepPosition = Vec3.ZERO;
        setPlayerUuid(playerUUID);
        setCorpseName(playerName);
        setEquipment(equipment);
        setCorpseModel(model);
        setIsSkeleton(isSkeleton);
    }

    public CorpseEntity(Level world, UUID playerUUID, String playerName, EnumMap<EquipmentSlot, ItemStack> equipment, byte model) {
        super(EntityRegistry.CORPSE_ENTITY.get(), world);
        blocksBuilding = true;
        emptyAge = -1;
        initializeInventories();
        sleepPosition = Vec3.ZERO;
        setPlayerUuid(playerUUID);
        setCorpseName(playerName);
        setEquipment(equipment);
        setCorpseModel(model);
    }

    /**
     * 初始化物品栏和存储结构
     */
    private void initializeInventories() {
        mainInventory = new ArrayList<>();
        armorInventory = new ArrayList<>();
        offHandInventory = new ArrayList<>();
        additionalItems = new ArrayList<>(); // 用于存储额外物品
    }

    /**
     * 从玩家进入石棺灵魂出窍事件创建躯体
     */
    public static CorpseEntity createFromSarcophagusSleep(Player player) {
        // 复制玩家装备（包括所有装备槽位）
        EnumMap<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                equipment.put(slot, stack.copy());
            }
        }
        
        // 创建并初始化尸体实体
        CorpseEntity corpse = new CorpseEntity(player.level(), player.getUUID(), player.getName().getString(), equipment, (byte) 0);

        // 复制玩家主物品栏
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                // 根据物品栏类型分类存储
                if (i >= 0 && i < 9) {
                    // 快捷栏
                    corpse.mainInventory.add(stack.copy());
                } else if (i >= 9 && i < 36) {
                    // 主物品栏
                    corpse.mainInventory.add(stack.copy());
                } else if (i >= 36 && i < 40) {
                    // 盔甲栏（已经在上面的equipment中处理）
                    continue;
                } else if (i == 40) {
                    // 副手（已经在上面的equipment中处理）
                    continue;
                } else {
                    // 其他物品栏（如果有）
                    corpse.additionalItems.add(stack.copy());
                }
            }
        }

        // 设置位置和旋转（稍微调整位置，使其更自然）
        corpse.setPos(player.getX(), player.getY() + 0.1, player.getZ());
        corpse.setYRot(player.getYRot());
        corpse.setXRot(player.getXRot());
        corpse.setCorpseModel((byte) 0); // 默认模型，代表玩家
        corpse.sleepPosition = player.position();
        
        // 从player api获取模型标志，确保躯体外观与玩家一致
        if (player instanceof ServerPlayer serverPlayer) {
            // 这里可以根据需要设置更多玩家特定的外观属性
        }

        return corpse;
    }

    @Override
    public void tick() {
        super.tick();
        if (!isNoGravity()) {
            double yMotion = 0D;
            Vec3 motion = getDeltaMovement();
            if (isEyeInFluid(FluidTags.WATER) || isEyeInFluid(FluidTags.LAVA)) {
                if (motion.y < 0D) {
                    yMotion = motion.y + (motion.y < 0.03D ? 0.01D : 0D);
                } else {
                    yMotion = motion.y + (motion.y < 0.03D ? 5E-4D : 0D);
                }
            } else if (getY() > level().getMinY()) {
                yMotion = Math.max(-2D, motion.y - 0.0625D);
            }
            setDeltaMovement(getDeltaMovement().x * 0.75D, yMotion, getDeltaMovement().z * 0.75D);

            if (getY() < level().getMinY()) {
                teleportTo(getX(), level().getMinY(), getZ());
            }

            move(MoverType.SELF, getDeltaMovement());
        }

        if (level().isClientSide()) {
            return;
        }

        age++;
        // 石棺中的遗体不会变成骷髅，保持原样
        setIsSkeleton(false);

        // 石棺遗体不自动消失，只有在玩家醒来时才会移除
        boolean empty = isEmpty();
        if (empty && emptyAge < 0) {
            emptyAge = age;
        }
        // 保持为空但不移除，等待玩家交互
    }

    public boolean isMainInventoryEmpty() {
        return mainInventory.stream().allMatch(ItemStack::isEmpty)
                && armorInventory.stream().allMatch(ItemStack::isEmpty)
                && offHandInventory.stream().allMatch(ItemStack::isEmpty);
    }

    public boolean isAdditionalInventoryEmpty() {
        return additionalItems.stream().allMatch(ItemStack::isEmpty);
    }

    public boolean isEmpty() {
        return isMainInventoryEmpty() && isAdditionalInventoryEmpty();
    }

    // 添加物品到主物品栏
    public void addMainItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            mainInventory.add(stack.copy());
        }
    }

    // 添加物品到额外物品栏
    public void addAdditionalItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            additionalItems.add(stack.copy());
        }
    }

    /**
     * 获取所有物品
     */
    public List<ItemStack> getAllItems() {
        List<ItemStack> items = new ArrayList<>();
        items.addAll(mainInventory);
        items.addAll(armorInventory);
        items.addAll(offHandInventory);
        items.addAll(additionalItems);
        return items;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // 石棺中的遗体对火焰和岩浆免疫
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide() && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;

            // 只有玩家本人可以与自己的遗体交互
            if (serverPlayer.getUUID().equals(getPlayerUuid())) {
                // 无论是否处于灵魂状态，都将物品归还给玩家
                returnItemsToPlayer(serverPlayer);
                
                // 如果玩家处于灵魂状态，恢复可见性
                if (serverPlayer.getPersistentData().getBoolean("soul_state").orElse(false)) {
                    serverPlayer.setInvisible(false);
                    serverPlayer.getPersistentData().remove("soul_state");
                    serverPlayer.sendSystemMessage(Component.translatable("entity.ghostly_ufo_descent.corpse.returned_to_body"));
                } else {
                    // 非灵魂状态的反馈
                    serverPlayer.sendSystemMessage(Component.translatable("entity.ghostly_ufo_descent.corpse.retrieved_items"));
                }
                
                // 移除尸体实体
                this.remove(RemovalReason.DISCARDED);
            } else {
                // 其他玩家不能交互
                serverPlayer.sendSystemMessage(Component.translatable("entity.ghostly_ufo_descent.corpse.cannot_interact"));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getDisplayName() {
        String name = getCorpseName();
        if (name == null || name.trim().isEmpty()) {
            return Component.translatable("entity.ghostly_ufo_descent.corpse.display_name_empty");
        } else {
            return Component.literal(name).append(Component.translatable("entity.ghostly_ufo_descent.corpse.display_name_suffix"));
        }
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return isAlive();
    }

    public UUID getPlayerUuid() {
        return entityData.get(ID);
    }

    public void setPlayerUuid(UUID uuid) {
        entityData.set(ID, uuid);
    }

    public Vec3 getSleepPosition() {
        return sleepPosition;
    }

    public void setSleepPosition(Vec3 position) {
        this.sleepPosition = position;
    }

    public String getCorpseName() {
        return entityData.get(NAME);
    }

    public void setCorpseName(String name) {
        entityData.set(NAME, name);
    }

    public boolean isSkeleton() {
        return entityData.get(SKELETON);
    }

    public void setIsSkeleton(boolean skeleton) {
        entityData.set(SKELETON, skeleton);
    }

    public byte getCorpseModel() {
        return entityData.get(MODEL);
    }

    public void setCorpseModel(byte model) {
        entityData.set(MODEL, model);
    }



    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ID, Util.NIL_UUID);
        builder.define(NAME, "");
        builder.define(SKELETON, false);
        builder.define(MODEL, (byte) 0);
        // 自定义序列化器
        builder.define(EQUIPMENT, new EnumMap<>(EquipmentSlot.class));
    }

    public void setEquipment(EnumMap<EquipmentSlot, ItemStack> equipment) {
        entityData.set(EQUIPMENT, equipment);
    }


    public EnumMap<EquipmentSlot, ItemStack> getEquipment() {
        return entityData.get(EQUIPMENT);
    }


    @Override
    public void remove(RemovalReason reason) {
        // 当玩家从石棺中醒来时，将物品归还给玩家而不是掉落
        // 这里暂时保留掉落逻辑作为备选
        for (ItemStack item : getAllItems()) {
            Containers.dropItemStack(level(), getX(), getY(), getZ(), item);
        }
        super.remove(reason);

        // 生成一些效果粒子
        spawnWakeParticles();
    }

    public void spawnWakeParticles() {
        double x = getX();
        double y = getY();
        double z = getZ();
        Vec3 lookVec = getLookAngle().normalize();
        for (int i = 0; i <= 10; i++) {
            double d = ((((double) i) / 10D) - 0.5D) * 2D;
            level().addParticle(ParticleTypes.CLOUD, x + lookVec.x * d + (level().random.nextDouble() - 0.5D), y + 0.25D, z + lookVec.z * d + (level().random.nextDouble() - 0.5D), 0D, 0D, 0D);
        }
    }

    /**
     * 将物品归还给对应的玩家，包括所有装备和物品栏物品
     */
    public void returnItemsToPlayer(ServerPlayer player) {
        // 首先归还装备
        EnumMap<EquipmentSlot, ItemStack> equipment = getEquipment();
        if (equipment != null) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (equipment.containsKey(slot)) {
                    ItemStack stack = equipment.get(slot);
                    if (!stack.isEmpty()) {
                        // 如果玩家当前槽位有物品，先将其放入背包
                        ItemStack currentItem = player.getItemBySlot(slot);
                        if (!currentItem.isEmpty()) {
                            if (!player.addItem(currentItem)) {
                                // 如果背包满了，掉落物品
                                Containers.dropItemStack(level(), player.getX(), player.getY(), player.getZ(), currentItem);
                            }
                        }
                        // 设置装备到对应槽位
                        player.setItemSlot(slot, stack);
                    }
                }
            }
        }
        
        // 归还主物品栏和额外物品栏的物品
        List<ItemStack> itemsToReturn = new ArrayList<>();
        itemsToReturn.addAll(mainInventory);
        itemsToReturn.addAll(additionalItems);
        
        for (ItemStack item : itemsToReturn) {
            if (!item.isEmpty()) {
                if (!player.addItem(item)) {
                    // 如果背包满了，掉落物品
                    Containers.dropItemStack(level(), player.getX(), player.getY(), player.getZ(), item);
                }
            }
        }

        // 清除物品栏
        initializeInventories();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        // 直接从NBT读取数据到entityData
        setPlayerUuid(valueInput.read("PlayerUUID", UUIDUtil.CODEC).orElse(Util.NIL_UUID));
        setCorpseName(valueInput.getStringOr("CorpseName", ""));
        setIsSkeleton(valueInput.getBooleanOr("IsSkeleton", false));
        setCorpseModel(valueInput.getByteOr("CorpseModel", (byte) 0));
        
        // 读取装备
        Optional<CompoundTag> optionalEquipmentTag = ValueInputOutputUtils.getTag(valueInput, "Equipment");
        if (optionalEquipmentTag.isPresent()) {
            CompoundTag equipmentTag = optionalEquipmentTag.get();
            EnumMap<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                String slotName = slot.getName();
                if (equipmentTag.contains(slotName)) {
                        Optional<CompoundTag> slotTagOptional = equipmentTag.getCompound(slotName);
                        if (slotTagOptional.isPresent()) {
                            CompoundTag slotTag = slotTagOptional.get();
                            try {
                                ItemStack itemStack = CodecUtils.fromNBT(ItemStack.CODEC, slotTag).orElse(ItemStack.EMPTY);
                                if (!itemStack.isEmpty()) {
                                    equipment.put(slot, itemStack);
                                }
                            } catch (Exception e) {
                                // 忽略无法读取的物品
                            }
                        }
                    }
            }
            setEquipment(equipment);
        }
        
        age = valueInput.getIntOr("Age", 0);
        emptyAge = valueInput.getIntOr("EmptyAge", -1);
    }


    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        // 直接保存entityData中的数据到NBT
        valueOutput.store("PlayerUUID", UUIDUtil.CODEC, getPlayerUuid());
        valueOutput.putString("CorpseName", getCorpseName());
        valueOutput.putBoolean("IsSkeleton", isSkeleton());
        valueOutput.putByte("CorpseModel", getCorpseModel());
        
        // 保存装备
        CompoundTag equipmentTag = new CompoundTag();
        EnumMap<EquipmentSlot, ItemStack> equipment = getEquipment();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
            ItemStack stack = entry.getValue();
            if (!stack.isEmpty()) {
                CompoundTag slotTag = CodecUtils.toNBT(ItemStack.CODEC, stack).filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).orElseGet(CompoundTag::new);
                equipmentTag.put(entry.getKey().getName(), slotTag);
            }
        }
        ValueInputOutputUtils.setTag(valueOutput, "Equipment", equipmentTag);

        valueOutput.putInt("Age", age);
        if (emptyAge >= 0) {
            valueOutput.putInt("EmptyAge", emptyAge);
        }
    }
}