/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;

public record GeodeFeature(GeodeBlockSettings blockSettings, GeodeLayerSettings layerSettings, GeodeCrackSettings crackSettings, double usePotentialPlacementsChance, double useAlternateLayer0Chance, boolean placementsRequireLayer0Alternate, IntProvider outerWallDistance, IntProvider distributionPoints, IntProvider pointOffset, int minGenOffset, int maxGenOffset, double noiseMultiplier, int invalidBlocksThreshold) implements Feature
{
    private static final NormalNoise NOISE_PARAMETERS = NormalNoise.createParity(-4, 1.0);
    public static final Codec<Double> CHANCE_RANGE = Codec.doubleRange((double)0.0, (double)1.0);
    public static final MapCodec<GeodeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)GeodeBlockSettings.CODEC.fieldOf("blocks").forGetter(GeodeFeature::blockSettings), (App)GeodeLayerSettings.CODEC.fieldOf("layers").forGetter(GeodeFeature::layerSettings), (App)GeodeCrackSettings.CODEC.fieldOf("crack").forGetter(GeodeFeature::crackSettings), (App)CHANCE_RANGE.optionalFieldOf("use_potential_placements_chance", (Object)0.35).forGetter(GeodeFeature::usePotentialPlacementsChance), (App)CHANCE_RANGE.optionalFieldOf("use_alternate_layer0_chance", (Object)0.0).forGetter(GeodeFeature::useAlternateLayer0Chance), (App)Codec.BOOL.optionalFieldOf("placements_require_layer0_alternate", (Object)true).forGetter(GeodeFeature::placementsRequireLayer0Alternate), (App)IntProviders.codec(1, 20).optionalFieldOf("outer_wall_distance", (Object)UniformInt.of(4, 5)).forGetter(GeodeFeature::outerWallDistance), (App)IntProviders.codec(1, 20).optionalFieldOf("distribution_points", (Object)UniformInt.of(3, 4)).forGetter(GeodeFeature::distributionPoints), (App)IntProviders.codec(0, 10).optionalFieldOf("point_offset", (Object)UniformInt.of(1, 2)).forGetter(GeodeFeature::pointOffset), (App)Codec.INT.optionalFieldOf("min_gen_offset", (Object)-16).forGetter(GeodeFeature::minGenOffset), (App)Codec.INT.optionalFieldOf("max_gen_offset", (Object)16).forGetter(GeodeFeature::maxGenOffset), (App)CHANCE_RANGE.optionalFieldOf("noise_multiplier", (Object)0.05).forGetter(GeodeFeature::noiseMultiplier), (App)Codec.INT.fieldOf("invalid_blocks_threshold").forGetter(GeodeFeature::invalidBlocksThreshold)).apply((Applicative)i, GeodeFeature::new));
    private static final Direction[] DIRECTIONS = Direction.values();

    public MapCodec<GeodeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        LinkedList points = Lists.newLinkedList();
        int numPoints = this.distributionPoints.sample(random);
        Noise noise = NOISE_PARAMETERS.create(new WorldgenRandom(new LegacyRandomSource(level.getSeed())));
        LinkedList crackPoints = Lists.newLinkedList();
        double crackSizeAdjustment = (double)numPoints / (double)this.outerWallDistance.maxInclusive();
        double innerAir = 1.0 / Math.sqrt(this.layerSettings.filling);
        double innermostBlockLayer = 1.0 / Math.sqrt(this.layerSettings.innerLayer + crackSizeAdjustment);
        double innerCrust = 1.0 / Math.sqrt(this.layerSettings.middleLayer + crackSizeAdjustment);
        double outerCrust = 1.0 / Math.sqrt(this.layerSettings.outerLayer + crackSizeAdjustment);
        double crackSize = 1.0 / Math.sqrt(this.crackSettings.baseCrackSize + random.nextDouble() / 2.0 + (numPoints > 3 ? crackSizeAdjustment : 0.0));
        boolean shouldGenerateCrack = (double)random.nextFloat() < this.crackSettings.generateCrackChance;
        int numInvalidPoints = 0;
        for (int i = 0; i < numPoints; ++i) {
            int z;
            int y;
            int x = this.outerWallDistance.sample(random);
            BlockPos pos = origin.offset(x, y = this.outerWallDistance.sample(random), z = this.outerWallDistance.sample(random));
            BlockState state = level.getBlockState(pos);
            if ((state.isAir() || state.is(this.blockSettings.invalidBlocks())) && ++numInvalidPoints > this.invalidBlocksThreshold) {
                return false;
            }
            points.add(Pair.of((Object)pos, (Object)this.pointOffset.sample(random)));
        }
        if (shouldGenerateCrack) {
            int offsetIndex = random.nextInt(4);
            int crackOffset = numPoints * 2 + 1;
            if (offsetIndex == 0) {
                crackPoints.add(origin.offset(crackOffset, 7, 0));
                crackPoints.add(origin.offset(crackOffset, 5, 0));
                crackPoints.add(origin.offset(crackOffset, 1, 0));
            } else if (offsetIndex == 1) {
                crackPoints.add(origin.offset(0, 7, crackOffset));
                crackPoints.add(origin.offset(0, 5, crackOffset));
                crackPoints.add(origin.offset(0, 1, crackOffset));
            } else if (offsetIndex == 2) {
                crackPoints.add(origin.offset(crackOffset, 7, crackOffset));
                crackPoints.add(origin.offset(crackOffset, 5, crackOffset));
                crackPoints.add(origin.offset(crackOffset, 1, crackOffset));
            } else {
                crackPoints.add(origin.offset(0, 7, 0));
                crackPoints.add(origin.offset(0, 5, 0));
                crackPoints.add(origin.offset(0, 1, 0));
            }
        }
        ArrayList potentialCrystalPlacements = Lists.newArrayList();
        HolderSet<Block> cantReplace = this.blockSettings.cannotReplace();
        Predicate<BlockState> canReplace = s -> !s.is(cantReplace);
        for (BlockPos pointInside : BlockPos.betweenClosed(origin.offset(this.minGenOffset, this.minGenOffset, this.minGenOffset), origin.offset(this.maxGenOffset, this.maxGenOffset, this.maxGenOffset))) {
            double noiseOffset = (double)noise.get(pointInside.getX(), pointInside.getY(), pointInside.getZ()) * this.noiseMultiplier;
            double distSumShell = 0.0;
            for (Pair point : points) {
                distSumShell += Mth.invSqrt(pointInside.distSqr((Vec3i)point.getFirst()) + (double)((Integer)point.getSecond()).intValue()) + noiseOffset;
            }
            if (distSumShell < outerCrust) continue;
            if (distSumShell >= innerAir) {
                this.safeSetBlock(level, pointInside, this.blockSettings.fillingProvider().value().getState(level, random, pointInside), canReplace);
                continue;
            }
            double distSumCrack = 0.0;
            for (BlockPos point : crackPoints) {
                distSumCrack += Mth.invSqrt(pointInside.distSqr(point) + (double)this.crackSettings.crackPointOffset) + noiseOffset;
            }
            if (shouldGenerateCrack && distSumCrack >= crackSize) {
                this.safeSetBlock(level, pointInside, Blocks.AIR.defaultBlockState(), canReplace);
                for (Direction direction : DIRECTIONS) {
                    BlockPos adjacentPos = pointInside.relative(direction);
                    FluidState adjacentFluidState = level.getFluidState(adjacentPos);
                    if (adjacentFluidState.isEmpty()) continue;
                    level.scheduleTick(adjacentPos, adjacentFluidState.getType(), 0);
                }
                continue;
            }
            if (distSumShell >= innermostBlockLayer) {
                boolean useAlternateLayer;
                boolean bl = useAlternateLayer = (double)random.nextFloat() < this.useAlternateLayer0Chance;
                if (useAlternateLayer) {
                    this.safeSetBlock(level, pointInside, this.blockSettings.alternateInnerLayerProvider().value().getState(level, random, pointInside), canReplace);
                } else {
                    this.safeSetBlock(level, pointInside, this.blockSettings.innerLayerProvider().value().getState(level, random, pointInside), canReplace);
                }
                if (this.placementsRequireLayer0Alternate && !useAlternateLayer || !((double)random.nextFloat() < this.usePotentialPlacementsChance)) continue;
                potentialCrystalPlacements.add(pointInside.immutable());
                continue;
            }
            if (distSumShell >= innerCrust) {
                this.safeSetBlock(level, pointInside, this.blockSettings.middleLayerProvider().value().getState(level, random, pointInside), canReplace);
                continue;
            }
            if (!(distSumShell >= outerCrust)) continue;
            this.safeSetBlock(level, pointInside, this.blockSettings.outerLayerProvider().value().getState(level, random, pointInside), canReplace);
        }
        List<BlockState> innerPlacements = this.blockSettings.innerPlacements();
        block5: for (BlockPos crystalPos : potentialCrystalPlacements) {
            BlockState blockState = Util.getRandom(innerPlacements, random);
            for (Direction direction : DIRECTIONS) {
                if (blockState.hasProperty(BlockStateProperties.FACING)) {
                    blockState = (BlockState)blockState.setValue(BlockStateProperties.FACING, direction);
                }
                BlockPos placePos = crystalPos.relative(direction);
                BlockState placeState = level.getBlockState(placePos);
                if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    blockState = (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, placeState.getFluidState().isSource());
                }
                if (!BuddingAmethystBlock.canClusterGrowAtState(placeState)) continue;
                this.safeSetBlock(level, placePos, blockState, canReplace);
                continue block5;
            }
        }
        return true;
    }
}

