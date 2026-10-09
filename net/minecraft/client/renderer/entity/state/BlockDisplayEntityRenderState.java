/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;

public class BlockDisplayEntityRenderState
extends DisplayEntityRenderState {
    public final BlockModelRenderState blockModel = new BlockModelRenderState();

    @Override
    public boolean hasSubState() {
        return !this.blockModel.isEmpty();
    }
}

