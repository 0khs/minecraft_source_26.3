/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.FeaturePlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record SequenceFeature(HolderSet<PlacedFeature> features) implements Feature
{
    public static final MapCodec<SequenceFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)ExtraCodecs.nonEmptyHolderSet(PlacedFeature.LIST_CODEC).fieldOf("features").forGetter(SequenceFeature::features)).apply((Applicative)i, SequenceFeature::new));

    public MapCodec<SequenceFeature> codec() {
        return CODEC;
    }

    @Override
    public Stream<Holder<Feature>> getSubFeatures() {
        return this.features.stream().flatMap(f -> ((PlacedFeature)f.value()).getFeatures());
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        FeaturePlacer placer = new FeaturePlacer(level, chunkGenerator);
        for (Holder holder : this.features) {
            if (placer.place((PlacedFeature)holder.value(), random, origin)) continue;
            return false;
        }
        return true;
    }
}

