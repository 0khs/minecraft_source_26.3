/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.apache.commons.lang3.mutable.MutableLong
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLandNearLiquid {
    public static BehaviorControl<PathfinderMob> create(int range, float speedModifier, TagKey<Fluid> fluidTag) {
        MutableLong nextOkStartTime = new MutableLong(0L);
        return BehaviorBuilder.create(i -> i.group(i.absent(MemoryModuleType.ATTACK_TARGET), i.absent(MemoryModuleType.WALK_TARGET), i.registered(MemoryModuleType.LOOK_TARGET)).apply((Applicative)i, (memoryAccessor, walkTarget, lookTarget) -> (level, body, timestamp) -> {
            if (level.getFluidState(body.blockPosition()).is(fluidTag)) {
                return false;
            }
            if (timestamp < nextOkStartTime.longValue()) {
                nextOkStartTime.setValue(timestamp + 40L);
                return true;
            }
            CollisionContext context = CollisionContext.of(body);
            BlockPos bodyBlockPos = body.blockPosition();
            BlockPos.MutableBlockPos testPos = new BlockPos.MutableBlockPos();
            level.findBlocksInBoxByManhattanDistance(bodyBlockPos, range).filterPos(pos -> pos.differsHorizontally(bodyBlockPos)).findFirst((pos, state) -> {
                if (!state.getCollisionShape(level, (BlockPos)pos, context).isEmpty()) {
                    return false;
                }
                if (level.getBlockState(testPos.setWithOffset((Vec3i)pos, Direction.DOWN)).getCollisionShape(level, (BlockPos)pos, context).isEmpty()) {
                    return false;
                }
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    testPos.setWithOffset((Vec3i)pos, direction);
                    if (!level.getBlockState(testPos).isAir() || !level.getBlockState(testPos.move(Direction.DOWN)).getFluidState().is(fluidTag)) continue;
                    return true;
                }
                return false;
            }).ifPresent(pos -> {
                BlockPos targetPos = pos.immutable();
                lookTarget.set(new BlockPosTracker(targetPos));
                walkTarget.set(new WalkTarget(new BlockPosTracker(targetPos), speedModifier, 0));
            });
            nextOkStartTime.setValue(timestamp + 40L);
            return true;
        }));
    }
}

