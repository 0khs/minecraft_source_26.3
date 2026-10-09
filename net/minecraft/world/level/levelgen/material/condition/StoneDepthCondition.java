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
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public record StoneDepthCondition(int offset, boolean addSurfaceDepth, int secondaryDepthRange, CaveSurface surfaceType) implements MaterialCondition
{
    public static final MapCodec<StoneDepthCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.INT.fieldOf("offset").forGetter(StoneDepthCondition::offset), (App)Codec.BOOL.fieldOf("add_surface_depth").forGetter(StoneDepthCondition::addSurfaceDepth), (App)Codec.INT.fieldOf("secondary_depth_range").forGetter(StoneDepthCondition::secondaryDepthRange), (App)CaveSurface.CODEC.fieldOf("surface_type").forGetter(StoneDepthCondition::surfaceType)).apply((Applicative)i, StoneDepthCondition::new));

    public MapCodec<StoneDepthCondition> codec() {
        return CODEC;
    }

    @Override
    public ConditionEvaluator compile(MaterialRuleContext ruleContext) {
        final boolean ceiling = this.surfaceType == CaveSurface.CEILING;
        return new MaterialRuleContext.LazyYCondition(this, ruleContext){
            final /* synthetic */ StoneDepthCondition this$0;
            {
                StoneDepthCondition stoneDepthCondition = this$0;
                Objects.requireNonNull(stoneDepthCondition);
                this.this$0 = stoneDepthCondition;
                super(context);
            }

            @Override
            protected boolean compute() {
                int stoneDepth = ceiling ? this.context.stoneDepthBelow() : this.context.stoneDepthAbove();
                int surfaceDepth = this.this$0.addSurfaceDepth ? this.context.surfaceDepth() : 0;
                int secondarySurfaceDepth = this.this$0.secondaryDepthRange == 0 ? 0 : (int)Mth.map(this.context.getSurfaceSecondary(), -1.0, 1.0, 0.0, (double)this.this$0.secondaryDepthRange);
                return stoneDepth <= 1 + this.this$0.offset + surfaceDepth + secondarySurfaceDepth;
            }
        };
    }
}

