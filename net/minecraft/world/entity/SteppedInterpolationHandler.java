/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.lang.runtime.SwitchBootstraps;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.PositionAndRotation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AbstractInterpolationHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.PositionStep;
import net.minecraft.world.entity.SteppedInterpolationTracker;
import net.minecraft.world.phys.Vec3;

public class SteppedInterpolationHandler
extends AbstractInterpolationHandler {
    private final InterpolationData interpolationData = new InterpolationData();

    private SteppedInterpolationHandler(Entity entity) {
        super(entity, entity.getType().updateInterval());
    }

    public static InterpolationHandler create(Entity entity) {
        if (entity.level().isClientSide()) {
            return new SteppedInterpolationHandler(entity);
        }
        return new SteppedInterpolationTracker(entity);
    }

    @Override
    protected PositionAndRotation.Mutable interpolationData() {
        return this.interpolationData;
    }

    @Override
    protected void startInterpolating(PositionPath position, float yRot, float xRot) {
        Vec3 endPosition;
        if (!this.hasActiveInterpolation()) {
            this.interpolationData.setStartingPoint(this.entity.position(), this.entity.getYRot(), this.entity.getXRot());
        }
        if (Objects.equals(endPosition = position.endPosition(), this.interpolationData.position())) {
            this.interpolationData.addStep(endPosition, yRot, xRot, this.interpolationSteps);
        } else {
            this.interpolationData.addSteps(position, yRot, xRot, this.interpolationSteps);
        }
        this.interpolationData.set(endPosition, yRot, xRot);
    }

    @Override
    protected void doInterpolate() {
        PositionAndRotation target = this.interpolationData.getNewPositionAndRotation();
        this.entity.setPos(target.position());
        this.entity.setRot(target.yRot(), target.xRot());
        float tick = this.entity.level().getRelativeTickSpeed();
        this.interpolationData.advance(tick, this.interpolationSteps);
    }

    @Override
    public boolean hasActiveInterpolation() {
        return !this.interpolationData.remainingSteps.isEmpty();
    }

    @Override
    public void cancel() {
        this.interpolationData.reset();
    }

    private static class InterpolationData
    extends PositionAndRotation.Mutable {
        private final LinkedList<Step> remainingSteps = new LinkedList();
        private final PositionAndRotation.Mutable lastStepPosRot = new PositionAndRotation.Mutable();
        private float currentStepTicks;
        private float remainingTicks;
        private float interpolationSpeed = 1.0f;

        private InterpolationData() {
        }

        private void advance(float ticks, int interpolationSteps) {
            float targetSpeed = Math.max(this.remainingTicks / (float)interpolationSteps, 1.0f);
            this.interpolationSpeed = Mth.lerp(1.0f / (float)interpolationSteps, this.interpolationSpeed, targetSpeed);
            if (ticks * this.interpolationSpeed < this.remainingTicks) {
                ticks *= this.interpolationSpeed;
            } else {
                ticks = this.remainingTicks;
                this.interpolationSpeed = 1.0f;
            }
            this.currentStepTicks += ticks;
            this.remainingTicks -= ticks;
        }

        private void reset() {
            this.remainingSteps.clear();
            this.remainingTicks = 0.0f;
            this.interpolationSpeed = 1.0f;
        }

        @Override
        public void addDelta(Vec3 delta) {
            super.addDelta(delta);
            for (Step step : this.remainingSteps) {
                step.addDelta(delta);
            }
            this.lastStepPosRot.addDelta(delta);
        }

        @Override
        public void addRotation(float yRot, float xRot) {
            super.addRotation(yRot, xRot);
            for (Step step : this.remainingSteps) {
                step.addRotation(yRot, xRot);
            }
            this.lastStepPosRot.addRotation(yRot, xRot);
        }

        private void setStartingPoint(Vec3 position, float yRot, float xRot) {
            this.lastStepPosRot.set(position, yRot, xRot);
            this.currentStepTicks = 1.0f;
        }

        private void addStep(Vec3 position, float yRot, float xRot, int interpolationSteps) {
            this.remainingSteps.add(new Step(position, yRot, xRot, interpolationSteps));
            this.remainingTicks += (float)interpolationSteps;
        }

        private void addSteps(PositionPath position, float yRot, float xRot, int interpolationSteps) {
            PositionPath positionPath = position;
            Objects.requireNonNull(positionPath);
            PositionPath positionPath2 = positionPath;
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PositionPath.Linear.class, PositionPath.Stepped.class}, (PositionPath)positionPath2, n)) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    Vec3 vec3;
                    PositionPath.Linear linear = (PositionPath.Linear)positionPath2;
                    Vec3 pos = vec3 = linear.endPosition();
                    this.addStep(pos, yRot, xRot, interpolationSteps);
                    break;
                }
                case 1: {
                    Iterator steps;
                    PositionPath.Stepped stepped = (PositionPath.Stepped)positionPath2;
                    Iterator iterator = stepped.endPosition();
                    try {
                        steps = iterator = stepped.steps();
                    }
                    catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }
                    if (yRot == this.yRot() && xRot == this.xRot()) {
                        iterator = steps.iterator();
                        while (iterator.hasNext()) {
                            PositionStep step = (PositionStep)iterator.next();
                            this.addStep(step.position(), yRot, xRot, step.tickOffset());
                        }
                        return;
                    }
                    int totalInterpolationTicks = InterpolationData.getInterpolationTicks((List<PositionStep>)((Object)steps));
                    int offset = 0;
                    Iterator iterator2 = steps.iterator();
                    while (iterator2.hasNext()) {
                        PositionStep step = (PositionStep)iterator2.next();
                        float a = (float)(offset += step.tickOffset()) / (float)totalInterpolationTicks;
                        this.addStep(step.position(), Mth.rotLerp(a, this.yRot(), yRot), Mth.lerp(a, this.xRot(), xRot), step.tickOffset());
                    }
                    break;
                }
            }
        }

        private static int getInterpolationTicks(List<PositionStep> steps) {
            int ticks = 0;
            for (PositionStep step : steps) {
                ticks += step.tickOffset();
            }
            return ticks;
        }

        private PositionAndRotation getNewPositionAndRotation() {
            while (!this.remainingSteps.isEmpty()) {
                Step step = this.remainingSteps.getFirst();
                int offset = step.tickOffset;
                if (this.currentStepTicks < (float)offset) {
                    float a = this.currentStepTicks / (float)offset;
                    return PositionAndRotation.of(this.lastStepPosRot.position().lerp(step.position(), a), Mth.rotLerp(a, this.lastStepPosRot.yRot(), step.yRot()), Mth.lerp(a, this.lastStepPosRot.xRot(), step.xRot()));
                }
                this.currentStepTicks -= (float)offset;
                this.lastStepPosRot.set(step.position(), step.yRot(), step.xRot());
                this.remainingSteps.removeFirst();
            }
            return this;
        }
    }

    private static class Step
    extends PositionAndRotation.Mutable {
        private final int tickOffset;

        public Step(Vec3 position, float yRot, float xRot, int tickOffset) {
            this.tickOffset = tickOffset;
            this.set(position, yRot, xRot);
        }
    }
}

