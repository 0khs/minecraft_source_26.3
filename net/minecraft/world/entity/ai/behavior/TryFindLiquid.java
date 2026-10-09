/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.apache.commons.lang3.mutable.MutableLong
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Continuation;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.blockscan.OrderedBlockMatcher;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class TryFindLiquid {
    public static BehaviorControl<PathfinderMob> create(int range, float speedModifier, TagKey<Fluid> fluidTag) {
        MutableLong nextOkStartTime = new MutableLong(0L);
        return BehaviorBuilder.create(i -> i.group(i.absent(MemoryModuleType.ATTACK_TARGET), i.absent(MemoryModuleType.WALK_TARGET), i.registered(MemoryModuleType.LOOK_TARGET)).apply((Applicative)i, (memoryAccessor, walkTarget, lookTarget) -> (level, body, timestamp) -> {
            if (level.getFluidState(body.blockPosition()).is(fluidTag)) {
                return false;
            }
            if (timestamp < nextOkStartTime.longValue()) {
                nextOkStartTime.setValue(timestamp + 20L + 2L);
                return true;
            }
            @Nullable MutableObject foundPos = new MutableObject(null);
            BlockPos bodyBlockPos = body.blockPosition();
            ((OrderedBlockMatcher)level.findBlocksInBoxByManhattanDistance(bodyBlockPos, range).filterPos(pos -> pos.differsHorizontally(bodyBlockPos)).filterState(state -> state.getBlock() instanceof LiquidBlock && state.getFluidState().is(fluidTag))).forEachUntil((pos, blockState) -> {
                BlockState aboveState = level.getBlockState(pos.above());
                if (aboveState.isAir()) {
                    foundPos.setValue((Object)pos.immutable());
                    return Continuation.ABORT;
                }
                if (foundPos.get() == null && !pos.closerToCenterThan(body.position(), 1.5)) {
                    foundPos.setValue((Object)pos.immutable());
                }
                return Continuation.CONTINUE;
            });
            BlockPos blockPos = (BlockPos)foundPos.get();
            if (blockPos != null) {
                lookTarget.set(new BlockPosTracker(blockPos));
                walkTarget.set(new WalkTarget(new BlockPosTracker(blockPos), speedModifier, 0));
            }
            nextOkStartTime.setValue(timestamp + 40L);
            return true;
        }));
    }
}

