/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.fixes.References;

public class SignsOpFlagFix
extends DataFix {
    public SignsOpFlagFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    private TypeRewriteRule makeRuleForType(String entityName) {
        Type entityType = this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, entityName);
        OpticFinder entityF = DSL.namedChoice((String)entityName, (Type)entityType);
        return this.fixTypeEverywhereTyped("Add allow-op flag to " + entityName, this.getInputSchema().getType(References.BLOCK_ENTITY), input -> input.updateTyped(entityF, entityType, entity -> entity.update(DSL.remainderFinder(), remainder -> remainder.set("allow_op_features", remainder.createBoolean(true)))));
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.makeRuleForType("minecraft:sign"), (TypeRewriteRule)this.makeRuleForType("minecraft:hanging_sign"));
    }
}

