/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import org.jspecify.annotations.Nullable;

public class ShulkerBoxRenderState
extends BlockEntityRenderState {
    public Direction direction = Direction.NORTH;
    public @Nullable DyeColor color;
    public float progress;
}

