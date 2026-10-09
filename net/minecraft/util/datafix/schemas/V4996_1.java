/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V4996_1
extends NamespacedSchema {
    public V4996_1(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map map = super.registerBlockEntities(schema);
        schema.register(map, "minecraft:decorated_pot", () -> DSL.optionalFields((String)"sherds", (TypeTemplate)DSL.optionalFields((String)"back", (TypeTemplate)References.ITEM_STACK.in(schema), (String)"left", (TypeTemplate)References.ITEM_STACK.in(schema), (String)"right", (TypeTemplate)References.ITEM_STACK.in(schema), (String)"front", (TypeTemplate)References.ITEM_STACK.in(schema)), (String)"item", (TypeTemplate)References.ITEM_STACK.in(schema)));
        return map;
    }
}

