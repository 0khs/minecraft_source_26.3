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
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpeleothemUtils;

public record SpeleothemFeature(BlockState baseBlock, BlockState pointedBlock, HolderSet<Block> replaceableBlocks, float chanceOfTallerGeneration, float chanceOfDirectionalSpread, float chanceOfSpreadRadius2, float chanceOfSpreadRadius3) implements Feature
{
    public static final MapCodec<SpeleothemFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockState.CODEC.fieldOf("base_block").forGetter(SpeleothemFeature::baseBlock), (App)BlockState.CODEC.fieldOf("pointed_block").forGetter(SpeleothemFeature::pointedBlock), (App)RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("replaceable_blocks").forGetter(SpeleothemFeature::replaceableBlocks), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("chance_of_taller_generation", (Object)Float.valueOf(0.2f)).forGetter(SpeleothemFeature::chanceOfTallerGeneration), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("chance_of_directional_spread", (Object)Float.valueOf(0.7f)).forGetter(SpeleothemFeature::chanceOfDirectionalSpread), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("chance_of_spread_radius2", (Object)Float.valueOf(0.5f)).forGetter(SpeleothemFeature::chanceOfSpreadRadius2), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("chance_of_spread_radius3", (Object)Float.valueOf(0.5f)).forGetter(SpeleothemFeature::chanceOfSpreadRadius3)).apply((Applicative)i, SpeleothemFeature::new));

    public MapCodec<SpeleothemFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Optional<Direction> tipDirection = this.getTipDirection(level, origin, random);
        if (tipDirection.isEmpty()) {
            return false;
        }
        BlockPos rootPos = origin.relative(tipDirection.get().getOpposite());
        this.createPatchOfBaseBlocks(level, random, rootPos);
        int height = random.nextFloat() < this.chanceOfTallerGeneration && SpeleothemUtils.isEmptyOrWater(level.getBlockState(origin.relative(tipDirection.get()))) ? 2 : 1;
        SpeleothemUtils.growSpeleothem(level, origin, tipDirection.get(), height, false, this.baseBlock.getBlock(), this.pointedBlock.getBlock(), this.replaceableBlocks);
        return true;
    }

    private Optional<Direction> getTipDirection(LevelAccessor level, BlockPos pos, RandomSource random) {
        boolean canPlaceAbove = SpeleothemUtils.isBase(level.getBlockState(pos.above()), this.baseBlock.getBlock(), this.replaceableBlocks);
        boolean canPlaceBelow = SpeleothemUtils.isBase(level.getBlockState(pos.below()), this.baseBlock.getBlock(), this.replaceableBlocks);
        if (canPlaceAbove && canPlaceBelow) {
            return Optional.of(random.nextBoolean() ? Direction.DOWN : Direction.UP);
        }
        if (canPlaceAbove) {
            return Optional.of(Direction.DOWN);
        }
        if (canPlaceBelow) {
            return Optional.of(Direction.UP);
        }
        return Optional.empty();
    }

    private void createPatchOfBaseBlocks(LevelAccessor level, RandomSource random, BlockPos pos) {
        SpeleothemUtils.placeBaseBlockIfPossible(level, pos, this.baseBlock.getBlock(), this.replaceableBlocks);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() > this.chanceOfDirectionalSpread) continue;
            BlockPos pos1 = pos.relative(direction);
            SpeleothemUtils.placeBaseBlockIfPossible(level, pos1, this.baseBlock.getBlock(), this.replaceableBlocks);
            if (random.nextFloat() > this.chanceOfSpreadRadius2) continue;
            BlockPos pos2 = pos1.relative(Direction.getRandom(random));
            SpeleothemUtils.placeBaseBlockIfPossible(level, pos2, this.baseBlock.getBlock(), this.replaceableBlocks);
            if (random.nextFloat() > this.chanceOfSpreadRadius3) continue;
            BlockPos pos3 = pos2.relative(Direction.getRandom(random));
            SpeleothemUtils.placeBaseBlockIfPossible(level, pos3, this.baseBlock.getBlock(), this.replaceableBlocks);
        }
    }
}

