/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.FeaturePlacer;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public record PlacedFeature(Holder<Feature> feature, List<PlacementModifier> placement) {
    public static final Codec<PlacedFeature> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group((App)Feature.CODEC.fieldOf("feature").forGetter(c -> c.feature), (App)PlacementModifier.CODEC.listOf().fieldOf("placement").forGetter(c -> c.placement)).apply((Applicative)i, PlacedFeature::new));
    public static final Codec<Holder<PlacedFeature>> CODEC = RegistryCodecs.holder(Registries.PLACED_FEATURE, DIRECT_CODEC);
    public static final Codec<HolderSet<PlacedFeature>> LIST_CODEC = RegistryCodecs.holderSet(Registries.PLACED_FEATURE, DIRECT_CODEC);
    public static final Codec<List<HolderSet<PlacedFeature>>> LIST_OF_LISTS_CODEC = RegistryCodecs.holderSet(Registries.PLACED_FEATURE, DIRECT_CODEC, true).listOf();

    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        FeaturePlacer placer = new FeaturePlacer(level, generator);
        return placer.place(this, random, origin);
    }

    public Stream<Holder<Feature>> getFeatures() {
        return Stream.concat(Stream.of(this.feature), this.feature.value().getSubFeatures());
    }

    @Override
    public String toString() {
        return "Placed " + String.valueOf(this.feature);
    }
}

