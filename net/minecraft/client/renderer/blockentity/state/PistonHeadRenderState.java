/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity.state;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public class PistonHeadRenderState
extends BlockEntityRenderState {
    public @Nullable MovingBlockRenderState block;
    public @Nullable MovingBlockRenderState base;
    public float xOffset;
    public float yOffset;
    public float zOffset;
}

