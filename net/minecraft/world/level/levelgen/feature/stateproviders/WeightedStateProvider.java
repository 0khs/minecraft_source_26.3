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
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record WeightedStateProvider(WeightedList<BlockState> weightedList) implements BlockStateProvider
{
    public static final MapCodec<WeightedStateProvider> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)WeightedList.nonEmptyCodec(BlockState.CODEC).fieldOf("entries").forGetter(o -> o.weightedList)).apply((Applicative)i, WeightedStateProvider::new));

    public WeightedStateProvider {
        if (weightedList.isEmpty()) {
            throw new IllegalArgumentException("Weighted list must have at least one entry");
        }
    }

    public WeightedStateProvider(WeightedList.Builder<BlockState> weightedList) {
        this(weightedList.build());
    }

    public MapCodec<WeightedStateProvider> codec() {
        return CODEC;
    }

    @Override
    public BlockState getState(LevelAccessor level, RandomSource random, BlockPos pos) {
        return this.weightedList.getRandomOrThrow(random);
    }
}

