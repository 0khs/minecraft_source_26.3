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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record RandomBooleanSelectorFeature(Holder<PlacedFeature> featureTrue, Holder<PlacedFeature> featureFalse) implements Feature
{
    public static final MapCodec<RandomBooleanSelectorFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)PlacedFeature.CODEC.fieldOf("feature_true").forGetter(RandomBooleanSelectorFeature::featureTrue), (App)PlacedFeature.CODEC.fieldOf("feature_false").forGetter(RandomBooleanSelectorFeature::featureFalse)).apply((Applicative)i, RandomBooleanSelectorFeature::new));

    public MapCodec<RandomBooleanSelectorFeature> codec() {
        return CODEC;
    }

    @Override
    public Stream<Holder<Feature>> getSubFeatures() {
        return Stream.concat(this.featureTrue.value().getFeatures(), this.featureFalse.value().getFeatures());
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        boolean result = random.nextBoolean();
        return (result ? this.featureTrue : this.featureFalse).value().place(level, chunkGenerator, random, origin);
    }
}

