/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SpeleothemBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SulfurSpikeBlock
extends SpeleothemBlock {
    private static final int MAX_GROWING_LENGTH = 2;

    public SulfurSpikeBlock(BlockState blockToGrowOn, BlockBehaviour.Properties properties) {
        super(blockToGrowOn, properties);
    }

    @Override
    protected @LevelEvent.Value int getStalactiteLandingSound() {
        return 1052;
    }

    @Override
    protected int getMaxGrowthLength() {
        return 2;
    }
}

