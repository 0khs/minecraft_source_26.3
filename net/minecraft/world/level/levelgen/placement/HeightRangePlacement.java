/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public record HeightRangePlacement(HeightProvider height) implements PlacementModifier
{
    public static final MapCodec<HeightRangePlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)HeightProvider.CODEC.fieldOf("height").forGetter(HeightRangePlacement::height)).apply((Applicative)i, HeightRangePlacement::new));

    public static HeightRangePlacement of(HeightProvider height) {
        return new HeightRangePlacement(height);
    }

    public static HeightRangePlacement uniform(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) {
        return HeightRangePlacement.of(UniformHeight.of(minInclusive, maxInclusive));
    }

    public static HeightRangePlacement triangle(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) {
        return HeightRangePlacement.of(TrapezoidHeight.of(minInclusive, maxInclusive));
    }

    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        output.accept(origin.atY(this.height.sample(random, context)));
    }

    public MapCodec<HeightRangePlacement> codec() {
        return CODEC;
    }
}

