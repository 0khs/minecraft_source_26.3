/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.annotations.VisibleForTesting;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.protocol.game.VecDelta;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.PositionStep;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class VecDeltaCodec {
    private static final double TRUNCATION_STEPS = 4096.0;
    private Vec3 base = Vec3.ZERO;

    @VisibleForTesting
    static long encode(double input) {
        return Math.round(input * 4096.0);
    }

    @VisibleForTesting
    static double decode(long v) {
        return (double)v / 4096.0;
    }

    public static double encodingPrecisionLoss(double d) {
        return VecDeltaCodec.decode(VecDeltaCodec.encode(d)) - d;
    }

    public static boolean isDeltaTooBig(long xa, long ya, long za) {
        return xa < -32768L || xa > 32767L || ya < -32768L || ya > 32767L || za < -32768L || za > 32767L;
    }

    public Vec3 decode(long xa, long ya, long za) {
        if (xa == 0L && ya == 0L && za == 0L) {
            return this.base;
        }
        double x = xa == 0L ? this.base.x : VecDeltaCodec.decode(VecDeltaCodec.encode(this.base.x) + xa);
        double y = ya == 0L ? this.base.y : VecDeltaCodec.decode(VecDeltaCodec.encode(this.base.y) + ya);
        double z = za == 0L ? this.base.z : VecDeltaCodec.decode(VecDeltaCodec.encode(this.base.z) + za);
        return new Vec3(x, y, z);
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public @Nullable VecDelta tryEncode(PositionPath position) {
        Object steps;
        VecDelta vecDelta;
        PositionPath positionPath = position;
        Objects.requireNonNull(positionPath);
        PositionPath positionPath2 = positionPath;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PositionPath.Linear.class, PositionPath.Stepped.class}, (PositionPath)positionPath2, n)) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                PositionPath.Linear linear = (PositionPath.Linear)positionPath2;
                try {
                    Vec3 vec3;
                    Vec3 pos = vec3 = linear.endPosition();
                    vecDelta = this.tryEncode(pos);
                    return vecDelta;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            case 1: 
        }
        PositionPath.Stepped stepped = (PositionPath.Stepped)positionPath2;
        {
            Object object = stepped.endPosition();
            steps = object = stepped.steps();
        }
        vecDelta = VecDelta.Stepped.tryEncode(this, (List<PositionStep>)steps);
        return vecDelta;
    }

    public @Nullable VecDelta tryEncode(Vec3 pos) {
        long za;
        long ya;
        long xa = this.encodeX(pos);
        if (VecDeltaCodec.isDeltaTooBig(xa, ya = this.encodeY(pos), za = this.encodeZ(pos))) {
            return null;
        }
        return new VecDelta.Linear((short)xa, (short)ya, (short)za);
    }

    public long encodeX(Vec3 pos) {
        return VecDeltaCodec.encode(pos.x) - VecDeltaCodec.encode(this.base.x);
    }

    public long encodeY(Vec3 pos) {
        return VecDeltaCodec.encode(pos.y) - VecDeltaCodec.encode(this.base.y);
    }

    public long encodeZ(Vec3 pos) {
        return VecDeltaCodec.encode(pos.z) - VecDeltaCodec.encode(this.base.z);
    }

    public Vec3 delta(Vec3 pos) {
        return pos.subtract(this.base);
    }

    public void setBase(Vec3 base) {
        this.base = base;
    }

    public Vec3 getBase() {
        return this.base;
    }
}

