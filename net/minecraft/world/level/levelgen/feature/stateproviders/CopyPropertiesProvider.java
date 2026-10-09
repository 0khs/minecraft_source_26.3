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
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record CopyPropertiesProvider(Holder<BlockStateProvider> source) implements BlockStateProvider
{
    public static final MapCodec<CopyPropertiesProvider> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("source").forGetter(CopyPropertiesProvider::source)).apply((Applicative)i, CopyPropertiesProvider::new));

    public CopyPropertiesProvider(Block block) {
        this(BlockStateProvider.holderOf(block));
    }

    public MapCodec<CopyPropertiesProvider> codec() {
        return CODEC;
    }

    @Override
    public BlockState getState(LevelAccessor level, RandomSource random, BlockPos pos) {
        return this.source.value().getState(level, random, pos).withPropertiesOf(level.getBlockState(pos));
    }
}

