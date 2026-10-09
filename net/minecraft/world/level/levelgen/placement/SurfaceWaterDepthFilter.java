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

public record SurfaceWaterDepthFilter(int maxWaterDepth) implements PlacementFilter
{
    public static final MapCodec<SurfaceWaterDepthFilter> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.INT.fieldOf("max_water_depth").forGetter(SurfaceWaterDepthFilter::maxWaterDepth)).apply((Applicative)i, SurfaceWaterDepthFilter::new));

    public static SurfaceWaterDepthFilter forMaxDepth(int maxWaterDepth) {
        return new SurfaceWaterDepthFilter(maxWaterDepth);
    }

    @Override
    public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
        int yOceanFloor = context.getHeight(Heightmap.Types.OCEAN_FLOOR, origin.getX(), origin.getZ());
        int ySurfaceFloor = context.getHeight(Heightmap.Types.WORLD_SURFACE, origin.getX(), origin.getZ());
        return ySurfaceFloor - yOceanFloor <= this.maxWaterDepth;
    }

    public MapCodec<SurfaceWaterDepthFilter> codec() {
        return CODEC;
    }
}

