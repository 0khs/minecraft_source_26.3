/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.densityfunction;

import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.CachingDensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.op.CacheFunction;

public class DensityFunctionCompiler {
    private final DensityFunction.CompileContext context;
    private final Map<DensityFunction, DensitySampler> samplers = new ConcurrentHashMap<DensityFunction, DensitySampler>();
    private final Function<DensityFunction, DensitySampler> optimizeAndCompile = this::optimizeAndCompile;
    private final DfRewriteRule optimizerRule = DfRewriteRule.sequence(new DfRewriteRule(this){
        final /* synthetic */ DensityFunctionCompiler this$0;
        {
            DensityFunctionCompiler densityFunctionCompiler = this$0;
            Objects.requireNonNull(densityFunctionCompiler);
            this.this$0 = densityFunctionCompiler;
        }

        @Override
        public DensityFunction rewrite(DensityFunction function) {
            if ((function = DfRewriteRule.INLINE_REFERENCE.rewrite(function)) instanceof CacheFunction) {
                CacheFunction cache = (CacheFunction)function;
                return this.this$0.reuseOrPrepareCache(cache);
            }
            return function.rewriteChildren(this);
        }
    }, DfRewriteRule.SLICE_UNIFORM_AXES);
    private final ReentrantLock compileLock = new ReentrantLock();
    private final Map<DensityFunction, PreparedCache> preparedCaches = new HashMap<DensityFunction, PreparedCache>();
    private int nextCacheId;

    public DensityFunctionCompiler(DensityFunction.CompileContext context) {
        this.context = context;
    }

    public DensitySampler getSampler(DensityFunction function) {
        return this.samplers.computeIfAbsent(function, this.optimizeAndCompile);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private DensitySampler optimizeAndCompile(DensityFunction function) {
        this.compileLock.lock();
        try {
            DensityFunction optimizedFunction = this.optimizerRule.rewrite(function);
            DensitySampler densitySampler = optimizedFunction.compileSampler(this.context);
            return densitySampler;
        }
        finally {
            this.compileLock.unlock();
        }
    }

    private DensityFunction reuseOrPrepareCache(CacheFunction cache) {
        PreparedCache prepared = this.preparedCaches.get(cache.input());
        if (prepared == null) {
            prepared = this.prepareCache(cache);
            this.preparedCaches.put(cache.input(), prepared);
        }
        return prepared;
    }

    private PreparedCache prepareCache(CacheFunction cache) {
        int id = this.nextCacheId++;
        DensityFunction input = this.optimizerRule.rewrite(cache.input());
        CachingDensitySampler cachingSampler = new CachingDensitySampler(id, input.compileSampler(this.context));
        return new PreparedCache(id, input.range(), input.domainAxes(), cachingSampler);
    }

    private record PreparedCache(int id, Interval range, @DensityFunction.Axes int domainAxes, DensitySampler cachingSampler) implements DensityFunction
    {
        @Override
        public DensitySampler compileSampler(DensityFunction.CompileContext context) {
            return this.cachingSampler;
        }

        @Override
        public DensityFunction rewriteChildren(DfRewriteRule rule) {
            return this;
        }

        public MapCodec<PreparedCache> codec() {
            throw new UnsupportedOperationException("PreparedCache should never be encoded");
        }
    }
}

