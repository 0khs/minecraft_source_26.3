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
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class SwingAnimationComponentSplitFix
extends DataFix {
    public SwingAnimationComponentSplitFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("SwingAnimationComponentSplitFix", this.getInputSchema().getType(References.DATA_COMPONENTS), input -> input.update(DSL.remainderFinder(), tag -> {
            Optional swingAnimationOpt = tag.get("minecraft:swing_animation").result();
            if (swingAnimationOpt.isPresent()) {
                Dynamic swingAnimation = (Dynamic)swingAnimationOpt.get();
                return tag.remove("minecraft:swing_animation").set("minecraft:attack_animation", swingAnimation).set("minecraft:interact_animation", swingAnimation);
            }
            return tag;
        }));
    }
}

