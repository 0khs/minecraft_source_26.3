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
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

@Deprecated
public record RandomSelectorFeature(List<WeightedPlacedFeature> features, Holder<PlacedFeature> defaultFeature) implements Feature
{
    public static final MapCodec<RandomSelectorFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)WeightedPlacedFeature.CODEC.listOf().fieldOf("features").forGetter(RandomSelectorFeature::features), (App)PlacedFeature.CODEC.fieldOf("default").forGetter(RandomSelectorFeature::defaultFeature)).apply((Applicative)i, RandomSelectorFeature::new));

    public MapCodec<RandomSelectorFeature> codec() {
        return CODEC;
    }

    @Override
    public Stream<Holder<Feature>> getSubFeatures() {
        return Stream.concat(this.features.stream().flatMap(weighted -> weighted.feature().value().getFeatures()), this.defaultFeature.value().getFeatures());
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        for (WeightedPlacedFeature feature : this.features) {
            if (!(random.nextFloat() < feature.chance())) continue;
            return feature.place(level, chunkGenerator, random, origin);
        }
        return this.defaultFeature.value().place(level, chunkGenerator, random, origin);
    }
}

