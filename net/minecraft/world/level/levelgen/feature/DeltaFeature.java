/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record DeltaFeature(BlockState contents, BlockState rim, IntProvider size, IntProvider rimSize) implements Feature
{
    public static final MapCodec<DeltaFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockState.CODEC.fieldOf("contents").forGetter(DeltaFeature::contents), (App)BlockState.CODEC.fieldOf("rim").forGetter(DeltaFeature::rim), (App)IntProviders.codec(0, 16).fieldOf("size").forGetter(DeltaFeature::size), (App)IntProviders.codec(0, 16).fieldOf("rim_size").forGetter(DeltaFeature::rimSize)).apply((Applicative)i, DeltaFeature::new));
    private static final ImmutableList<Block> CANNOT_REPLACE = ImmutableList.of((Object)Blocks.BEDROCK, (Object)Blocks.NETHER_BRICKS, (Object)Blocks.NETHER_BRICK_FENCE, (Object)Blocks.NETHER_BRICK_STAIRS, (Object)Blocks.NETHER_WART, (Object)Blocks.CHEST, (Object)Blocks.SPAWNER);
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final double RIM_SPAWN_CHANCE = 0.9;

    public MapCodec<DeltaFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        boolean anyPlaced = false;
        boolean spawnRim = random.nextDouble() < 0.9;
        int rimX = spawnRim ? this.rimSize.sample(random) : 0;
        int rimZ = spawnRim ? this.rimSize.sample(random) : 0;
        boolean hasRim = spawnRim && rimX != 0 && rimZ != 0;
        int radiusX = this.size.sample(random);
        int radiusZ = this.size.sample(random);
        int radiusLimit = Math.max(radiusX, radiusZ);
        for (BlockPos pos : BlockPos.withinBoxByManhattanDistance(origin, radiusX, 0, radiusZ)) {
            BlockPos posOffset;
            if (pos.distManhattan(origin) > radiusLimit) break;
            if (!this.isClear(level, pos)) continue;
            if (hasRim) {
                anyPlaced = true;
                this.setBlock(level, pos, this.rim);
            }
            if (!this.isClear(level, posOffset = pos.offset(rimX, 0, rimZ))) continue;
            anyPlaced = true;
            this.setBlock(level, posOffset, this.contents);
        }
        return anyPlaced;
    }

    private boolean isClear(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this.contents.getBlock())) {
            return false;
        }
        if (CANNOT_REPLACE.contains((Object)state.getBlock())) {
            return false;
        }
        for (Direction d : DIRECTIONS) {
            boolean isAir = level.getBlockState(pos.relative(d)).isAir();
            if ((!isAir || d == Direction.UP) && (isAir || d != Direction.UP)) continue;
            return false;
        }
        return true;
    }
}

