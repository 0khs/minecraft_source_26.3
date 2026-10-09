/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record MultifaceGrowthFeature(Block placeBlock, int searchRange, boolean canPlaceOnFloor, boolean canPlaceOnCeiling, boolean canPlaceOnWall, float chanceOfSpreading, HolderSet<Block> canBePlacedOn) implements Feature
{
    public static final MapCodec<MultifaceGrowthFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BuiltInRegistries.BLOCK.byNameCodec().validate(MultifaceGrowthFeature::validateBlock).fieldOf("block").forGetter(MultifaceGrowthFeature::placeBlock), (App)Codec.intRange((int)1, (int)64).optionalFieldOf("search_range", (Object)10).forGetter(MultifaceGrowthFeature::searchRange), (App)Codec.BOOL.optionalFieldOf("can_place_on_floor", (Object)false).forGetter(MultifaceGrowthFeature::canPlaceOnFloor), (App)Codec.BOOL.optionalFieldOf("can_place_on_ceiling", (Object)false).forGetter(MultifaceGrowthFeature::canPlaceOnCeiling), (App)Codec.BOOL.optionalFieldOf("can_place_on_wall", (Object)false).forGetter(MultifaceGrowthFeature::canPlaceOnWall), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("chance_of_spreading", (Object)Float.valueOf(0.5f)).forGetter(MultifaceGrowthFeature::chanceOfSpreading), (App)RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("can_be_placed_on").forGetter(MultifaceGrowthFeature::canBePlacedOn)).apply((Applicative)i, MultifaceGrowthFeature::new));

    private static DataResult<Block> validateBlock(Block block) {
        DataResult dataResult;
        if (block instanceof MultifaceSpreadeableBlock) {
            MultifaceSpreadeableBlock multifaceBlock = (MultifaceSpreadeableBlock)block;
            dataResult = DataResult.success((Object)multifaceBlock);
        } else {
            dataResult = DataResult.error(() -> "Growth block should be a multiface spreadeable block");
        }
        return dataResult;
    }

    public MapCodec<MultifaceGrowthFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (!MultifaceGrowthFeature.isAirOrWater(level.getBlockState(origin))) {
            return false;
        }
        Block block = this.placeBlock;
        if (!(block instanceof MultifaceSpreadeableBlock)) {
            return false;
        }
        MultifaceSpreadeableBlock placerBlock = (MultifaceSpreadeableBlock)block;
        List<Direction> searchDirections = this.getShuffledDirections(random);
        if (this.placeGrowthIfPossible(placerBlock, level, origin, level.getBlockState(origin), random, searchDirections)) {
            return true;
        }
        BlockPos.MutableBlockPos pos = origin.mutable();
        block0: for (Direction searchDirection : searchDirections) {
            pos.set(origin);
            List<Direction> placementDirections = this.getShuffledDirectionsExcept(random, searchDirection.getOpposite());
            for (int i = 0; i < this.searchRange; ++i) {
                pos.setWithOffset((Vec3i)origin, searchDirection);
                BlockState state = level.getBlockState(pos);
                if (!MultifaceGrowthFeature.isAirOrWater(state) && !state.is(this.placeBlock)) continue block0;
                if (!this.placeGrowthIfPossible(placerBlock, level, pos, state, random, placementDirections)) continue;
                return true;
            }
        }
        return false;
    }

    public boolean placeGrowthIfPossible(MultifaceSpreadeableBlock placerBlock, WorldGenLevel level, BlockPos pos, BlockState oldState, RandomSource random, List<Direction> placementDirections) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        for (Direction placementDirection : placementDirections) {
            BlockState neighbourState = level.getBlockState(mutable.setWithOffset((Vec3i)pos, placementDirection));
            if (!neighbourState.is(this.canBePlacedOn)) continue;
            BlockState newState = placerBlock.getStateForPlacement(oldState, level, pos, placementDirection);
            if (newState == null) {
                return false;
            }
            level.setBlockAndUpdate(pos, newState);
            level.getChunk(pos).markPosForPostProcessing(pos);
            if (random.nextFloat() < this.chanceOfSpreading) {
                placerBlock.getSpreader().spreadFromFaceTowardRandomDirection(newState, level, pos, placementDirection, random, true);
            }
            return true;
        }
        return false;
    }

    private ObjectArrayList<Direction> validDirections() {
        ObjectArrayList validDirections = new ObjectArrayList(6);
        if (this.canPlaceOnCeiling) {
            validDirections.add((Object)Direction.UP);
        }
        if (this.canPlaceOnFloor) {
            validDirections.add((Object)Direction.DOWN);
        }
        if (this.canPlaceOnWall) {
            Direction.Plane.HORIZONTAL.forEach(arg_0 -> ((ObjectArrayList)validDirections).add(arg_0));
        }
        return validDirections;
    }

    private List<Direction> getShuffledDirectionsExcept(RandomSource random, Direction excludeDirection) {
        return Util.toShuffledList(this.validDirections().stream().filter(direction -> direction != excludeDirection), random);
    }

    private List<Direction> getShuffledDirections(RandomSource random) {
        return Util.shuffledCopy(this.validDirections(), random);
    }

    private static boolean isAirOrWater(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER);
    }
}

