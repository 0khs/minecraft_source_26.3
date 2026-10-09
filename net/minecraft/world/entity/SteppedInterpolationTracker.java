/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.InterpolationTracker;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.PositionStep;
import net.minecraft.world.phys.Vec3;

public class SteppedInterpolationTracker
extends InterpolationHandler.NoOpInterpolationHandler
implements InterpolationTracker {
    private final List<PositionStep> trackedSteps = new ArrayList<PositionStep>();
    private int ticksSinceLastStep = -1;
    private Vec3 predictedDelta = Vec3.ZERO;
    private final Entity entity;

    public SteppedInterpolationTracker(Entity entity) {
        this.entity = entity;
    }

    @Override
    public InterpolationTracker interpolationTracker() {
        return this;
    }

    @Override
    public void applyPredictedMovement(Vec3 delta) {
        this.predictedDelta = this.predictedDelta.add(delta);
    }

    private void addStep(Vec3 pos) {
        if (this.ticksSinceLastStep > 0) {
            this.trackedSteps.add(new PositionStep(pos, this.ticksSinceLastStep));
            this.ticksSinceLastStep = 0;
        }
    }

    @Override
    public void updateTracking(Vec3 trackingPos) {
        if (this.predictedDelta.lengthSqr() > (double)1.0E-5f) {
            this.trackedSteps.replaceAll(step -> step.addDelta(this.predictedDelta));
        }
        this.predictedDelta = Vec3.ZERO;
        ++this.ticksSinceLastStep;
        if (this.entity.syncPosition) {
            this.entity.syncPosition = false;
            this.addStep(trackingPos);
        }
    }

    @Override
    public PositionPath getPositionPath(Vec3 currentPosition) {
        this.addStep(currentPosition);
        if (this.trackedSteps.isEmpty()) {
            return PositionPath.of(currentPosition);
        }
        return PositionPath.stepped(List.copyOf(this.trackedSteps));
    }

    @Override
    public void clear() {
        this.trackedSteps.clear();
        this.ticksSinceLastStep = 0;
    }
}

