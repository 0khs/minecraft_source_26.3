/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P4
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseBasedStateProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseProvider
extends NoiseBasedStateProvider {
    public static final MapCodec<NoiseProvider> CODEC = RecordCodecBuilder.mapCodec(i -> NoiseProvider.noiseProviderCodec(i).apply((Applicative)i, NoiseProvider::new));
    protected final List<BlockState> states;

    protected static <P extends NoiseProvider> Products.P4<RecordCodecBuilder.Mu<P>, Long, NormalNoise, Float, List<BlockState>> noiseProviderCodec(RecordCodecBuilder.Instance<P> instance) {
        return NoiseProvider.noiseCodec(instance).and((App)ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("states").forGetter(p -> p.states));
    }

    public NoiseProvider(long seed, NormalNoise parameters, float scale, List<BlockState> states) {
        super(seed, parameters, scale);
        this.states = states;
    }

    public MapCodec<? extends NoiseProvider> codec() {
        return CODEC;
    }

    @Override
    public BlockState getState(LevelAccessor level, RandomSource random, BlockPos pos) {
        return this.getRandomState(this.states, pos, this.scale);
    }

    protected BlockState getRandomState(List<BlockState> states, BlockPos pos, double scale) {
        float noiseValue = this.getNoiseValue(pos, scale);
        return this.getRandomState(states, noiseValue);
    }

    protected BlockState getRandomState(List<BlockState> states, float noiseValue) {
        float placementValue = Mth.clamp((1.0f + noiseValue) / 2.0f, 0.0f, 0.9999f);
        return states.get((int)(placementValue * (float)states.size()));
    }
}

