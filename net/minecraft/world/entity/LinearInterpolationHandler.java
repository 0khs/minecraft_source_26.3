/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.core.PositionAndRotation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AbstractInterpolationHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.phys.Vec3;

public class LinearInterpolationHandler
extends AbstractInterpolationHandler {
    public static final int DEFAULT_INTERPOLATION_STEPS = 3;
    private final InterpolationData interpolationData = new InterpolationData();

    private LinearInterpolationHandler(Entity entity, int interpolationSteps) {
        super(entity, interpolationSteps);
    }

    public static InterpolationHandler create(Entity entity, int interpolationSteps) {
        if (entity.level().isClientSide()) {
            return new LinearInterpolationHandler(entity, interpolationSteps);
        }
        return InterpolationHandler.NO_OP;
    }

    public static InterpolationHandler create(Entity entity) {
        return LinearInterpolationHandler.create(entity, 3);
    }

    public void setInterpolationLength(int steps) {
        this.interpolationSteps = steps;
    }

    @Override
    protected PositionAndRotation.Mutable interpolationData() {
        return this.interpolationData;
    }

    @Override
    protected void startInterpolating(PositionPath position, float yRot, float xRot) {
        this.interpolationData.set(position.endPosition(), yRot, xRot);
        this.interpolationData.remainingSteps = this.interpolationSteps;
    }

    @Override
    protected void doInterpolate() {
        double alpha = 1.0 / (double)this.interpolationData.remainingSteps;
        Vec3 newPosition = this.entity.position().lerp(this.interpolationData.position(), alpha);
        float newYRot = (float)Mth.rotLerp(alpha, (double)this.entity.getYRot(), (double)this.interpolationData.yRot());
        float newXRot = (float)Mth.lerp(alpha, (double)this.entity.getXRot(), (double)this.interpolationData.xRot());
        this.entity.setPos(newPosition);
        this.entity.setRot(newYRot, newXRot);
        --this.interpolationData.remainingSteps;
    }

    @Override
    public boolean hasActiveInterpolation() {
        return this.interpolationData.remainingSteps > 0;
    }

    @Override
    public void cancel() {
        this.interpolationData.remainingSteps = 0;
    }

    private static class InterpolationData
    extends PositionAndRotation.Mutable {
        private int remainingSteps;

        private InterpolationData() {
        }
    }
}

