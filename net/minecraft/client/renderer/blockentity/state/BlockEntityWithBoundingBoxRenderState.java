/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BoundingBoxRenderable;
import org.jspecify.annotations.Nullable;

public class BlockEntityWithBoundingBoxRenderState
extends BlockEntityRenderState {
    public boolean isVisible;
    public BoundingBoxRenderable.Mode mode;
    public BoundingBoxRenderable.RenderableBox box;
    public @Nullable InvisibleBlockType @Nullable [] invisibleBlocks;
    public boolean @Nullable [] structureVoids;

    public static enum InvisibleBlockType {
        AIR,
        BARRIER,
        LIGHT,
        STRUCTURE_VOID;

    }
}

