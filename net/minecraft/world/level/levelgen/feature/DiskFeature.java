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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record DiskFeature(Holder<BlockStateProvider> stateProvider, BlockPredicate target, IntProvider radius, int halfHeight) implements Feature
{
    public static final MapCodec<DiskFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(DiskFeature::stateProvider), (App)BlockPredicate.CODEC.fieldOf("target").forGetter(DiskFeature::target), (App)IntProviders.codec(0, 8).fieldOf("radius").forGetter(DiskFeature::radius), (App)Codec.intRange((int)0, (int)4).fieldOf("half_height").forGetter(DiskFeature::halfHeight)).apply((Applicative)i, DiskFeature::new));

    public MapCodec<DiskFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        boolean placedAny = false;
        int originY = origin.getY();
        int top = originY + this.halfHeight;
        int bottom = originY - this.halfHeight - 1;
        int r = this.radius.sample(random);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (BlockPos columnPos : BlockPos.betweenClosed(origin.offset(-r, 0, -r), origin.offset(r, 0, r))) {
            int zd;
            int xd = columnPos.getX() - origin.getX();
            if (xd * xd + (zd = columnPos.getZ() - origin.getZ()) * zd > r * r) continue;
            placedAny |= this.placeColumn(level, random, top, bottom, mutablePos.set(columnPos));
        }
        return placedAny;
    }

    private boolean placeColumn(WorldGenLevel level, RandomSource random, int top, int bottom, BlockPos.MutableBlockPos pos) {
        boolean placedAny = false;
        boolean placedAbove = false;
        for (int y = top; y > bottom; --y) {
            pos.setY(y);
            if (this.target.test(level, pos)) {
                BlockState state = this.stateProvider.value().getOptionalState(level, random, pos);
                if (state == null) continue;
                level.setBlock(pos, state, 2);
                if (!placedAbove) {
                    this.markAboveForPostProcessing(level, pos);
                }
                placedAny = true;
                placedAbove = true;
                continue;
            }
            placedAbove = false;
        }
        return placedAny;
    }
}

