/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.material.condition;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;

public record VerticalGradientCondition(Identifier randomName, VerticalAnchor trueAtAndBelow, VerticalAnchor falseAtAndAbove) implements MaterialCondition
{
    public static final MapCodec<VerticalGradientCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Identifier.CODEC.fieldOf("random_name").forGetter(VerticalGradientCondition::randomName), (App)VerticalAnchor.CODEC.fieldOf("true_at_and_below").forGetter(VerticalGradientCondition::trueAtAndBelow), (App)VerticalAnchor.CODEC.fieldOf("false_at_and_above").forGetter(VerticalGradientCondition::falseAtAndAbove)).apply((Applicative)i, VerticalGradientCondition::new));

    public MapCodec<VerticalGradientCondition> codec() {
        return CODEC;
    }

    @Override
    public ConditionEvaluator compile(MaterialRuleContext ruleContext) {
        final int trueAtAndBelow = ruleContext.resolveAnchorY(this.trueAtAndBelow);
        final int falseAtAndAbove = ruleContext.resolveAnchorY(this.falseAtAndAbove);
        final PositionalRandomFactory randomFactory = ruleContext.getOrCreateRandomFactory(this.randomName);
        return new MaterialRuleContext.LazyYCondition(this, ruleContext){
            {
                Objects.requireNonNull(this$0);
                super(context);
            }

            @Override
            protected boolean compute() {
                int blockY = this.context.blockY();
                if (blockY <= trueAtAndBelow) {
                    return true;
                }
                if (blockY >= falseAtAndAbove) {
                    return false;
                }
                double probability = Mth.map((double)blockY, (double)trueAtAndBelow, (double)falseAtAndAbove, 1.0, 0.0);
                RandomSource random = randomFactory.at(this.context.blockX(), blockY, this.context.blockZ());
                return (double)random.nextFloat() < probability;
            }
        };
    }
}

