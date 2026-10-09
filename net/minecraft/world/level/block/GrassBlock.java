/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.references.BlockItemIds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class GrassBlock
extends SpreadingSnowyBlock
implements BonemealableBlock {
    private static final int ATTEMPT_COUNT = 128;
    private static final float GROW_TALL_GRASS_CHANCE = 0.1f;
    private static final float PLACE_FLOWER_CHANCE = 0.125f;

    public GrassBlock(BlockBehaviour.Properties properties) {
        super(properties, BlockItemIds.DIRT.block());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return level.getBlockState(pos.above()).isAir() && level.isInsideBuildHeight(pos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        BlockPos above = pos.above();
        block0: for (int attempt = 0; attempt < 128; ++attempt) {
            BlockPos testPos = above;
            int randomizeCount = attempt / 16;
            for (int i = 0; i < randomizeCount; ++i) {
                int dz;
                int dy;
                int dx = random.nextIntBetweenInclusive(-1, 1);
                if (this.stopBonemealSpread(level, testPos = testPos.offset(dx, dy = random.nextIntBetweenInclusive(-1, 1) * random.nextInt(3) / 2, dz = random.nextIntBetweenInclusive(-1, 1)))) continue block0;
            }
            GrassBlock.placeBonemealEffect(level, random, testPos, source);
        }
    }

    private static void placeBonemealEffect(ServerLevel level, RandomSource random, BlockPos testPos, BonemealSource source) {
        BonemealableBlock bonemealableBlock;
        BlockState grass = Blocks.SHORT_GRASS.defaultBlockState();
        Optional grassFeature = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).get(VegetationPlacements.GRASS_BONEMEAL);
        BlockState testState = level.getBlockState(testPos);
        if (testState.is(grass.getBlock()) && random.nextFloat() < 0.1f && (bonemealableBlock = (BonemealableBlock)((Object)grass.getBlock())).isValidBonemealTarget(level, testPos, testState, source)) {
            bonemealableBlock.performBonemeal(level, random, testPos, testState, source);
        }
        if (!testState.isAir() || level.isOutsideBuildHeight(testPos)) {
            return;
        }
        if (random.nextFloat() < 0.125f) {
            List<Feature> features = level.getBiome(testPos).value().getGenerationSettings().getBoneMealFeatures();
            if (features.isEmpty()) {
                return;
            }
            Feature placementFeature = Util.getRandom(features, random);
            placementFeature.place(level, level.getChunkSource().getGenerator(), random, testPos);
        } else if (grassFeature.isPresent()) {
            ((PlacedFeature)((Holder.Reference)grassFeature.get()).value()).place(level, level.getChunkSource().getGenerator(), random, testPos);
        }
    }

    private boolean stopBonemealSpread(ServerLevel level, BlockPos testPos) {
        return !level.getBlockState(testPos.below()).is(this) || level.getBlockState(testPos).isCollisionShapeFullBlock(level, testPos);
    }

    @Override
    public BonemealableBlock.Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }
}

