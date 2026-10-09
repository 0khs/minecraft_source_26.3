/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.References;

public class DebugProfileRenameEntryFix
extends DataFix {
    private final String oldName;
    private final String newName;

    public DebugProfileRenameEntryFix(Schema outputSchema, String oldName, String newName) {
        super(outputSchema, false);
        this.oldName = oldName;
        this.newName = newName;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("DebugProfilePostEffectsFix", this.getInputSchema().getType(References.DEBUG_PROFILE), input -> input.update(DSL.remainderFinder(), remainder -> remainder.update("custom", custom -> custom.renameField(this.oldName, this.newName))));
    }
}

