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

public enum AbovePreliminarySurfaceCondition implements MaterialCondition
{
    INSTANCE;

    public static final MapCodec<AbovePreliminarySurfaceCondition> CODEC;

    public MapCodec<AbovePreliminarySurfaceCondition> codec() {
        return CODEC;
    }

    @Override
    public ConditionEvaluator compile(MaterialRuleContext context) {
        return () -> context.blockY() >= context.getMinSurfaceLevel();
    }

    static {
        CODEC = MapCodec.unit((Object)INSTANCE);
    }
}

