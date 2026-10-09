/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.densityfunction;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.generator.ConstantFunction;
import net.minecraft.world.level.levelgen.densityfunction.generator.GradientFunction;
import net.minecraft.world.level.levelgen.densityfunction.op.SliceFunction;

@FunctionalInterface
public interface DfRewriteRule {
    public static final DfRewriteRule INLINE_REFERENCE = function -> {
        if (!(function instanceof DensityFunctions.HolderHolder)) return function;
        DensityFunctions.HolderHolder $b$0 = (DensityFunctions.HolderHolder)function;
        try {
            Holder<DensityFunction> patt1$temp;
            Holder<DensityFunction> holder = patt1$temp = $b$0.function();
            return holder.value();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    };
    public static final DfRewriteRule SLICE_UNIFORM_AXES = new SliceUniformAxes(7);

    public static DfRewriteRule sequence(DfRewriteRule ... rules) {
        return function -> {
            for (DfRewriteRule rule : rules) {
                function = rule.rewrite(function);
            }
            return function;
        };
    }

    public DensityFunction rewrite(DensityFunction var1);

    public record SliceUniformAxes(@DensityFunction.Axes int parentDomainAxes) implements DfRewriteRule
    {
        @Override
        public DensityFunction rewrite(DensityFunction function) {
            if (SliceUniformAxes.shouldSkip(function)) {
                return function;
            }
            int domainAxes = function.domainAxes();
            if (this.parentDomainAxes == domainAxes) {
                return function.rewriteChildren(this);
            }
            DensityFunction newFunction = function.rewriteChildren(new SliceUniformAxes(domainAxes));
            int removedAxes = this.parentDomainAxes & ~domainAxes;
            return this.removeAxes(newFunction, removedAxes);
        }

        private DensityFunction removeAxes(DensityFunction function, @DensityFunction.Axes int axes) {
            int filteredAxes = axes & ~SliceUniformAxes.getExistingRemovedAxes(function);
            if ((filteredAxes & 1) != 0) {
                function = new SliceFunction(Direction.Axis.X, 0, function);
            }
            if ((filteredAxes & 4) != 0) {
                function = new SliceFunction(Direction.Axis.Z, 0, function);
            }
            if ((filteredAxes & 2) != 0) {
                function = new SliceFunction(Direction.Axis.Y, 0, function);
            }
            return function;
        }

        private static @DensityFunction.Axes int getExistingRemovedAxes(DensityFunction function) {
            int axes = 0;
            while (function instanceof SliceFunction) {
                SliceFunction slice = (SliceFunction)function;
                axes |= DensityFunction.axesFrom(slice.axis());
                function = slice.input();
            }
            return axes;
        }

        private static boolean shouldSkip(DensityFunction function) {
            return function instanceof ConstantFunction || function instanceof GradientFunction;
        }
    }
}

