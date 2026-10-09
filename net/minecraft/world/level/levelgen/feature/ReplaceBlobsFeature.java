/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.jspecify.annotations.Nullable;

public record ReplaceBlobsFeature(BlockState targetState, BlockState replaceState, IntProvider radius) implements Feature
{
    public static final MapCodec<ReplaceBlobsFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockState.CODEC.fieldOf("target").forGetter(ReplaceBlobsFeature::targetState), (App)BlockState.CODEC.fieldOf("state").forGetter(ReplaceBlobsFeature::replaceState), (App)IntProviders.codec(0, 12).fieldOf("radius").forGetter(ReplaceBlobsFeature::radius)).apply((Applicative)i, ReplaceBlobsFeature::new));

    public MapCodec<ReplaceBlobsFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Block targetBlock = this.targetState.getBlock();
        BlockPos centerPos = ReplaceBlobsFeature.findTarget(level, origin.mutable().clamp(Direction.Axis.Y, level.getMinY() + 1, level.getMaxY()), targetBlock);
        if (centerPos == null) {
            return false;
        }
        int radiusX = this.radius.sample(random);
        int radiusY = this.radius.sample(random);
        int radiusZ = this.radius.sample(random);
        int maximumRadius = Math.max(radiusX, Math.max(radiusY, radiusZ));
        boolean replacedAny = false;
        for (BlockPos pos : BlockPos.withinBoxByManhattanDistance(centerPos, radiusX, radiusY, radiusZ)) {
            if (pos.distManhattan(centerPos) > maximumRadius) break;
            BlockState blockState = level.getBlockState(pos);
            if (!blockState.is(targetBlock)) continue;
            this.setBlock(level, pos, this.replaceState);
            replacedAny = true;
        }
        return replacedAny;
    }

    private static @Nullable BlockPos findTarget(LevelAccessor level, BlockPos.MutableBlockPos cursor, Block target) {
        while (cursor.getY() > level.getMinY() + 1) {
            BlockState blockState = level.getBlockState(cursor);
            if (blockState.is(target)) {
                return cursor;
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }
}

