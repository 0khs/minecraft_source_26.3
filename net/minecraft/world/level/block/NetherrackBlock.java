/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Continuation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NetherrackBlock
extends Block
implements BonemealableBlock {
    public NetherrackBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        if (!level.getBlockState(pos.above()).propagatesSkylightDown()) {
            return false;
        }
        return level.findBlocksIn(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)).filterState(blockState -> blockState.is(BlockTags.NYLIUM)).anyMatched();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        MutableBoolean foundRed = new MutableBoolean();
        MutableBoolean foundBlue = new MutableBoolean();
        level.findBlocksIn(pos.offset(-1, -1, -1), pos.offset(1, 1, 1)).filterState(blockState -> blockState.is(BlockTags.NYLIUM)).forEachUntil((blockPos, blockState) -> {
            if (blockState.is(Blocks.WARPED_NYLIUM)) {
                foundBlue.setTrue();
            } else if (blockState.is(Blocks.CRIMSON_NYLIUM)) {
                foundRed.setTrue();
            }
            return Continuation.abortIf(foundBlue.isTrue() && foundRed.isTrue());
        });
        if (foundBlue.isTrue() && foundRed.isTrue()) {
            level.setBlockAndUpdate(pos, random.nextBoolean() ? Blocks.WARPED_NYLIUM.defaultBlockState() : Blocks.CRIMSON_NYLIUM.defaultBlockState());
        } else if (foundBlue.isTrue()) {
            level.setBlockAndUpdate(pos, Blocks.WARPED_NYLIUM.defaultBlockState());
        } else if (foundRed.isTrue()) {
            level.setBlockAndUpdate(pos, Blocks.CRIMSON_NYLIUM.defaultBlockState());
        }
    }

    @Override
    public BonemealableBlock.Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }
}

