/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import net.minecraft.core.PositionAndRotation;
import net.minecraft.world.entity.InterpolationTracker;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface InterpolationHandler {
    public static final InterpolationHandler NO_OP = new NoOpInterpolationHandler();

    default public InterpolationTracker interpolationTracker() {
        return InterpolationTracker.NO_OP;
    }

    public @Nullable PositionAndRotation target();

    public boolean interpolateTo(@Nullable PositionPath var1, float var2, float var3, boolean var4);

    public void interpolate();

    public void applyPredictedMovement(Vec3 var1);

    public boolean hasActiveInterpolation();

    public void cancel();

    public static class NoOpInterpolationHandler
    implements InterpolationHandler {
        @Override
        public @Nullable PositionAndRotation target() {
            return null;
        }

        @Override
        public boolean interpolateTo(@Nullable PositionPath position, float yRot, float xRot, boolean hasRotation) {
            return false;
        }

        @Override
        public void interpolate() {
        }

        @Override
        public void applyPredictedMovement(Vec3 delta) {
        }

        @Override
        public boolean hasActiveInterpolation() {
            return false;
        }

        @Override
        public void cancel() {
        }
    }
}

