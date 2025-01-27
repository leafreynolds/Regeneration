package me.suff.mc.regen.client.skin;


import com.google.common.base.MoreObjects;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import me.suff.mc.regen.common.regen.RegenCap;
import me.suff.mc.regen.network.NetworkDispatcher;
import me.suff.mc.regen.network.messages.SkinMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkinHandler {


    //Skin Storage
    public static final HashMap< UUID, ResourceLocation > PLAYER_SKINS = new HashMap<>();

    public static void tick(AbstractClientPlayerEntity playerEntity) {
        RegenCap.get(playerEntity).ifPresent(iRegen -> {
            boolean hasBeenModified = false;

            byte[] skin = iRegen.skin();
            UUID uuid = playerEntity.getUUID();

            boolean validSkin = iRegen.isSkinValidForUse();

            // Check if the player has a MOD skin and if the cache is present
            // if these conditions are true, we want to generate and cache the skin
            if (validSkin && !hasPlayerSkin(uuid) || iRegen.updateTicks() >= 140) {
                NativeImage skinImage = genSkinNative(skin);
                if (skinImage != null) {
                    addPlayerSkin(playerEntity.getUUID(), loadImage(skinImage));
                    hasBeenModified = true;
                }
            }

            //If the skin is invalid, we want to remove it and revert to Mojang
            if (!validSkin) {
                removePlayerSkin(playerEntity.getUUID());
                hasBeenModified = true;
            }

            //Update the skin if required.
            if (hasBeenModified || playerEntity.tickCount < 20) {
                ResourceLocation skinTexture = getSkinToUse(playerEntity);
                setPlayerSkin(playerEntity, skinTexture);
            }

            boolean isAlex = iRegen.isSkinValidForUse() ? iRegen.currentlyAlex() : getUnmodifiedSkinType(playerEntity);
            setPlayerSkinType(playerEntity, isAlex);
        });
    }

    public static boolean getUnmodifiedSkinType(AbstractClientPlayerEntity abstractClientPlayerEntity) {
        abstractClientPlayerEntity.playerInfo.registerTextures();
        return abstractClientPlayerEntity.playerInfo.getModelName().contentEquals("slim");
    }

    public static void setPlayerSkinType(AbstractClientPlayerEntity player, boolean isAlex) {
        NetworkPlayerInfo playerInfo = player.playerInfo;
        if (playerInfo == null) return;
        playerInfo.skinModel = isAlex ? "slim" : "default";
    }

    public static void sendResetMessage() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.playerInfo.registerTextures();
            boolean isAlex = player.playerInfo.getModelName().equals("slim");
            NetworkDispatcher.NETWORK_CHANNEL.sendToServer(new SkinMessage(new byte[0], isAlex));
        }
    }

    public static ResourceLocation loadImage(NativeImage nativeImage) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        return textureManager.register("player_", new DynamicTexture(nativeImage));
    }

    public static NativeImage genSkinNative(byte[] skinArray) {
        try {
            return NativeImage.read(new ByteArrayInputStream(skinArray));
        } catch (IOException e) {
            return null;
        }
    }

    //Set players skin
    public static void setPlayerSkin(AbstractClientPlayerEntity player, ResourceLocation texture) {
        if (player.getSkinTextureLocation().equals(texture)) {
            return;
        }
        NetworkPlayerInfo playerInfo = player.playerInfo;
        if (playerInfo == null) return;
        Map< MinecraftProfileTexture.Type, ResourceLocation > playerTextures = playerInfo.textureLocations;
        playerTextures.put(MinecraftProfileTexture.Type.SKIN, texture);
        if (texture == null) {
            playerInfo.pendingTextures = false;
        }
    }

    public static boolean hasPlayerSkin(UUID uuid) {
        return PLAYER_SKINS.containsKey(uuid);
    }

    public static void addPlayerSkin(UUID uuid, ResourceLocation texture) {
        PLAYER_SKINS.put(uuid, texture);
    }

    public static void removePlayerSkin(UUID uuid) {
        PLAYER_SKINS.remove(uuid);
    }

    public static ResourceLocation getSkinToUse(AbstractClientPlayerEntity playerEntity) {
        if (PLAYER_SKINS.containsKey(playerEntity.getGameProfile().getId())) {
            return PLAYER_SKINS.get(playerEntity.getGameProfile().getId());
        }
        if (playerEntity.playerInfo != null) {
            NetworkPlayerInfo info = playerEntity.playerInfo;
            info.pendingTextures = false;
            info.registerTextures();
            return MoreObjects.firstNonNull(info.textureLocations.get(MinecraftProfileTexture.Type.SKIN), DefaultPlayerSkin.getDefaultSkin(info.profile.getId()));
        }
        return playerEntity.getSkinTextureLocation();
    }

}
