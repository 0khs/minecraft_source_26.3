/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.material.condition;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;

public enum SteepCondition implements MaterialCondition
{
    INSTANCE;

    public static final MapCodec<SteepCondition> CODEC;

    public MapCodec<SteepCondition> codec() {
        return CODEC;
    }

    @Override
    public ConditionEvaluator compile(MaterialRuleContext context) {
        return new MaterialRuleContext.LazyXZCondition(this, context){
            {
                Objects.requireNonNull(this$0);
                super(context);
            }

            @Override
            protected boolean compute() {
                return this.context.surfaceGradientX() <= -4 || this.context.surfaceGradientZ() >= 4;
            }
        };
    }

    static {
        CODEC = MapCodec.unit((Object)INSTANCE);
    }
}

