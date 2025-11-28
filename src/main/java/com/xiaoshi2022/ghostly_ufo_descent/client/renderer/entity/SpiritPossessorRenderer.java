package com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity;

import com.mojang.authlib.GameProfile;
import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity.layer.SpiritPossessorEyesFeatureRenderer;
import com.xiaoshi2022.ghostly_ufo_descent.client.renderer.entity.state.SpiritPossessorEntityRenderState;
import com.xiaoshi2022.ghostly_ufo_descent.entities.SpiritPossessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpiritPossessorRenderer extends AbstractZombieRenderer<SpiritPossessor, SpiritPossessorEntityRenderState, ZombieModel<SpiritPossessorEntityRenderState>> {

    private static final ResourceLocation TEXTURE_FALLBACK = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    // 皮肤缓存
    private final Map<UUID, ResourceLocation> cachedPlayerSkinsByUUID = new HashMap<>();
    private final Map<String, ResourceLocation> cachedPlayerSkinsByName = new HashMap<>();
    private final Map<UUID, UUID> uuidMissmatches = new HashMap<>();

    // 异步获取状态
    private GameProfile receivedGameProfile = null;
    private GameProfile inProgress = null;
    private boolean gameProfileReceived = false;

    // 重试计数器
    private final int counterSteps = 40;
    private final int maxSubTries = 5;
    private final int maxTotalTries = 5;
    private final int counterMax = 2000 + (counterSteps * maxSubTries);

    private int counter = counterMax;
    private int totalTries = 0;

    public SpiritPossessorRenderer(EntityRendererProvider.Context ctx) {
        this(ctx, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_BABY, ModelLayers.ZOMBIE_ARMOR, ModelLayers.ZOMBIE_BABY_ARMOR);

        // 添加眼睛渲染层
        this.addLayer(new SpiritPossessorEyesFeatureRenderer(this));
    }

    public SpiritPossessorRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer, ModelLayerLocation legsArmorLayer, ArmorModelSet<ModelLayerLocation> equipmentModelData, ArmorModelSet<ModelLayerLocation> equipmentModelData2) {
        super(ctx, new ZombieModel<>(ctx.bakeLayer(layer)), new ZombieModel<>(ctx.bakeLayer(legsArmorLayer)),
                ArmorModelSet.bake(equipmentModelData, ctx.getModelSet(), ZombieModel::new),
                ArmorModelSet.bake(equipmentModelData2, ctx.getModelSet(), ZombieModel::new));
    }

    @Override
    public @NotNull SpiritPossessorEntityRenderState createRenderState() {
        return new SpiritPossessorEntityRenderState();
    }

    @Override
    public void extractRenderState(@NotNull SpiritPossessor entity, @NotNull SpiritPossessorEntityRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        reusedState.playerUUID = entity.getPlayerUuid();
        reusedState.playerName = entity.getPlayerName();
        reusedState.eyeColorType = entity.getEyeColorType();
        reusedState.isGlowing = entity.isGlowing();

        // 异步获取玩家皮肤纹理
        if (reusedState.playerUUID != null && reusedState.playerName != null) {
            getPlayerSkinFromUUID(reusedState.playerUUID, reusedState.playerName);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritPossessorEntityRenderState state) {
        // 优先使用玩家皮肤，如果没有则使用默认僵尸纹理
        if (state.playerUUID != null) {
            // 1. 从UUID缓存获取
            if (cachedPlayerSkinsByUUID.containsKey(state.playerUUID)) {
                state.skinTexture = cachedPlayerSkinsByUUID.get(state.playerUUID);
                return cachedPlayerSkinsByUUID.get(state.playerUUID);
            }

            // 2. UUID不匹配时从名称缓存获取
            if (uuidMissmatches.containsKey(state.playerUUID)) {
                UUID correctUUID = uuidMissmatches.get(state.playerUUID);
                if (cachedPlayerSkinsByUUID.containsKey(correctUUID)) {
                    state.skinTexture = cachedPlayerSkinsByUUID.get(correctUUID);
                    return cachedPlayerSkinsByUUID.get(correctUUID);
                }
            }

            // 3. 从名称缓存获取（不区分大小写）
            if (state.playerName != null) {
                if (cachedPlayerSkinsByName.containsKey(state.playerName)) {
                    state.skinTexture = cachedPlayerSkinsByName.get(state.playerName);
                    return cachedPlayerSkinsByName.get(state.playerName);
                }
                if (cachedPlayerSkinsByName.containsKey(state.playerName.toLowerCase())) {
                    state.skinTexture = cachedPlayerSkinsByName.get(state.playerName.toLowerCase());
                    return cachedPlayerSkinsByName.get(state.playerName.toLowerCase());
                }
            }
        }

        // 4. 使用默认皮肤
        return TEXTURE_FALLBACK;
    }

    /**
     * 从UUID和名称获取玩家皮肤
     */
    public void getPlayerSkinFromUUID(UUID playerUUID, String playerName) {
        try {
            // 检查皮肤是否已经在缓存中，如果是则直接返回
            if (cachedPlayerSkinsByUUID.containsKey(playerUUID)) {
                return;
            }
            
            // 检查UUID不匹配情况
            if (uuidMissmatches.containsKey(playerUUID)) {
                UUID correctUUID = uuidMissmatches.get(playerUUID);
                if (cachedPlayerSkinsByUUID.containsKey(correctUUID)) {
                    return;
                }
            }
            
            // 检查名称缓存
            if (playerName != null) {
                if (cachedPlayerSkinsByName.containsKey(playerName) || 
                    cachedPlayerSkinsByName.containsKey(playerName.toLowerCase())) {
                    return;
                }
            }

            if (inProgress == null) {
                inProgress = new GameProfile(playerUUID, playerName);
            }

            if (!inProgress.id().equals(playerUUID)) {
                return;
            }

            if ((counter > (counterMax - maxSubTries)) && (totalTries < maxTotalTries)) {
                if (receivedGameProfile == null) {
                    if (counter == counterMax) {
                        System.out.println("Trying to get GameProfile for " + playerName + " UUID: " + playerUUID);
                    }
                    receivedGameProfile = getGameProfile(playerName);
                }

                if ((!gameProfileReceived) && (receivedGameProfile != null)) {
                    System.out.println("Successfully received GameProfile for " + receivedGameProfile.name() + ", UUID: " + receivedGameProfile.id());
                    counter = counterMax;
                    totalTries = 0;
                    gameProfileReceived = true;
                }

                if (receivedGameProfile != null) {
                    Minecraft minecraft = Minecraft.getInstance();

                    Optional<PlayerSkin> optionalSkinTextures;
                    PlayerSkin skinTexture = null;
                    optionalSkinTextures = minecraft.getSkinManager().get(receivedGameProfile).get(100, TimeUnit.MILLISECONDS);

                    // 重试机制
                    int tries = 5;
                    while (!minecraft.getSkinManager().get(receivedGameProfile).isDone() && (tries > 0)) {
                        try {
                            optionalSkinTextures = minecraft.getSkinManager().get(receivedGameProfile).get(50, TimeUnit.MILLISECONDS);
                        } catch (TimeoutException timeoutException) {
                            tries--;
                        }
                    }

                    if (optionalSkinTextures.isPresent()) {
                        skinTexture = optionalSkinTextures.get();
                    }

                    if (skinTexture != null) {
                        ResourceLocation skinTextureLocation = skinTexture.body().texturePath();

                        // 处理UUID不匹配的情况
                        if (!receivedGameProfile.id().equals(playerUUID)) {
                            System.out.println("The spirit possessor for " + receivedGameProfile.name() + " has a different UUID, caching both!");
                            uuidMissmatches.put(playerUUID, receivedGameProfile.id());
                            cachedPlayerSkinsByName.put(receivedGameProfile.name(), skinTextureLocation);
                        }

                        // 缓存皮肤纹理
                        cachedPlayerSkinsByUUID.put(receivedGameProfile.id(), skinTextureLocation);
                        System.out.println("Successfully received Skin for " + receivedGameProfile.name() + ", UUID: " + receivedGameProfile.id());

                        // 重置状态
                        counter = counterMax;
                        totalTries = 0;
                        receivedGameProfile = null;
                        inProgress = null;
                        gameProfileReceived = false;
                        return;
                    } else {
                        System.out.println("No valid Skin was received for " + receivedGameProfile.name());
                    }
                }
            }

            // 计数器逻辑
            if (counter > 0) {
                counter--;
            } else {
                counter = counterMax;
                totalTries++;
                if (totalTries >= (maxTotalTries - 1)) {
                    // 使用默认皮肤作为回退
                    ResourceLocation defaultSkin = TEXTURE_FALLBACK;
                    cachedPlayerSkinsByUUID.put(playerUUID, defaultSkin);
                    System.out.println("Could not fetch a valid Skin for " + playerName + ", using default skin.");

                    // 重置状态
                    counter = counterMax;
                    totalTries = 0;
                    receivedGameProfile = null;
                    inProgress = null;
                    gameProfileReceived = false;
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting player skin: " + e.getMessage());
        }
    }

    /**
     * 通过玩家名称获取GameProfile
     */
    private GameProfile getGameProfile(String playerName) {
        try {
            ResolvableProfile profileComponent = ResolvableProfile.createUnresolved(playerName);

            CompletableFuture<Optional<PlayerSkinRenderCache.RenderInfo>> futureOptionalEntry =
                    Minecraft.getInstance().playerSkinRenderCache().lookup(profileComponent);
            Optional<PlayerSkinRenderCache.RenderInfo> optionalEntry = futureOptionalEntry.get(100, TimeUnit.MILLISECONDS);

            // 重试机制
            int tries = 5;
            while (!futureOptionalEntry.isDone() && (tries > 0)) {
                try {
                    futureOptionalEntry.get(50, TimeUnit.MILLISECONDS);
                } catch (TimeoutException timeoutException) {
                    tries--;
                }
            }

            return optionalEntry.map(PlayerSkinRenderCache.RenderInfo::gameProfile).orElse(null);
        } catch (Exception e) {
            System.out.println("Error getting GameProfile: " + e.getMessage());
        }
        return null;
    }

    /**
     * 清理缓存（用于内存管理）
     */
    public void clearSkinCache() {
        cachedPlayerSkinsByUUID.clear();
        cachedPlayerSkinsByName.clear();
        uuidMissmatches.clear();
    }
}