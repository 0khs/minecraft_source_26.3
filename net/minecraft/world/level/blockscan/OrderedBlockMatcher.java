/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.world.level.blockscan;

import com.google.common.collect.Iterables;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Continuation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.blockscan.BlockMatcher;
import net.minecraft.world.level.blockscan.BlockScanUtils;
import net.minecraft.world.level.blockscan.BlockStateConsumer;
import org.apache.commons.lang3.mutable.MutableObject;

public class OrderedBlockMatcher
extends BlockMatcher {
    private Iterable<BlockPos> positions;

    public OrderedBlockMatcher(LevelReader level, Iterable<BlockPos> positions) {
        super(level);
        this.positions = positions;
    }

    public OrderedBlockMatcher filterPos(Predicate<? super BlockPos> predicate) {
        this.positions = Iterables.filter(this.positions, predicate::test);
        return this;
    }

    @Override
    public OrderedBlockMatcher filterState(Predicate<BlockState> predicate) {
        super.filterState(predicate);
        return this;
    }

    @Override
    public boolean atLeastMatched(final int n) {
        return BlockScanUtils.findBlocksWithCache(this.level, this.positions, this.statePredicate, new BlockStateConsumer(){
            private int count;
            {
                Objects.requireNonNull(this$0);
                this.count = 0;
            }

            @Override
            public Continuation apply(BlockPos pos, BlockState state) {
                return Continuation.abortIf(++this.count >= n);
            }
        });
    }

    @Override
    protected boolean anyMatched(Predicate<BlockState> statePredicate) {
        return BlockScanUtils.findBlocksWithCache(this.level, this.positions, statePredicate, (blockPos, blockState) -> Continuation.ABORT);
    }

    @Override
    public void forEach(BiConsumer<BlockPos, BlockState> consumer) {
        BlockScanUtils.findBlocksWithCache(this.level, this.positions, this.statePredicate, (pos, state) -> {
            consumer.accept(pos, state);
            return Continuation.CONTINUE;
        });
    }

    @Override
    public boolean forEachUntil(BlockStateConsumer consumer) {
        return BlockScanUtils.findBlocksWithCache(this.level, this.positions, this.statePredicate, consumer);
    }

    public Optional<BlockPos> findFirst() {
        MutableObject result = new MutableObject(Optional.empty());
        BlockScanUtils.findBlocksWithCache(this.level, this.positions, this.statePredicate, (pos, blockState) -> {
            result.setValue(Optional.of(pos.immutable()));
            return Continuation.ABORT;
        });
        return (Optional)result.get();
    }

    public Optional<BlockPos> findFirst(BiPredicate<BlockPos, BlockState> predicate) {
        MutableObject result = new MutableObject(Optional.empty());
        BlockScanUtils.findBlocksWithCache(this.level, this.positions, this.statePredicate, (pos, state) -> {
            if (predicate.test(pos, state)) {
                result.setValue(Optional.of(pos.immutable()));
                return Continuation.ABORT;
            }
            return Continuation.CONTINUE;
        });
        return (Optional)result.get();
    }
}

