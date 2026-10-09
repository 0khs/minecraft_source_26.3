/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ShulkerRenderState
extends LivingEntityRenderState {
    public Vec3 renderOffset = Vec3.ZERO;
    public @Nullable DyeColor color;
    public float peekAmount;
    public float yHeadRot;
    public float yBodyRot;
    public Direction attachFace = Direction.DOWN;
}

