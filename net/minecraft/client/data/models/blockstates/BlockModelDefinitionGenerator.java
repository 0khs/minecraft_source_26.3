/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data.models.blockstates;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.world.level.block.Block;

public interface BlockModelDefinitionGenerator {
    public Block block();

    public BlockStateModelDispatcher create();
}

