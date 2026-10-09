/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.material.rule;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;

public enum BandlandsRule implements MaterialRule
{
    INSTANCE;

    public static final MapCodec<BandlandsRule> CODEC;

    public MapCodec<BandlandsRule> codec() {
        return CODEC;
    }

    @Override
    public RuleEvaluator compile(MaterialRuleContext context) {
        return context::getBand;
    }

    static {
        CODEC = MapCodec.unit((Object)INSTANCE);
    }
}

