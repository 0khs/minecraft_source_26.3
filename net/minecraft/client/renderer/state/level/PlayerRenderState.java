/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.state.level;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class PlayerRenderState {
    public boolean hasPlayer;
    public @Nullable AvatarRenderState avatarRenderState;
    public final FirstPersonHandsAndItemsRenderState firstPersonHandsAndItems = new FirstPersonHandsAndItemsRenderState();
    public float portalEffectIntensity;
    public float nauseaEffectIntensity;
    public float spinningEffectAngle;
    public boolean isEyeInWater;
    public boolean isOnFire;
    public boolean isUnderWater;
    public double eyePositionY;
    public @Nullable BlockOverlay blockOverlay;
    public @Nullable WaterOverlay waterOverlay;
    public @Nullable ItemActivationRenderState itemActivation;

    public void reset() {
        this.hasPlayer = false;
        this.avatarRenderState = null;
        this.blockOverlay = null;
        this.waterOverlay = null;
        this.itemActivation = null;
    }

    public record BlockOverlay(Identifier atlasLocation, float u0, float v0, float u1, float v1) {
    }

    public record WaterOverlay(int color, float uOffset, float vOffset) {
    }

    public static class ItemActivationRenderState {
        public final ItemStack item;
        public final int ticks;
        public final float offX;
        public final float offY;
        public final ItemStackRenderState itemState = new ItemStackRenderState();

        public ItemActivationRenderState(ItemStack item, int ticks, float offX, float offY) {
            this.item = item;
            this.ticks = ticks;
            this.offX = offX;
            this.offY = offY;
        }
    }
}

