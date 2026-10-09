/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.densityfunction;

import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

public enum DistanceMetric implements StringRepresentable
{
    EUCLIDEAN("euclidean"),
    EUCLIDEAN_SQUARED("euclidean_squared"),
    MANHATTAN("manhattan"),
    CHEBYSHEV("chebyshev");

    public static final Codec<DistanceMetric> CODEC;
    private final String name;

    private DistanceMetric(String name) {
        this.name = name;
    }

    public float compute(float deltaX, float deltaY, float deltaZ) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Mth.length(deltaX, deltaY, deltaZ);
            case 1 -> Mth.lengthSquared(deltaX, deltaY, deltaZ);
            case 2 -> Math.abs(deltaX) + Math.abs(deltaY) + Math.abs(deltaZ);
            case 3 -> Math.max(Math.max(Math.abs(deltaX), Math.abs(deltaY)), Math.abs(deltaZ));
        };
    }

    public float compute(float deltaX, float deltaZ) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Mth.length(deltaX, deltaZ);
            case 1 -> Mth.lengthSquared(deltaX, deltaZ);
            case 2 -> Math.abs(deltaX) + Math.abs(deltaZ);
            case 3 -> Math.max(Math.abs(deltaX), Math.abs(deltaZ));
        };
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromEnum(DistanceMetric::values);
    }
}

