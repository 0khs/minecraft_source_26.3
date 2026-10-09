/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public abstract class DirectionalBlock
extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    protected DirectionalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

