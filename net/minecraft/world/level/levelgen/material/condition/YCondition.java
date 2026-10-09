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
import java.util.Objects;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;

public record YCondition(VerticalAnchor anchor, int surfaceDepthMultiplier, boolean addStoneDepth) implements MaterialCondition
{
    public static final MapCodec<YCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)VerticalAnchor.CODEC.fieldOf("anchor").forGetter(YCondition::anchor), (App)Codec.intRange((int)-20, (int)20).fieldOf("surface_depth_multiplier").forGetter(YCondition::surfaceDepthMultiplier), (App)Codec.BOOL.fieldOf("add_stone_depth").forGetter(YCondition::addStoneDepth)).apply((Applicative)i, YCondition::new));

    public MapCodec<YCondition> codec() {
        return CODEC;
    }

    @Override
    public ConditionEvaluator compile(MaterialRuleContext ruleContext) {
        return new MaterialRuleContext.LazyYCondition(this, ruleContext){
            final /* synthetic */ YCondition this$0;
            {
                YCondition yCondition = this$0;
                Objects.requireNonNull(yCondition);
                this.this$0 = yCondition;
                super(context);
            }

            @Override
            protected boolean compute() {
                return this.context.blockY() + (this.this$0.addStoneDepth ? this.context.stoneDepthAbove() : 0) >= this.context.resolveAnchorY(this.this$0.anchor) + this.context.surfaceDepth() * this.this$0.surfaceDepthMultiplier;
            }
        };
    }
}

