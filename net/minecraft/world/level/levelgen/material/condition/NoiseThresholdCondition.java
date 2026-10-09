/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.material.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.DoubleSupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record NoiseThresholdCondition(ResourceKey<NormalNoise> noise, double minThreshold, double maxThreshold, boolean is3d) implements MaterialCondition
{
    public static final MapCodec<NoiseThresholdCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)ResourceKey.codec(Registries.NOISE).fieldOf("noise").forGetter(NoiseThresholdCondition::noise), (App)Codec.DOUBLE.fieldOf("min_threshold").forGetter(NoiseThresholdCondition::minThreshold), (App)Codec.DOUBLE.fieldOf("max_threshold").forGetter(NoiseThresholdCondition::maxThreshold), (App)Codec.BOOL.optionalFieldOf("is_3d", (Object)false).forGetter(NoiseThresholdCondition::is3d)).apply((Applicative)i, NoiseThresholdCondition::new));

    public MapCodec<NoiseThresholdCondition> codec() {
        return CODEC;
    }

    @Override
    public ConditionEvaluator compile(MaterialRuleContext ruleContext) {
        DoubleSupplier noise = ruleContext.getNoiseSampler(this.noise, this.is3d);
        return () -> {
            double value = noise.getAsDouble();
            return value >= this.minThreshold && value <= this.maxThreshold;
        };
    }
}

