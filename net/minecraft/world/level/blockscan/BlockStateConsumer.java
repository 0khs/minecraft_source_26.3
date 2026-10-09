/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.blockscan;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Continuation;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BlockStateConsumer {
    public Continuation apply(BlockPos var1, BlockState var2);
}

