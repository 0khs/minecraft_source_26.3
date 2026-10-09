/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.blockscan.BlockMatcher;
import net.minecraft.world.level.blockscan.OrderedBlockMatcher;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AnimalPanic<E extends PathfinderMob>
extends Behavior<E> {
    private static final int PANIC_MIN_DURATION = 100;
    private static final int PANIC_MAX_DURATION = 120;
    private static final int PANIC_DISTANCE_HORIZONTAL = 5;
    private static final int PANIC_DISTANCE_VERTICAL = 4;
    private final float speedMultiplier;
    private final Function<PathfinderMob, TagKey<DamageType>> panicCausingDamageTypes;
    private final Function<E, Vec3> positionGetter;

    public AnimalPanic(float speedMultiplier) {
        this(speedMultiplier, pathfinderMob -> DamageTypeTags.PANIC_CAUSES, mob -> LandRandomPos.getPos(mob, 5, 4));
    }

    public AnimalPanic(float speedMultiplier, int flyHeight) {
        this(speedMultiplier, pathfinderMob -> DamageTypeTags.PANIC_CAUSES, mob -> AirAndWaterRandomPos.getPos(mob, 5, 4, flyHeight, mob.getViewVector((float)0.0f).x, mob.getViewVector((float)0.0f).z, 1.5707963705062866));
    }

    public AnimalPanic(float speedMultiplier, Function<PathfinderMob, TagKey<DamageType>> panicCausingDamageTypes) {
        this(speedMultiplier, panicCausingDamageTypes, mob -> LandRandomPos.getPos(mob, 5, 4));
    }

    public AnimalPanic(float speedMultiplier, Function<PathfinderMob, TagKey<DamageType>> panicCausingDamageTypes, Function<E, Vec3> positionGetter) {
        super(Map.of(MemoryModuleType.IS_PANICKING, MemoryStatus.REGISTERED, MemoryModuleType.HURT_BY, MemoryStatus.REGISTERED), 100, 120);
        this.speedMultiplier = speedMultiplier;
        this.panicCausingDamageTypes = panicCausingDamageTypes;
        this.positionGetter = positionGetter;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E body) {
        return ((LivingEntity)body).getBrain().getMemory(MemoryModuleType.HURT_BY).map(d -> d.is(this.panicCausingDamageTypes.apply((PathfinderMob)body))).orElse(false) != false || ((LivingEntity)body).getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E body, long timestamp) {
        return true;
    }

    @Override
    protected void start(ServerLevel level, E body, long timestamp) {
        ((LivingEntity)body).getBrain().setMemory(MemoryModuleType.IS_PANICKING, true);
        ((LivingEntity)body).getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        ((Mob)body).getNavigation().stop();
    }

    @Override
    protected void stop(ServerLevel level, E body, long timestamp) {
        Brain<? extends LivingEntity> brain = ((LivingEntity)body).getBrain();
        brain.eraseMemory(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected void tick(ServerLevel level, E body, long timestamp) {
        Vec3 panicToPos;
        if (((Mob)body).getNavigation().isDone() && (panicToPos = this.getPanicPos(body, level)) != null) {
            ((LivingEntity)body).getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(panicToPos, this.speedMultiplier, 0));
        }
    }

    private @Nullable Vec3 getPanicPos(E body, ServerLevel level) {
        Optional<Vec3> nearestWater;
        if (((Entity)body).isOnFire() && (nearestWater = this.lookForWater(level, (Entity)body).map(Vec3::atBottomCenterOf)).isPresent()) {
            return nearestWater.get();
        }
        return this.positionGetter.apply(body);
    }

    private Optional<BlockPos> lookForWater(LevelReader level, Entity mob) {
        BlockPos mobPosition = mob.blockPosition();
        if (!level.getBlockState(mobPosition).getCollisionShape(level, mobPosition).isEmpty()) {
            return Optional.empty();
        }
        BlockMatcher blockMatcher = level.findBlocksInBoxByManhattanDistance(mobPosition, 5, 1).filterState(AnimalPanic::isWater);
        if (Mth.ceil(mob.getBbWidth()) == 2) {
            return ((OrderedBlockMatcher)blockMatcher).findFirst((pos, blockState) -> AnimalPanic.isWater(level.getBlockState(pos.south())) && AnimalPanic.isWater(level.getBlockState(pos.east())) && AnimalPanic.isWater(level.getBlockState(pos.south().east())));
        }
        return ((OrderedBlockMatcher)blockMatcher).findFirst();
    }

    private static boolean isWater(BlockState state) {
        return state.getFluidState().is(FluidTags.WATER);
    }
}

