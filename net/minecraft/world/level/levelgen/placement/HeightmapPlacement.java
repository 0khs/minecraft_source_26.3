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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public record HeightmapPlacement(Heightmap.Types heightmap) implements PlacementModifier
{
    public static final MapCodec<HeightmapPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(HeightmapPlacement::heightmap)).apply((Applicative)i, HeightmapPlacement::new));

    public static HeightmapPlacement onHeightmap(Heightmap.Types heightmap) {
        return new HeightmapPlacement(heightmap);
    }

    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        int z;
        int x = origin.getX();
        int height = context.getHeight(this.heightmap, x, z = origin.getZ());
        if (height > context.getMinY()) {
            output.accept(new BlockPos(x, height, z));
        }
    }

    public MapCodec<HeightmapPlacement> codec() {
        return CODEC;
    }
}

