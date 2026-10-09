/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.material.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;

public enum TemperatureCondition implements MaterialCondition
{
    INSTANCE;

    public static final MapCodec<TemperatureCondition> CODEC;

    public MapCodec<TemperatureCondition> codec() {
        return CODEC;
    }

    @Override
    public ConditionEvaluator compile(MaterialRuleContext context) {
        return () -> context.getBiome().value().coldEnoughToSnow(context.blockPos(), context.getSeaLevel());
    }

    static {
        CODEC = MapCodec.unit((Object)INSTANCE);
    }
}

