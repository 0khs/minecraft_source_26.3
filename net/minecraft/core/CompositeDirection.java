/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 */
package net.minecraft.core;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.Directional;
import net.minecraft.core.Vec3i;

public final class CompositeDirection
extends Record
implements Directional {
    private final Set<Direction> directions;
    private final Vec3i step;
    public static final CompositeDirection NORTH = new CompositeDirection(Direction.NORTH);
    public static final CompositeDirection EAST = new CompositeDirection(Direction.EAST);
    public static final CompositeDirection SOUTH = new CompositeDirection(Direction.SOUTH);
    public static final CompositeDirection WEST = new CompositeDirection(Direction.WEST);
    public static final CompositeDirection NORTH_EAST = NORTH.compose(EAST);
    public static final CompositeDirection SOUTH_EAST = SOUTH.compose(EAST);
    public static final CompositeDirection SOUTH_WEST = SOUTH.compose(WEST);
    public static final CompositeDirection NORTH_WEST = NORTH.compose(WEST);
    public static final CompositeDirection UP = new CompositeDirection(Direction.UP);
    public static final CompositeDirection NORTH_UP = NORTH.compose(UP);
    public static final CompositeDirection EAST_UP = EAST.compose(UP);
    public static final CompositeDirection SOUTH_UP = SOUTH.compose(UP);
    public static final CompositeDirection WEST_UP = WEST.compose(UP);
    public static final CompositeDirection NORTH_EAST_UP = NORTH_EAST.compose(UP);
    public static final CompositeDirection SOUTH_EAST_UP = SOUTH_EAST.compose(UP);
    public static final CompositeDirection SOUTH_WEST_UP = SOUTH_WEST.compose(UP);
    public static final CompositeDirection NORTH_WEST_UP = NORTH_WEST.compose(UP);
    public static final CompositeDirection DOWN = new CompositeDirection(Direction.DOWN);
    public static final CompositeDirection NORTH_DOWN = NORTH.compose(DOWN);
    public static final CompositeDirection EAST_DOWN = EAST.compose(DOWN);
    public static final CompositeDirection SOUTH_DOWN = SOUTH.compose(DOWN);
    public static final CompositeDirection WEST_DOWN = WEST.compose(DOWN);
    public static final CompositeDirection NORTH_EAST_DOWN = NORTH_EAST.compose(DOWN);
    public static final CompositeDirection SOUTH_EAST_DOWN = SOUTH_EAST.compose(DOWN);
    public static final CompositeDirection SOUTH_WEST_DOWN = SOUTH_WEST.compose(DOWN);
    public static final CompositeDirection NORTH_WEST_DOWN = NORTH_WEST.compose(DOWN);

    public CompositeDirection(Set<Direction> directions, Vec3i step) {
        if (directions.isEmpty()) {
            throw new IllegalArgumentException("Directions cannot be empty");
        }
        this.directions = directions;
        this.step = step;
    }

    @VisibleForTesting
    CompositeDirection(Direction ... directions) {
        ImmutableSet immutableDirections = Sets.immutableEnumSet(Arrays.asList(directions));
        int x = 0;
        int y = 0;
        int z = 0;
        for (Direction direction : immutableDirections) {
            x += direction.getStepX();
            y += direction.getStepY();
            z += direction.getStepZ();
        }
        this((Set<Direction>)immutableDirections, new Vec3i(x, y, z));
    }

    public CompositeDirection compose(CompositeDirection other) {
        EnumSet<Direction> newDirections = EnumSet.copyOf(this.directions);
        newDirections.addAll(other.directions);
        Vec3i step = new Vec3i(this.getStepX(), this.getStepY(), this.getStepZ());
        Vec3i additionalStep = other.getStep();
        step.setX(step.getX() + additionalStep.getX()).setY(step.getY() + additionalStep.getY()).setZ(step.getZ() + additionalStep.getZ());
        return new CompositeDirection((Set<Direction>)Sets.immutableEnumSet(newDirections), step);
    }

    @Override
    public int getStepX() {
        return this.step.getX();
    }

    @Override
    public int getStepY() {
        return this.step.getY();
    }

    @Override
    public int getStepZ() {
        return this.step.getZ();
    }

    @Override
    public Vec3i getStep() {
        return this.step;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{CompositeDirection.class, "directions;step", "directions", "step"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CompositeDirection.class, "directions;step", "directions", "step"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CompositeDirection.class, "directions;step", "directions", "step"}, this, o);
    }

    public Set<Direction> directions() {
        return this.directions;
    }

    public Vec3i step() {
        return this.step;
    }

    public static enum Direction8 implements Directional
    {
        NORTH(NORTH),
        NORTH_EAST(NORTH_EAST),
        EAST(EAST),
        SOUTH_EAST(SOUTH_EAST),
        SOUTH(SOUTH),
        SOUTH_WEST(SOUTH_WEST),
        WEST(WEST),
        NORTH_WEST(NORTH_WEST);

        private final CompositeDirection compositeDirection;

        private Direction8(CompositeDirection compositeDirection) {
            this.compositeDirection = compositeDirection;
        }

        public Set<Direction> getDirections() {
            return this.compositeDirection.directions();
        }

        @Override
        public int getStepX() {
            return this.compositeDirection.getStepX();
        }

        @Override
        public int getStepY() {
            return this.compositeDirection.getStepY();
        }

        @Override
        public int getStepZ() {
            return this.compositeDirection.getStepZ();
        }

        @Override
        public Vec3i getStep() {
            return this.compositeDirection.getStep();
        }
    }
}

