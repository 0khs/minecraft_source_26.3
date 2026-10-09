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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.jspecify.annotations.Nullable;

public record SteppedColumnClusterFeature(Holder<BlockStateProvider> block, BlockPredicate continueThrough, BlockPredicate canReplace, HolderSet<Block> cannotPlaceOn, IntProvider clusterReach, IntProvider columnCount, IntProvider columnReach, IntProvider height) implements Feature
{
    public static final MapCodec<SteppedColumnClusterFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("block").forGetter(SteppedColumnClusterFeature::block), (App)BlockPredicate.CODEC.fieldOf("continue_through").forGetter(SteppedColumnClusterFeature::continueThrough), (App)BlockPredicate.CODEC.fieldOf("can_replace").forGetter(SteppedColumnClusterFeature::canReplace), (App)RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("cannot_place_on").forGetter(SteppedColumnClusterFeature::cannotPlaceOn), (App)IntProviders.codec(0, 13).fieldOf("cluster_reach").forGetter(SteppedColumnClusterFeature::clusterReach), (App)IntProviders.codec(1, 150).fieldOf("column_count").forGetter(SteppedColumnClusterFeature::columnCount), (App)IntProviders.codec(0, 3).fieldOf("column_reach").forGetter(SteppedColumnClusterFeature::columnReach), (App)IntProviders.codec(1, 10).fieldOf("height").forGetter(SteppedColumnClusterFeature::height)).apply((Applicative)i, SteppedColumnClusterFeature::new));

    public MapCodec<SteppedColumnClusterFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int lavaSeaLevel = chunkGenerator.getSeaLevel();
        if (!this.canPlaceAt(level, origin.mutable())) {
            return false;
        }
        int columnHeight = this.height.sample(random);
        int clusterReach = Math.min(columnHeight, this.clusterReach.sample(random));
        int count = this.columnCount.sample(random);
        boolean placed = false;
        for (BlockPos pos : BlockPos.randomBetweenClosed(random, count, origin.getX() - clusterReach, origin.getY(), origin.getZ() - clusterReach, origin.getX() + clusterReach, origin.getY(), origin.getZ() + clusterReach)) {
            int blocksToPlaceY = columnHeight - pos.distManhattan(origin);
            if (blocksToPlaceY < 0) continue;
            placed |= this.placeColumn(level, random, lavaSeaLevel, pos, blocksToPlaceY, this.columnReach.sample(random));
        }
        return placed;
    }

    private boolean placeColumn(WorldGenLevel level, RandomSource random, int lavaSeaLevel, BlockPos origin, int columnHeight, int reach) {
        boolean placedAny = false;
        block0: for (BlockPos pos : BlockPos.betweenClosed(origin.getX() - reach, origin.getY(), origin.getZ() - reach, origin.getX() + reach, origin.getY(), origin.getZ() + reach)) {
            BlockPos columnPos;
            int stepLimit = pos.distManhattan(origin);
            BlockPos blockPos = columnPos = this.canReplace.test(level, pos) ? this.findSurface(level, lavaSeaLevel, pos.mutable(), stepLimit) : this.findAir(level, pos.mutable(), stepLimit);
            if (columnPos == null) continue;
            BlockPos.MutableBlockPos cursor = columnPos.mutable();
            for (int blocksY = columnHeight - stepLimit / 2; blocksY >= 0; --blocksY) {
                if (this.canReplace.test(level, cursor)) {
                    this.setBlock(level, cursor, this.block.value().getState(level, random, cursor));
                    cursor.move(Direction.UP);
                    placedAny = true;
                    continue;
                }
                if (!this.continueThrough.test(level, cursor)) continue block0;
                cursor.move(Direction.UP);
            }
        }
        return placedAny;
    }

    private @Nullable BlockPos findSurface(WorldGenLevel level, int lavaSeaLevel, BlockPos.MutableBlockPos cursor, int limit) {
        while (cursor.getY() > level.getMinY() + 1 && limit > 0) {
            --limit;
            if (this.canPlaceAt(level, cursor)) {
                return cursor;
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    private boolean canPlaceAt(WorldGenLevel level, BlockPos.MutableBlockPos cursor) {
        if (this.canReplace.test(level, cursor)) {
            BlockState blockState = level.getBlockState(cursor.move(Direction.DOWN));
            cursor.move(Direction.UP);
            return !blockState.isAir() && !blockState.is(this.cannotPlaceOn);
        }
        return false;
    }

    private @Nullable BlockPos findAir(LevelAccessor level, BlockPos.MutableBlockPos cursor, int limit) {
        while (cursor.getY() <= level.getMaxY() && limit > 0) {
            --limit;
            BlockState blockState = level.getBlockState(cursor);
            if (blockState.is(this.cannotPlaceOn)) {
                return null;
            }
            if (blockState.isAir()) {
                return cursor;
            }
            cursor.move(Direction.UP);
        }
        return null;
    }
}

