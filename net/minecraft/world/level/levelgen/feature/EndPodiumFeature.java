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
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record EndPodiumFeature(boolean active) implements Feature
{
    public static final int PODIUM_RADIUS = 4;
    public static final int PODIUM_PILLAR_HEIGHT = 4;
    public static final int RIM_RADIUS = 1;
    public static final float CORNER_ROUNDING = 0.5f;
    public static final MapCodec<EndPodiumFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.BOOL.optionalFieldOf("active", (Object)false).forGetter(EndPodiumFeature::active)).apply((Applicative)i, EndPodiumFeature::new));

    public MapCodec<EndPodiumFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(origin.getX() - 4, origin.getY() - 1, origin.getZ() - 4), new BlockPos(origin.getX() + 4, origin.getY() + 32, origin.getZ() + 4))) {
            boolean insideRim = pos.closerThan(origin, 2.5);
            if (!insideRim && !pos.closerThan(origin, 3.5)) continue;
            if (pos.getY() < origin.getY()) {
                if (insideRim) {
                    this.setBlock(level, pos, Blocks.BEDROCK.defaultBlockState());
                    continue;
                }
                if (pos.getY() >= origin.getY()) continue;
                if (this.active) {
                    this.dropPreviousAndSetBlock(level, pos, Blocks.END_STONE);
                    continue;
                }
                this.setBlock(level, pos, Blocks.END_STONE.defaultBlockState());
                continue;
            }
            if (pos.getY() > origin.getY()) {
                if (this.active) {
                    this.dropPreviousAndSetBlock(level, pos, Blocks.AIR);
                    continue;
                }
                this.setBlock(level, pos, Blocks.AIR.defaultBlockState());
                continue;
            }
            if (!insideRim) {
                this.setBlock(level, pos, Blocks.BEDROCK.defaultBlockState());
                continue;
            }
            if (this.active) {
                this.dropPreviousAndSetBlock(level, pos, Blocks.END_PORTAL);
                continue;
            }
            this.setBlock(level, pos, Blocks.AIR.defaultBlockState());
        }
        for (int y = 0; y < 4; ++y) {
            this.setBlock(level, origin.above(y), Blocks.BEDROCK.defaultBlockState());
        }
        BlockPos centerOfPillar = origin.above(2);
        for (Direction face : Direction.Plane.HORIZONTAL) {
            this.setBlock(level, centerOfPillar.relative(face), (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, face));
        }
        return true;
    }

    private void dropPreviousAndSetBlock(WorldGenLevel level, BlockPos pos, Block block) {
        if (!level.getBlockState(pos).is(block)) {
            level.destroyBlock(pos, true, null);
            this.setBlock(level, pos, block.defaultBlockState());
        }
    }
}

