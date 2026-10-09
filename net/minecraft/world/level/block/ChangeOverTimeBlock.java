/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Continuation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.blockscan.OrderedBlockMatcher;
import org.apache.commons.lang3.mutable.MutableInt;

public interface ChangeOverTimeBlock<T extends Enum<T>> {
    public static final int SCAN_DISTANCE = 4;

    public Optional<BlockState> getNext(BlockState var1);

    public float getChanceModifier();

    default public void changeOverTime(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        float eachBlockOncePerDayChance = 0.05688889f;
        if (random.nextFloat() < 0.05688889f) {
            this.getNextState(state, level, pos, random).ifPresent(weatheredState -> level.setBlockAndUpdate(pos, (BlockState)weatheredState));
        }
    }

    public T getAge();

    default public Optional<BlockState> getNextState(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int ownAge = ((Enum)this.getAge()).ordinal();
        MutableInt sameAgeCount = new MutableInt(0);
        MutableInt olderCount = new MutableInt(0);
        boolean aborted = ((OrderedBlockMatcher)level.findBlocksInManhattan(pos, 4).filterPos(blockPos -> !blockPos.equals(pos)).filterState(this::isSameAgeingType)).forEachUntil((blockPos, blockState) -> {
            Block patt0$temp = level.getBlockState(blockPos).getBlock();
            if (patt0$temp instanceof ChangeOverTimeBlock) {
                ChangeOverTimeBlock neighborBlock = (ChangeOverTimeBlock)((Object)patt0$temp);
                int foundAge = ((Enum)neighborBlock.getAge()).ordinal();
                if (foundAge < ownAge) {
                    return Continuation.ABORT;
                }
                if (foundAge > ownAge) {
                    olderCount.increment();
                } else {
                    sameAgeCount.increment();
                }
            }
            return Continuation.CONTINUE;
        });
        if (aborted) {
            return Optional.empty();
        }
        float chance = (float)(olderCount.intValue() + 1) / (float)(olderCount.intValue() + sameAgeCount.intValue() + 1);
        float actualChance = chance * chance * this.getChanceModifier();
        if (random.nextFloat() < actualChance) {
            return this.getNext(state);
        }
        return Optional.empty();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean isSameAgeingType(BlockState state) {
        Block block = state.getBlock();
        if (!(block instanceof ChangeOverTimeBlock)) return false;
        ChangeOverTimeBlock neighborBlock = (ChangeOverTimeBlock)((Object)block);
        if (this.getAge().getClass() != neighborBlock.getAge().getClass()) return false;
        return true;
    }
}

