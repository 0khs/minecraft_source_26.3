/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class PigRenderState
extends LivingEntityRenderState {
    public ItemStack saddle = ItemStack.EMPTY;
    public @Nullable PigVariant variant;
}

