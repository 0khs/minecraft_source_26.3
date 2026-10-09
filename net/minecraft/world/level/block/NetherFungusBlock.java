/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherFungusBlock
extends VegetationBlock
implements BonemealableBlock {
    private static final double BONEMEAL_SUCCESS_PROBABILITY = 0.4;
    private static final VoxelShape SHAPE = Block.column(8.0, 0.0, 9.0);
    private final Block requiredBlock;
    private final ResourceKey<Feature> feature;
    private final TagKey<Block> supportBlocks;

    protected NetherFungusBlock(ResourceKey<Feature> feature, Block requiredBlock, TagKey<Block> supportBlocks, BlockBehaviour.Properties properties) {
        super(properties);
        this.feature = feature;
        this.requiredBlock = requiredBlock;
        this.supportBlocks = supportBlocks;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(this.supportBlocks);
    }

    private Optional<? extends Holder<Feature>> getFeature(LevelReader level) {
        return level.registryAccess().lookupOrThrow(Registries.FEATURE).get(this.feature);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        BlockState belowState = level.getBlockState(pos.below());
        return belowState.is(this.requiredBlock) && level.isInsideBuildHeight(pos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return (double)random.nextFloat() < 0.4;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        this.getFeature(level).ifPresent(feature -> ((Feature)feature.value()).place(level, level.getChunkSource().getGenerator(), random, pos));
    }
}

