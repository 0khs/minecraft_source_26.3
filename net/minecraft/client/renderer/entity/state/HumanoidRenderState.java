/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class HumanoidRenderState
extends ArmedEntityRenderState {
    public float swimAmount;
    public float speedValue = 1.0f;
    public float maxCrossbowChargeDuration;
    public float ticksUsingItem;
    public InteractionHand useItemHand = InteractionHand.MAIN_HAND;
    public boolean isCrouching;
    public boolean isFallFlying;
    public boolean isVisuallySwimming;
    public boolean isPassenger;
    public boolean isUsingItem;
    public float elytraRotX;
    public float elytraRotY;
    public float elytraRotZ;
    public ItemStack headEquipment = ItemStack.EMPTY;
    public ItemStack chestEquipment = ItemStack.EMPTY;
    public ItemStack legsEquipment = ItemStack.EMPTY;
    public ItemStack feetEquipment = ItemStack.EMPTY;

    @Override
    public float ticksUsingItem(HumanoidArm arm) {
        if (this.isUsingItem && this.useItemHand.asArm(this.mainArm) == arm) {
            return this.ticksUsingItem;
        }
        return 0.0f;
    }
}

