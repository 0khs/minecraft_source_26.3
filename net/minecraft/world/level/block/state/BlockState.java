/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.block.state;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockState
extends BlockBehaviour.BlockStateBase {
    public static final Codec<BlockState> FULL_CODEC = BlockState.codec(BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState, Block::getStateDefinition).stable();
    private static final Codec<Either<Block, BlockState>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(BuiltInRegistries.BLOCK.byNameCodec(), FULL_CODEC);
    public static final Codec<BlockState> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(either -> (BlockState)either.map(Block::defaultBlockState, f -> f), state -> state == state.getBlock().defaultBlockState() ? Either.left((Object)state.getBlock()) : Either.right((Object)state));

    public BlockState(Block owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
        super(owner, propertyKeys, propertyValues);
    }

    @Override
    protected BlockState asState() {
        return this;
    }
}

