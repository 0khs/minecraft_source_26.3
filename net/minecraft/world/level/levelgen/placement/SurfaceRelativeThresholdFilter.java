/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;

public record SurfaceRelativeThresholdFilter(Heightmap.Types heightmap, int minInclusive, int maxInclusive) implements PlacementFilter
{
    public static final MapCodec<SurfaceRelativeThresholdFilter> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(SurfaceRelativeThresholdFilter::heightmap), (App)Codec.INT.optionalFieldOf("min_inclusive", (Object)Integer.MIN_VALUE).forGetter(SurfaceRelativeThresholdFilter::minInclusive), (App)Codec.INT.optionalFieldOf("max_inclusive", (Object)Integer.MAX_VALUE).forGetter(SurfaceRelativeThresholdFilter::maxInclusive)).apply((Applicative)i, SurfaceRelativeThresholdFilter::new));

    public static SurfaceRelativeThresholdFilter of(Heightmap.Types heightmap, int minInclusive, int maxInclusive) {
        return new SurfaceRelativeThresholdFilter(heightmap, minInclusive, maxInclusive);
    }

    @Override
    public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
        long surfaceY = context.getHeight(this.heightmap, origin.getX(), origin.getZ());
        long minY = surfaceY + (long)this.minInclusive;
        long maxY = surfaceY + (long)this.maxInclusive;
        return minY <= (long)origin.getY() && (long)origin.getY() <= maxY;
    }

    public MapCodec<SurfaceRelativeThresholdFilter> codec() {
        return CODEC;
    }
}

