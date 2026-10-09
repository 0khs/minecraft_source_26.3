/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.densityfunction.op;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.generator.ConstantFunction;
import org.slf4j.Logger;

public record BinaryFunction(Type type, DensityFunction left, DensityFunction right) implements DensityFunction
{
    private static final Logger LOGGER = LogUtils.getLogger();

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        float f;
        float f2;
        Interval leftRange;
        DensitySampler densitySampler;
        DensitySampler left = this.left.compileSampler(context);
        DensitySampler right = this.right.compileSampler(context);
        switch (this.type.ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                float f3;
                float f4;
                ConstantFunction constantFunction;
                DensityFunction densityFunction = this.left;
                if (densityFunction instanceof ConstantFunction) {
                    constantFunction = (ConstantFunction)densityFunction;
                    f3 = f4 = constantFunction.value();
                    float leftValue = f4;
                    densitySampler = new ConstAddSampler(right, leftValue);
                    return densitySampler;
                }
                densityFunction = this.right;
                if (!(densityFunction instanceof ConstantFunction)) {
                    densitySampler = new AddSampler(left, right);
                    return densitySampler;
                }
                ConstantFunction constantFunction2 = (ConstantFunction)densityFunction;
                f3 = f4 = constantFunction2.value();
                float rightValue = f4;
                densitySampler = new ConstAddSampler(left, rightValue);
                return densitySampler;
            }
            case 1: {
                float f5;
                float f6;
                ConstantFunction constantFunction;
                DensityFunction densityFunction = this.left;
                if (densityFunction instanceof ConstantFunction) {
                    constantFunction = (ConstantFunction)densityFunction;
                    f5 = f6 = constantFunction.value();
                    float leftValue = f6;
                    densitySampler = new ConstSubSampler(leftValue, right);
                    return densitySampler;
                }
                densityFunction = this.right;
                if (!(densityFunction instanceof ConstantFunction)) {
                    densitySampler = new SubSampler(left, right);
                    return densitySampler;
                }
                ConstantFunction constantFunction3 = (ConstantFunction)densityFunction;
                f5 = f6 = constantFunction3.value();
                float rightValue = f6;
                densitySampler = new ConstAddSampler(left, -rightValue);
                return densitySampler;
            }
            case 2: {
                float f7;
                float f8;
                ConstantFunction constantFunction;
                DensityFunction densityFunction = this.left;
                if (densityFunction instanceof ConstantFunction) {
                    constantFunction = (ConstantFunction)densityFunction;
                    f7 = f8 = constantFunction.value();
                    float leftValue = f8;
                    densitySampler = new ConstMulSampler(right, leftValue);
                    return densitySampler;
                }
                densityFunction = this.right;
                if (!(densityFunction instanceof ConstantFunction)) {
                    densitySampler = new MulSampler(left, right);
                    return densitySampler;
                }
                ConstantFunction constantFunction4 = (ConstantFunction)densityFunction;
                f7 = f8 = constantFunction4.value();
                float rightValue = f8;
                densitySampler = new ConstMulSampler(left, rightValue);
                return densitySampler;
            }
            case 3: {
                float f9;
                float f10;
                ConstantFunction constantFunction;
                DensityFunction densityFunction = this.left;
                if (densityFunction instanceof ConstantFunction) {
                    constantFunction = (ConstantFunction)densityFunction;
                    f9 = f10 = constantFunction.value();
                    float leftValue = f10;
                    densitySampler = new ConstDivSampler(leftValue, right);
                    return densitySampler;
                }
                densityFunction = this.right;
                if (!(densityFunction instanceof ConstantFunction)) {
                    densitySampler = new DivSampler(left, right);
                    return densitySampler;
                }
                ConstantFunction constantFunction5 = (ConstantFunction)densityFunction;
                f9 = f10 = constantFunction5.value();
                float rightValue = f10;
                densitySampler = new ConstMulSampler(left, 1.0f / rightValue);
                return densitySampler;
            }
            case 4: {
                float f11;
                float f12;
                leftRange = this.left.range();
                Interval rightRange = this.right.range();
                if (leftRange.max() < rightRange.min()) {
                    this.warnNonIntersecting();
                    densitySampler = left;
                    return densitySampler;
                }
                if (rightRange.max() < leftRange.min()) {
                    this.warnNonIntersecting();
                    densitySampler = right;
                    return densitySampler;
                }
                DensityFunction densityFunction = this.left;
                if (densityFunction instanceof ConstantFunction) {
                    ConstantFunction constantFunction = (ConstantFunction)densityFunction;
                    f11 = f12 = constantFunction.value();
                    float leftValue = f12;
                    densitySampler = new ConstMinSampler(right, leftValue);
                    return densitySampler;
                }
                densityFunction = this.right;
                if (!(densityFunction instanceof ConstantFunction)) {
                    densitySampler = new MinSampler(left, right, rightRange.min());
                    return densitySampler;
                }
                ConstantFunction constantFunction = (ConstantFunction)densityFunction;
                f11 = f12 = constantFunction.value();
                float rightValue = f12;
                densitySampler = new ConstMinSampler(left, rightValue);
                return densitySampler;
            }
            case 5: 
        }
        leftRange = this.left.range();
        Interval rightRange = this.right.range();
        if (leftRange.min() > rightRange.max()) {
            this.warnNonIntersecting();
            densitySampler = left;
            return densitySampler;
        }
        if (rightRange.min() > leftRange.max()) {
            this.warnNonIntersecting();
            densitySampler = right;
            return densitySampler;
        }
        DensityFunction densityFunction = this.left;
        if (densityFunction instanceof ConstantFunction) {
            ConstantFunction constantFunction = (ConstantFunction)densityFunction;
            f = f2 = constantFunction.value();
            float leftValue = f2;
            densitySampler = new ConstMaxSampler(right, leftValue);
            return densitySampler;
        }
        densityFunction = this.right;
        if (!(densityFunction instanceof ConstantFunction)) {
            densitySampler = new MaxSampler(left, right, rightRange.max());
            return densitySampler;
        }
        ConstantFunction constantFunction = (ConstantFunction)densityFunction;
        try {
            f = f2 = constantFunction.value();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        float rightValue = f2;
        densitySampler = new ConstMaxSampler(left, rightValue);
        return densitySampler;
    }

    private void warnNonIntersecting() {
        LOGGER.warn("Compiling a {} function between two non-overlapping inputs: {} ({}) and {} ({})", new Object[]{this.type, this.left, this.left.range(), this.right, this.right.range()});
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction left = rule.rewrite(this.left);
        DensityFunction right = rule.rewrite(this.right);
        if (left == this.left && this.right == right) {
            return this;
        }
        return new BinaryFunction(this.type, left, right);
    }

    @Override
    public Interval range() {
        Interval left = this.left.range();
        Interval right = this.right.range();
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Interval.add(left, right);
            case 1 -> Interval.sub(left, right);
            case 2 -> Interval.mul(left, right);
            case 3 -> Interval.div(left, right);
            case 5 -> Interval.max(left, right);
            case 4 -> Interval.min(left, right);
        };
    }

    @Override
    public @DensityFunction.Axes int domainAxes() {
        return this.left.domainAxes() | this.right.domainAxes();
    }

    public MapCodec<BinaryFunction> codec() {
        return this.type().codec;
    }

    public static enum Type {
        ADD("add"),
        SUB("sub"),
        MUL("mul"),
        DIV("div"),
        MIN("min"),
        MAX("max");

        public final String id;
        public final MapCodec<BinaryFunction> codec = RecordCodecBuilder.mapCodec(i -> i.group((App)DensityFunction.CODEC.fieldOf("left").forGetter(BinaryFunction::left), (App)DensityFunction.CODEC.fieldOf("right").forGetter(BinaryFunction::right)).apply((Applicative)i, (left, right) -> new BinaryFunction(this, (DensityFunction)left, (DensityFunction)right)));

        private Type(String id) {
            this.id = id;
        }
    }

    public record ConstAddSampler(DensitySampler left, float right) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.left.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.addTo(i, this.right);
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.left.sampleValue(context, blockX, blockY, blockZ) + this.right;
        }
    }

    public record AddSampler(DensitySampler left, DensitySampler right) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.left.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer rightBuffer = context.acquireBuffer(volume);){
                this.right.sampleVolume(context, rightBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); ++i) {
                    outputBuffer.addTo(i, rightBuffer.get(i));
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.left.sampleValue(context, blockX, blockY, blockZ) + this.right.sampleValue(context, blockX, blockY, blockZ);
        }
    }

    public record ConstSubSampler(float left, DensitySampler right) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.right.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, this.left - outputBuffer.get(i));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.left - this.right.sampleValue(context, blockX, blockY, blockZ);
        }
    }

    public record SubSampler(DensitySampler left, DensitySampler right) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.left.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer rightBuffer = context.acquireBuffer(volume);){
                this.right.sampleVolume(context, rightBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); ++i) {
                    outputBuffer.addTo(i, -rightBuffer.get(i));
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.left.sampleValue(context, blockX, blockY, blockZ) - this.right.sampleValue(context, blockX, blockY, blockZ);
        }
    }

    public record ConstMulSampler(DensitySampler left, float right) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.left.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, outputBuffer.get(i) * this.right);
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.left.sampleValue(context, blockX, blockY, blockZ) * this.right;
        }
    }

    public record MulSampler(DensitySampler left, DensitySampler right) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.left.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer rightBuffer = context.acquireBuffer(volume);){
                this.right.sampleVolume(context, rightBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); ++i) {
                    outputBuffer.set(i, outputBuffer.get(i) * rightBuffer.get(i));
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float left = this.left.sampleValue(context, blockX, blockY, blockZ);
            if (left == 0.0f) {
                return 0.0f;
            }
            return left * this.right.sampleValue(context, blockX, blockY, blockZ);
        }
    }

    public record ConstDivSampler(float left, DensitySampler right) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.right.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                outputBuffer.set(i, this.left / outputBuffer.get(i));
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.left / this.right.sampleValue(context, blockX, blockY, blockZ);
        }
    }

    public record DivSampler(DensitySampler left, DensitySampler right) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.left.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer rightBuffer = context.acquireBuffer(volume);){
                this.right.sampleVolume(context, rightBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); ++i) {
                    outputBuffer.set(i, outputBuffer.get(i) / rightBuffer.get(i));
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float left = this.left.sampleValue(context, blockX, blockY, blockZ);
            if (left == 0.0f) {
                return 0.0f;
            }
            return left / this.right.sampleValue(context, blockX, blockY, blockZ);
        }
    }

    public record ConstMinSampler(DensitySampler left, float right) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.left.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                if (!(this.right < outputBuffer.get(i))) continue;
                outputBuffer.set(i, this.right);
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return Math.min(this.left.sampleValue(context, blockX, blockY, blockZ), this.right);
        }
    }

    public record MinSampler(DensitySampler left, DensitySampler right, float rightMinValue) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.left.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer rightBuffer = context.acquireBuffer(volume);){
                this.right.sampleVolume(context, rightBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); ++i) {
                    float rightValue = rightBuffer.get(i);
                    if (!(rightValue < outputBuffer.get(i))) continue;
                    outputBuffer.set(i, rightValue);
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float left = this.left.sampleValue(context, blockX, blockY, blockZ);
            if (left <= this.rightMinValue) {
                return left;
            }
            return Math.min(left, this.right.sampleValue(context, blockX, blockY, blockZ));
        }
    }

    public record ConstMaxSampler(DensitySampler left, float right) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.left.sampleVolume(context, outputBuffer, volume);
            for (int i = 0; i < outputBuffer.size(); ++i) {
                if (!(this.right > outputBuffer.get(i))) continue;
                outputBuffer.set(i, this.right);
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return Math.max(this.left.sampleValue(context, blockX, blockY, blockZ), this.right);
        }
    }

    public record MaxSampler(DensitySampler left, DensitySampler right, float rightMaxValue) implements DensitySampler
    {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.left.sampleVolume(context, outputBuffer, volume);
            try (ScopedDensityBuffer rightBuffer = context.acquireBuffer(volume);){
                this.right.sampleVolume(context, rightBuffer, volume);
                for (int i = 0; i < outputBuffer.size(); ++i) {
                    float rightValue = rightBuffer.get(i);
                    if (!(rightValue > outputBuffer.get(i))) continue;
                    outputBuffer.set(i, rightValue);
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float left = this.left.sampleValue(context, blockX, blockY, blockZ);
            if (left >= this.rightMaxValue) {
                return left;
            }
            return Math.max(left, this.right.sampleValue(context, blockX, blockY, blockZ));
        }
    }
}

