/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class BlendingDataNoValueHeightFix
extends DataFix {
    public BlendingDataNoValueHeightFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("BlendingDataNoValueHeightFix", this.getInputSchema().getType(References.CHUNK), chunk -> chunk.update(DSL.remainderFinder(), BlendingDataNoValueHeightFix::fix));
    }

    private static Dynamic<?> fix(Dynamic<?> chunk) {
        return chunk.update("blending_data", blendingData -> blendingData.update("heights", heights -> heights.createList(heights.asStream().map(BlendingDataNoValueHeightFix::fixHeightValue))));
    }

    private static Dynamic<?> fixHeightValue(Dynamic<?> height) {
        double heightValue = height.asDouble(Double.MAX_VALUE);
        if (heightValue == Double.MAX_VALUE) {
            return height.createFloat(Float.MAX_VALUE);
        }
        return height.createFloat((float)heightValue);
    }
}

