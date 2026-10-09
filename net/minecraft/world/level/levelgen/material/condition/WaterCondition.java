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
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;

public record WaterCondition(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) implements MaterialCondition
{
    public static final MapCodec<WaterCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.INT.fieldOf("offset").forGetter(WaterCondition::offset), (App)Codec.intRange((int)-20, (int)20).fieldOf("surface_depth_multiplier").forGetter(WaterCondition::surfaceDepthMultiplier), (App)Codec.BOOL.fieldOf("add_stone_depth").forGetter(WaterCondition::addStoneDepth)).apply((Applicative)i, WaterCondition::new));

    public MapCodec<WaterCondition> codec() {
        return CODEC;
    }

    @Override
    public ConditionEvaluator compile(MaterialRuleContext ruleContext) {
        return new MaterialRuleContext.LazyYCondition(this, ruleContext){
            final /* synthetic */ WaterCondition this$0;
            {
                WaterCondition waterCondition = this$0;
                Objects.requireNonNull(waterCondition);
                this.this$0 = waterCondition;
                super(context);
            }

            @Override
            protected boolean compute() {
                return this.context.waterHeight() == Integer.MIN_VALUE || this.context.blockY() + (this.this$0.addStoneDepth ? this.context.stoneDepthAbove() : 0) >= this.context.waterHeight() + this.this$0.offset + this.context.surfaceDepth() * this.this$0.surfaceDepthMultiplier;
            }
        };
    }
}

