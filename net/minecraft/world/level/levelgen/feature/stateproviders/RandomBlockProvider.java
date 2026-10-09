/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.jspecify.annotations.Nullable;

public record RandomBlockProvider(HolderSet<Block> blocks) implements BlockStateProvider
{
    public static final MapCodec<RandomBlockProvider> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("blocks").forGetter(RandomBlockProvider::blocks)).apply((Applicative)i, RandomBlockProvider::new));

    public MapCodec<RandomBlockProvider> codec() {
        return CODEC;
    }

    @Override
    public BlockState getState(LevelAccessor level, RandomSource random, BlockPos pos) {
        return this.getState(random).orElseGet(() -> level.getBlockState(pos));
    }

    @Override
    public @Nullable BlockState getOptionalState(LevelAccessor level, RandomSource random, BlockPos pos) {
        return this.getState(random).orElse(null);
    }

    private Optional<BlockState> getState(RandomSource random) {
        return this.blocks.getRandomElement(random).map(Holder::value).map(Block::defaultBlockState);
    }
}

