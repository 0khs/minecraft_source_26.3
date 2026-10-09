/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class UndeadRenderState
extends HumanoidRenderState {
    @Override
    public ItemStack getUseItemStackForArm(HumanoidArm arm) {
        return this.getMainHandItemStack();
    }
}

