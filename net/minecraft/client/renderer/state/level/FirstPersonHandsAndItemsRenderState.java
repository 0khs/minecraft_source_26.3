/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.client.renderer.state.level;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class FirstPersonHandsAndItemsRenderState {
    public InteractionHand attackHand;
    public float viewXRot;
    public float viewYRot;
    public float xBob;
    public float yBob;
    public boolean isScoping;
    public int useItemRemainingTicks;
    public int mainHandUseDuration;
    public int offHandUseDuration;
    public int mainHandChargeDuration;
    public int offHandChargeDuration;
    public float mainHandSwapScale;
    public float offHandSwapScale;
    public HandRenderSelection handRenderSelection;
    public ItemStack mainHandItem = ItemStack.EMPTY;
    public ItemStack offHandItem = ItemStack.EMPTY;
    public float mainHandHeight;
    public float oldMainHandHeight;
    public float offHandHeight;
    public float oldOffHandHeight;
    public final ItemStackRenderState mainHandRenderState = new ItemStackRenderState();
    public final ItemStackRenderState offHandRenderState = new ItemStackRenderState();
    public final MapRenderState mainHandMapRenderState = new MapRenderState();
    public final MapRenderState offHandMapRenderState = new MapRenderState();
    public boolean hasMainHandMapData;
    public boolean hasOffHandMapData;

    @VisibleForTesting
    public static enum HandRenderSelection {
        RENDER_BOTH_HANDS(true, true),
        RENDER_MAIN_HAND_ONLY(true, false),
        RENDER_OFF_HAND_ONLY(false, true);

        public final boolean renderMainHand;
        public final boolean renderOffHand;

        private HandRenderSelection(boolean renderMainHand, boolean renderOffHand) {
            this.renderMainHand = renderMainHand;
            this.renderOffHand = renderOffHand;
        }

        public static HandRenderSelection onlyForHand(InteractionHand hand) {
            return hand == InteractionHand.MAIN_HAND ? RENDER_MAIN_HAND_ONLY : RENDER_OFF_HAND_ONLY;
        }
    }
}

