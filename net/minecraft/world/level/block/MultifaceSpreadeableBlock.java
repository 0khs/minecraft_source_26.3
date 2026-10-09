/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class MultifaceSpreadeableBlock
extends MultifaceBlock {
    public MultifaceSpreadeableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public abstract MultifaceSpreader getSpreader();
}

