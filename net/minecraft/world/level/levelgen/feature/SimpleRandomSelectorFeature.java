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
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record SimpleRandomSelectorFeature(HolderSet<PlacedFeature> features) implements Feature
{
    public static final MapCodec<SimpleRandomSelectorFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)ExtraCodecs.nonEmptyHolderSet(PlacedFeature.LIST_CODEC).fieldOf("features").forGetter(SimpleRandomSelectorFeature::features)).apply((Applicative)i, SimpleRandomSelectorFeature::new));

    public MapCodec<SimpleRandomSelectorFeature> codec() {
        return CODEC;
    }

    @Override
    public Stream<Holder<Feature>> getSubFeatures() {
        return this.features.stream().flatMap(f -> ((PlacedFeature)f.value()).getFeatures());
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int index = random.nextInt(this.features.size());
        PlacedFeature feature = this.features.get(index).value();
        return feature.place(level, chunkGenerator, random, origin);
    }
}

