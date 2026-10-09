/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.DecoderException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import io.netty.handler.codec.DecoderException;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.PositionStep;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public sealed interface VecDelta {
    public static final VecDelta ZERO = new Linear(0, 0, 0);

    public int stepCount();

    public boolean hasDeltaX();

    public boolean hasDeltaZ();

    public PositionPath decode(VecDeltaCodec var1);

    public static VecDelta read(FriendlyByteBuf input, int stepCount) {
        if (stepCount > 0) {
            int maxSteps = input.readableBytes() / 7;
            if (stepCount > maxSteps) {
                throw new DecoderException("VecDelta with size " + stepCount + " is bigger than allowed " + maxSteps);
            }
            ArrayList<Stepped.DeltaStep> steps = new ArrayList<Stepped.DeltaStep>(stepCount);
            for (int i = 0; i < stepCount; ++i) {
                int ticks = input.readVarInt();
                short xa = input.readShort();
                short ya = input.readShort();
                short za = input.readShort();
                steps.add(new Stepped.DeltaStep(xa, ya, za, ticks));
            }
            return new Stepped(steps);
        }
        short xa = input.readShort();
        short ya = input.readShort();
        short za = input.readShort();
        return new Linear(xa, ya, za);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static void write(FriendlyByteBuf output, VecDelta delta) {
        Object object;
        VecDelta vecDelta = delta;
        Objects.requireNonNull(vecDelta);
        VecDelta vecDelta2 = vecDelta;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Linear.class, Stepped.class}, (VecDelta)vecDelta2, n)) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                short s;
                Linear linear = (Linear)vecDelta2;
                short s2 = s = linear.xa();
                short xa = s;
                s2 = s = linear.ya();
                short ya = s;
                s2 = s = linear.za();
                short za = s;
                output.writeShort(xa);
                output.writeShort(ya);
                output.writeShort(za);
                return;
            }
            case 1: 
        }
        Stepped stepped = (Stepped)vecDelta2;
        try {
            object = stepped.steps();
            List<Stepped.DeltaStep> steps = object;
            object = steps.iterator();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        while (object.hasNext()) {
            Stepped.DeltaStep step = (Stepped.DeltaStep)object.next();
            output.writeVarInt(step.ticks);
            output.writeShort(step.xa);
            output.writeShort(step.ya);
            output.writeShort(step.za);
        }
        return;
    }

    public record Stepped(List<DeltaStep> steps) implements VecDelta
    {
        private static final int MIN_BYTES_PER_STEP = 7;

        @Override
        public int stepCount() {
            return this.steps.size();
        }

        @Override
        public boolean hasDeltaX() {
            for (DeltaStep step : this.steps) {
                if (step.xa == 0) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean hasDeltaZ() {
            for (DeltaStep step : this.steps) {
                if (step.za == 0) continue;
                return true;
            }
            return false;
        }

        @Override
        public PositionPath decode(VecDeltaCodec positionCodec) {
            if (this.steps.isEmpty()) {
                return PositionPath.of(positionCodec.getBase());
            }
            ArrayList<PositionStep> output = new ArrayList<PositionStep>(this.steps.size());
            Vec3 originalBase = positionCodec.getBase();
            for (DeltaStep e : this.steps) {
                Vec3 pos = positionCodec.decode(e.xa, e.ya, e.za);
                output.add(new PositionStep(pos, e.ticks));
                positionCodec.setBase(pos);
            }
            positionCodec.setBase(originalBase);
            return PositionPath.stepped(output);
        }

        public static @Nullable Stepped tryEncode(VecDeltaCodec positionCodec, List<PositionStep> steps) {
            if (steps.isEmpty()) {
                return new Stepped(List.of());
            }
            ArrayList<DeltaStep> output = new ArrayList<DeltaStep>(steps.size());
            Vec3 originalBase = positionCodec.getBase();
            for (PositionStep step : steps) {
                long za;
                long ya;
                Vec3 pos = step.position();
                long xa = positionCodec.encodeX(pos);
                if (VecDeltaCodec.isDeltaTooBig(xa, ya = positionCodec.encodeY(pos), za = positionCodec.encodeZ(pos))) {
                    positionCodec.setBase(originalBase);
                    return null;
                }
                output.add(new DeltaStep((short)xa, (short)ya, (short)za, step.tickOffset()));
                positionCodec.setBase(pos);
            }
            positionCodec.setBase(originalBase);
            return new Stepped(output);
        }

        public record DeltaStep(short xa, short ya, short za, int ticks) {
        }
    }

    public record Linear(short xa, short ya, short za) implements VecDelta
    {
        @Override
        public int stepCount() {
            return 0;
        }

        @Override
        public boolean hasDeltaX() {
            return this.xa != 0;
        }

        @Override
        public boolean hasDeltaZ() {
            return this.za != 0;
        }

        @Override
        public PositionPath decode(VecDeltaCodec positionCodec) {
            Vec3 pos = positionCodec.decode(this.xa, this.ya, this.za);
            return PositionPath.of(pos);
        }
    }
}

