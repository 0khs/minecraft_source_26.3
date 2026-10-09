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
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record RandomNeighborSpreadFeature(Holder<BlockStateProvider> block, HolderSet<Block> acceptedNeighbors, BlockPredicate canReplace, IntProvider attempts, IntProvider xzOffset, IntProvider yOffset) implements Feature
{
    public static final MapCodec<RandomNeighborSpreadFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("block").forGetter(RandomNeighborSpreadFeature::block), (App)RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("accepted_neighbors").forGetter(RandomNeighborSpreadFeature::acceptedNeighbors), (App)BlockPredicate.CODEC.fieldOf("can_replace").forGetter(RandomNeighborSpreadFeature::canReplace), (App)IntProviders.codec(1, 3000).fieldOf("attempts").forGetter(RandomNeighborSpreadFeature::attempts), (App)IntProviders.codec(-16, 16).fieldOf("xz_offset").forGetter(RandomNeighborSpreadFeature::xzOffset), (App)IntProviders.codec(-16, 16).fieldOf("y_offset").forGetter(RandomNeighborSpreadFeature::yOffset)).apply((Applicative)i, RandomNeighborSpreadFeature::new));
    private static final Direction[] DIRECTIONS = Direction.values();

    public MapCodec<RandomNeighborSpreadFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        level.setBlock(origin, this.block.value().getState(level, random, origin), 2);
        BlockPos.MutableBlockPos placePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        int attempts = this.attempts.sample(random);
        for (int i = 0; i < attempts; ++i) {
            placePos.setWithOffset(origin, this.xzOffset.sample(random), this.yOffset.sample(random), this.xzOffset.sample(random));
            if (!this.canReplace.test(level, placePos)) continue;
            int neighbours = 0;
            for (Direction direction : DIRECTIONS) {
                neighborPos.setWithOffset((Vec3i)placePos, direction);
                if (level.getBlockState(neighborPos).is(this.acceptedNeighbors)) {
                    ++neighbours;
                }
                if (neighbours > 1) break;
            }
            if (neighbours != true) continue;
            level.setBlock(placePos, this.block.value().getState(level, random, placePos), 2);
        }
        return true;
    }
}

