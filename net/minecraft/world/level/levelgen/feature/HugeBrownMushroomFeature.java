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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.AbstractHugeMushroomFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record HugeBrownMushroomFeature(Holder<BlockStateProvider> capProvider, Holder<BlockStateProvider> stemProvider, int foliageRadius, BlockPredicate canPlaceOn) implements AbstractHugeMushroomFeature
{
    public static final MapCodec<HugeBrownMushroomFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("cap_provider").forGetter(HugeBrownMushroomFeature::capProvider), (App)BlockStateProvider.CODEC.fieldOf("stem_provider").forGetter(HugeBrownMushroomFeature::stemProvider), (App)Codec.INT.optionalFieldOf("foliage_radius", (Object)2).forGetter(HugeBrownMushroomFeature::foliageRadius), (App)BlockPredicate.CODEC.fieldOf("can_place_on").forGetter(HugeBrownMushroomFeature::canPlaceOn)).apply((Applicative)i, HugeBrownMushroomFeature::new));

    public MapCodec<HugeBrownMushroomFeature> codec() {
        return CODEC;
    }

    @Override
    public void makeCap(WorldGenLevel level, RandomSource random, BlockPos origin, int treeHeight, BlockPos.MutableBlockPos blockPos) {
        for (int dx = -this.foliageRadius; dx <= this.foliageRadius; ++dx) {
            for (int dz = -this.foliageRadius; dz <= this.foliageRadius; ++dz) {
                boolean zEdge;
                boolean minX = dx == -this.foliageRadius;
                boolean maxX = dx == this.foliageRadius;
                boolean minZ = dz == -this.foliageRadius;
                boolean maxZ = dz == this.foliageRadius;
                boolean xEdge = minX || maxX;
                boolean bl = zEdge = minZ || maxZ;
                if (xEdge && zEdge) continue;
                blockPos.setWithOffset(origin, dx, treeHeight, dz);
                boolean west = minX || zEdge && dx == 1 - this.foliageRadius;
                boolean east = maxX || zEdge && dx == this.foliageRadius - 1;
                boolean north = minZ || xEdge && dz == 1 - this.foliageRadius;
                boolean south = maxZ || xEdge && dz == this.foliageRadius - 1;
                BlockState state = this.capProvider.value().getState(level, random, origin);
                if (state.hasProperty(HugeMushroomBlock.WEST) && state.hasProperty(HugeMushroomBlock.EAST) && state.hasProperty(HugeMushroomBlock.NORTH) && state.hasProperty(HugeMushroomBlock.SOUTH)) {
                    state = (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(HugeMushroomBlock.WEST, west)).setValue(HugeMushroomBlock.EAST, east)).setValue(HugeMushroomBlock.NORTH, north)).setValue(HugeMushroomBlock.SOUTH, south);
                }
                this.placeMushroomBlock(level, blockPos, state);
            }
        }
    }

    @Override
    public int getTreeRadiusForHeight(int trunkHeight, int treeHeight, int leafRadius, int yo) {
        return yo <= 3 ? 0 : leafRadius;
    }
}

