/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.player;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class FirstPersonHandsAndItems {
    private ItemStack mainHandItem = ItemStack.EMPTY;
    private ItemStack offHandItem = ItemStack.EMPTY;
    private float mainHandHeight;
    private float oMainHandHeight;
    private float offHandHeight;
    private float oOffHandHeight;

    public void tick(LocalPlayer player) {
        this.oMainHandHeight = this.mainHandHeight;
        this.oOffHandHeight = this.offHandHeight;
        ItemStack nextMainHand = player.getMainHandItem();
        ItemStack nextOffHand = player.getOffhandItem();
        if (this.shouldInstantlyReplaceVisibleItem(this.mainHandItem, nextMainHand, player)) {
            this.mainHandItem = nextMainHand;
        }
        if (this.shouldInstantlyReplaceVisibleItem(this.offHandItem, nextOffHand, player)) {
            this.offHandItem = nextOffHand;
        }
        if (player.isHandsBusy()) {
            this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4f, 0.0f, 1.0f);
            this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4f, 0.0f, 1.0f);
        } else {
            float attackAnim = player.getItemSwapScale(1.0f);
            float mainHandTargetHeight = this.mainHandItem != nextMainHand ? 0.0f : attackAnim * attackAnim * attackAnim;
            float offHandTargetHeight = this.offHandItem != nextOffHand ? 0.0f : 1.0f;
            this.mainHandHeight += Mth.clamp(mainHandTargetHeight - this.mainHandHeight, -0.4f, 0.4f);
            this.offHandHeight += Mth.clamp(offHandTargetHeight - this.offHandHeight, -0.4f, 0.4f);
        }
        if (this.mainHandHeight < 0.1f) {
            this.mainHandItem = nextMainHand;
        }
        if (this.offHandHeight < 0.1f) {
            this.offHandItem = nextOffHand;
        }
    }

    public void itemUsed(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.mainHandHeight = 0.0f;
        } else {
            this.offHandHeight = 0.0f;
        }
    }

    public void extractRenderState(LocalPlayer player, float partialTicks, FirstPersonHandsAndItemsRenderState state) {
        LivingEntity.SwingDescription currentSwing = player.getCurrentSwing();
        state.attackHand = currentSwing == null ? InteractionHand.MAIN_HAND : currentSwing.hand();
        state.viewXRot = player.getViewXRot(partialTicks);
        state.viewYRot = player.getViewYRot(partialTicks);
        state.xBob = Mth.lerp(partialTicks, player.xBobO, player.xBob);
        state.yBob = Mth.lerp(partialTicks, player.yBobO, player.yBob);
        state.isScoping = player.isScoping();
        state.useItemRemainingTicks = player.getUseItemRemainingTicks();
        state.handRenderSelection = FirstPersonHandsAndItems.evaluateWhichHandsToRender(player);
        state.mainHandItem = this.mainHandItem;
        state.offHandItem = this.offHandItem;
        state.mainHandHeight = this.mainHandHeight;
        state.oldMainHandHeight = this.oMainHandHeight;
        state.offHandHeight = this.offHandHeight;
        state.oldOffHandHeight = this.oOffHandHeight;
        boolean isMainHandRight = player.getMainArm() == HumanoidArm.RIGHT;
        ItemDisplayContext mainHandDisplayContext = isMainHandRight ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        ItemDisplayContext offHandDisplayContext = isMainHandRight ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        state.mainHandRenderState.clear();
        state.offHandRenderState.clear();
        player.minecraft.getItemModelResolver().updateForTopItem(state.mainHandRenderState, state.mainHandItem, mainHandDisplayContext, player.level(), player, player.getId() + mainHandDisplayContext.ordinal());
        player.minecraft.getItemModelResolver().updateForTopItem(state.offHandRenderState, state.offHandItem, offHandDisplayContext, player.level(), player, player.getId() + offHandDisplayContext.ordinal());
        state.mainHandUseDuration = state.mainHandItem.getUseDuration(player);
        state.offHandUseDuration = state.offHandItem.getUseDuration(player);
        state.mainHandChargeDuration = CrossbowItem.getChargeDuration(state.mainHandItem, player);
        state.offHandChargeDuration = CrossbowItem.getChargeDuration(state.offHandItem, player);
        state.mainHandSwapScale = player.minecraft.getItemModelResolver().swapAnimationScale(state.mainHandItem);
        state.offHandSwapScale = player.minecraft.getItemModelResolver().swapAnimationScale(state.offHandItem);
        state.hasMainHandMapData = this.extractMapRenderState(player, state.mainHandItem, state.mainHandMapRenderState);
        state.hasOffHandMapData = this.extractMapRenderState(player, state.offHandItem, state.offHandMapRenderState);
    }

    private boolean shouldInstantlyReplaceVisibleItem(ItemStack currentlyVisibleItem, ItemStack expectedItem, LocalPlayer player) {
        if (ItemStack.matchesIgnoringComponents(currentlyVisibleItem, expectedItem, DataComponentType::ignoreSwapAnimation)) {
            return true;
        }
        return !player.minecraft.getItemModelResolver().shouldPlaySwapAnimation(expectedItem);
    }

    private boolean extractMapRenderState(LocalPlayer player, ItemStack itemStack, MapRenderState state) {
        MapItemSavedData mapData;
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        MapItemSavedData mapItemSavedData = mapData = mapId == null ? null : MapItem.getSavedData(mapId, player.level());
        if (mapId == null || mapData == null) {
            return false;
        }
        player.minecraft.getMapRenderer().extractRenderState(mapId, mapData, state);
        return true;
    }

    public static FirstPersonHandsAndItemsRenderState.HandRenderSelection evaluateWhichHandsToRender(LocalPlayer player) {
        boolean holdsCrossbow;
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offhandItem = player.getOffhandItem();
        boolean holdsBow = mainHandItem.is(Items.BOW) || offhandItem.is(Items.BOW);
        boolean bl = holdsCrossbow = mainHandItem.is(Items.CROSSBOW) || offhandItem.is(Items.CROSSBOW);
        if (!holdsBow && !holdsCrossbow) {
            return FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_BOTH_HANDS;
        }
        if (player.isUsingItem()) {
            ItemStack usedItemStack = player.getUseItem();
            InteractionHand usedHand = player.getUsedItemHand();
            if (usedItemStack.is(Items.BOW) || usedItemStack.is(Items.CROSSBOW)) {
                return FirstPersonHandsAndItemsRenderState.HandRenderSelection.onlyForHand(usedHand);
            }
            return usedHand == InteractionHand.MAIN_HAND && FirstPersonHandsAndItems.isChargedCrossbow(offhandItem) ? FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_MAIN_HAND_ONLY : FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_BOTH_HANDS;
        }
        if (FirstPersonHandsAndItems.isChargedCrossbow(mainHandItem)) {
            return FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_MAIN_HAND_ONLY;
        }
        return FirstPersonHandsAndItemsRenderState.HandRenderSelection.RENDER_BOTH_HANDS;
    }

    private static boolean isChargedCrossbow(ItemStack item) {
        return item.is(Items.CROSSBOW) && CrossbowItem.isCharged(item);
    }
}

