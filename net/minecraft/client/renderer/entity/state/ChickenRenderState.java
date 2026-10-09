/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import org.jspecify.annotations.Nullable;

public class ChickenRenderState
extends LivingEntityRenderState {
    public float flap;
    public float flapSpeed;
    public @Nullable ChickenVariant variant;
}

