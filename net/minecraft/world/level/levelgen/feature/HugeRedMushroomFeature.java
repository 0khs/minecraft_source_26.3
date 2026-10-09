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

public record HugeRedMushroomFeature(Holder<BlockStateProvider> capProvider, Holder<BlockStateProvider> stemProvider, int foliageRadius, BlockPredicate canPlaceOn) implements AbstractHugeMushroomFeature
{
    public static final MapCodec<HugeRedMushroomFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("cap_provider").forGetter(HugeRedMushroomFeature::capProvider), (App)BlockStateProvider.CODEC.fieldOf("stem_provider").forGetter(HugeRedMushroomFeature::stemProvider), (App)Codec.INT.optionalFieldOf("foliage_radius", (Object)2).forGetter(HugeRedMushroomFeature::foliageRadius), (App)BlockPredicate.CODEC.fieldOf("can_place_on").forGetter(HugeRedMushroomFeature::canPlaceOn)).apply((Applicative)i, HugeRedMushroomFeature::new));

    public MapCodec<HugeRedMushroomFeature> codec() {
        return CODEC;
    }

    @Override
    public void makeCap(WorldGenLevel level, RandomSource random, BlockPos origin, int treeHeight, BlockPos.MutableBlockPos blockPos) {
        for (int dy = treeHeight - 3; dy <= treeHeight; ++dy) {
            int radius = dy < treeHeight ? this.foliageRadius : this.foliageRadius - 1;
            int center = this.foliageRadius - 2;
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    boolean zEdge;
                    boolean minX = dx == -radius;
                    boolean maxX = dx == radius;
                    boolean minZ = dz == -radius;
                    boolean maxZ = dz == radius;
                    boolean xEdge = minX || maxX;
                    boolean bl = zEdge = minZ || maxZ;
                    if (dy < treeHeight && xEdge == zEdge) continue;
                    blockPos.setWithOffset(origin, dx, dy, dz);
                    BlockState state = this.capProvider.value().getState(level, random, origin);
                    if (state.hasProperty(HugeMushroomBlock.WEST) && state.hasProperty(HugeMushroomBlock.EAST) && state.hasProperty(HugeMushroomBlock.NORTH) && state.hasProperty(HugeMushroomBlock.SOUTH) && state.hasProperty(HugeMushroomBlock.UP)) {
                        state = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.setValue(HugeMushroomBlock.UP, dy >= treeHeight - 1)).setValue(HugeMushroomBlock.WEST, dx < -center)).setValue(HugeMushroomBlock.EAST, dx > center)).setValue(HugeMushroomBlock.NORTH, dz < -center)).setValue(HugeMushroomBlock.SOUTH, dz > center);
                    }
                    this.placeMushroomBlock(level, blockPos, state);
                }
            }
        }
    }

    @Override
    public int getTreeRadiusForHeight(int trunkHeight, int treeHeight, int leafRadius, int yo) {
        int radius = 0;
        if (yo < treeHeight && yo >= treeHeight - 3) {
            radius = leafRadius;
        } else if (yo == treeHeight) {
            radius = leafRadius;
        }
        return radius;
    }
}

