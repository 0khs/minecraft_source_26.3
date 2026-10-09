/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class StriderRenderState
extends LivingEntityRenderState {
    public ItemStack saddle = ItemStack.EMPTY;
    public boolean isSuffocating;
    public boolean isRidden;
}

