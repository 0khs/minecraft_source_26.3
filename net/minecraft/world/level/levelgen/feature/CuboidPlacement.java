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
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public record CuboidPlacement(IntProvider xzSize, IntProvider ySize, boolean includeEdges, boolean includeInterior) implements PlacementModifier
{
    public static final MapCodec<CuboidPlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)IntProviders.codec(1, 16).fieldOf("xz_size").forGetter(CuboidPlacement::xzSize), (App)IntProviders.codec(1, 16).fieldOf("y_size").forGetter(CuboidPlacement::ySize), (App)Codec.BOOL.optionalFieldOf("include_edges", (Object)true).forGetter(CuboidPlacement::includeEdges), (App)Codec.BOOL.optionalFieldOf("include_interior", (Object)true).forGetter(CuboidPlacement::includeInterior)).apply((Applicative)i, CuboidPlacement::new));

    public MapCodec<CuboidPlacement> codec() {
        return CODEC;
    }

    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        int height = this.ySize.sample(random);
        int width = this.xzSize.sample(random);
        int length = this.xzSize.sample(random);
        BlockPos.MutableBlockPos mutPos = origin.mutable();
        for (int x = 0; x <= width; ++x) {
            for (int y = 0; y <= height; ++y) {
                for (int z = 0; z <= length; ++z) {
                    mutPos.set(x + origin.getX(), y + origin.getY(), z + origin.getZ());
                    if (!((this.includeEdges || x != 0 && x != width || y != 0 && y != height) && (this.includeEdges || z != 0 && z != length || y != 0 && y != height) && (this.includeEdges || x != 0 && x != width || z != 0 && z != length) && (this.includeInterior || x == 0 || x == width || y == 0 || y == height || z == 0 || z == length))) continue;
                    output.accept(mutPos.immutable());
                }
            }
        }
    }
}

