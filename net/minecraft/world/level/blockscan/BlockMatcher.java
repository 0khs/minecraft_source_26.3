/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.blockscan;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.blockscan.BlockStateConsumer;

public abstract class BlockMatcher {
    private static final Predicate<BlockState> NO_PREDICATE = blockState -> true;
    protected final LevelReader level;
    protected Predicate<BlockState> statePredicate = NO_PREDICATE;

    public BlockMatcher(LevelReader levelReader) {
        this.level = levelReader;
    }

    public BlockMatcher filterState(Predicate<BlockState> predicate) {
        this.statePredicate = this.statePredicate == NO_PREDICATE ? predicate : this.statePredicate.and(predicate);
        return this;
    }

    public abstract boolean atLeastMatched(int var1);

    public boolean atMostMatched(int n) {
        return !this.atLeastMatched(n + 1);
    }

    public boolean anyMatched() {
        return this.anyMatched(this.statePredicate);
    }

    protected abstract boolean anyMatched(Predicate<BlockState> var1);

    public boolean noneMatched() {
        return !this.anyMatched();
    }

    public boolean allMatched() {
        return !this.anyMatched(state -> !this.statePredicate.test((BlockState)state));
    }

    public abstract void forEach(BiConsumer<BlockPos, BlockState> var1);

    public abstract boolean forEachUntil(BlockStateConsumer var1);
}

