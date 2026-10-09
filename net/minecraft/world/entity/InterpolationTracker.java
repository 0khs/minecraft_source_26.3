/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.phys.Vec3;

public interface InterpolationTracker {
    public static final InterpolationTracker NO_OP = new NoOpInterpolationTracker();

    public void updateTracking(Vec3 var1);

    public PositionPath getPositionPath(Vec3 var1);

    public void clear();

    public record NoOpInterpolationTracker() implements InterpolationTracker
    {
        @Override
        public void updateTracking(Vec3 trackingPos) {
        }

        @Override
        public PositionPath getPositionPath(Vec3 trackingPos) {
            return PositionPath.of(trackingPos);
        }

        @Override
        public void clear() {
        }
    }
}

