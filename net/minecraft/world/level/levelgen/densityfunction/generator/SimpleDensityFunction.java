/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.densityfunction.generator;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.ContextBoundSampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.generator.ConstantFunction;

public enum SimpleDensityFunction implements DensityFunction
{
    BLEND_ALPHA("blend_alpha"),
    BLEND_OFFSET("blend_offset"),
    BEARDIFIER("beardifier");

    private final String id;
    private final MapCodec<SimpleDensityFunction> codec = MapCodec.unit((Object)this);

    private SimpleDensityFunction(String id) {
        this.id = id;
    }

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 2 -> new ContextBoundSampler(Beardifier.CONTEXT_KEY, new ConstantFunction.Sampler(0.0f));
            case 0 -> new ContextBoundSampler(Blender.ALPHA_KEY, new ConstantFunction.Sampler(1.0f));
            case 1 -> new ContextBoundSampler(Blender.OFFSET_KEY, new ConstantFunction.Sampler(0.0f));
        };
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    @Override
    public Interval range() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Interval.of(0.0f, 1.0f);
            case 1 -> Interval.INFINITE;
            case 2 -> Beardifier.RANGE;
        };
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 2 -> 7;
            case 0, 1 -> 5;
        };
    }

    public String id() {
        return this.id;
    }

    public MapCodec<SimpleDensityFunction> codec() {
        return this.codec;
    }
}

