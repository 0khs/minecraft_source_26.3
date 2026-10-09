/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record RotatedBlockProvider(Holder<BlockStateProvider> state, Optional<Direction> direction) implements BlockStateProvider
{
    public static final MapCodec<RotatedBlockProvider> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("state").forGetter(RotatedBlockProvider::state), (App)Direction.CODEC.optionalFieldOf("direction").forGetter(RotatedBlockProvider::direction)).apply((Applicative)i, RotatedBlockProvider::new));

    public RotatedBlockProvider(BlockStateProvider state) {
        this(Holder.direct(state), Optional.empty());
    }

    public MapCodec<RotatedBlockProvider> codec() {
        return CODEC;
    }

    @Override
    public BlockState getState(LevelAccessor level, RandomSource random, BlockPos pos) {
        Direction direction = this.direction.orElseGet(() -> Direction.getRandom(random));
        BlockState newState = (BlockState)((BlockState)this.state.value().getState(level, random, pos).trySetValue(BlockStateProperties.AXIS, direction.getAxis())).trySetValue(BlockStateProperties.FACING, direction);
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)newState.trySetValue(BlockStateProperties.HORIZONTAL_FACING, direction);
        }
        return newState;
    }
}

