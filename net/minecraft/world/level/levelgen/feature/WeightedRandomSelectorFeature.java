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
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record WeightedRandomSelectorFeature(WeightedList<Holder<PlacedFeature>> features) implements Feature
{
    public static final MapCodec<WeightedRandomSelectorFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)WeightedList.codec(PlacedFeature.CODEC).fieldOf("features").forGetter(WeightedRandomSelectorFeature::features)).apply((Applicative)i, WeightedRandomSelectorFeature::new));

    public MapCodec<WeightedRandomSelectorFeature> codec() {
        return CODEC;
    }

    @Override
    public Stream<Holder<Feature>> getSubFeatures() {
        return this.features.unwrap().stream().flatMap(weighted -> ((PlacedFeature)((Holder)weighted.value()).value()).getFeatures());
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Optional<Holder<PlacedFeature>> featureToPlace = this.features.getRandom(random);
        return featureToPlace.map(placedFeatureHolder -> ((PlacedFeature)placedFeatureHolder.value()).place(level, chunkGenerator, random, origin)).orElse(false);
    }
}

