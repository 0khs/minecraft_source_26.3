/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record ProjectedRandomPatchySquare(Holder<BlockStateProvider> block, BlockPredicate projectThrough, IntProvider size, int maxProjectionHeight) implements Feature
{
    public static final MapCodec<ProjectedRandomPatchySquare> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("block").forGetter(ProjectedRandomPatchySquare::block), (App)BlockPredicate.CODEC.fieldOf("project_through").forGetter(ProjectedRandomPatchySquare::projectThrough), (App)IntProviders.codec(1, 16).fieldOf("size").forGetter(ProjectedRandomPatchySquare::size), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_projection_height").forGetter(ProjectedRandomPatchySquare::maxProjectionHeight)).apply((Applicative)i, ProjectedRandomPatchySquare::new));

    public MapCodec<ProjectedRandomPatchySquare> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos pos) {
        BlockPos.MutableBlockPos basePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos tmpPos = new BlockPos.MutableBlockPos();
        int size = this.size.sample(random);
        int bound = Mth.square(size) + 1;
        for (int dx = -size; dx <= size; ++dx) {
            for (int dz = -size; dz <= size; ++dz) {
                BlockState state;
                int probability = Mth.abs(dx) * Mth.abs(dz);
                if (random.nextInt(bound) >= bound - probability) continue;
                basePos.setWithOffset(pos, dx, 0, dz);
                int drop = this.maxProjectionHeight;
                while (this.projectThrough.test(level, tmpPos.setWithOffset((Vec3i)basePos, Direction.DOWN))) {
                    basePos.move(Direction.DOWN);
                    if (--drop > 0) continue;
                }
                if ((state = this.block.value().getOptionalState(level, random, basePos)) == null) continue;
                level.setBlock(basePos, state, 2);
            }
        }
        return true;
    }
}

