/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.Hook$HookFunction
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V3818_5
extends NamespacedSchema {
    protected static final Hook.HookFunction UNPACK_PLAIN_ID = new Hook.HookFunction(){

        public <T> T apply(DynamicOps<T> ops, T value) {
            Optional maybePlainId = ops.getStringValue(value).result();
            if (maybePlainId.isPresent()) {
                return (T)ops.createMap(Map.of(ops.createString("id"), value, ops.createString("count"), ops.createInt(1)));
            }
            return value;
        }
    };

    public V3818_5(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(true, References.ITEM_STACK, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"id", (TypeTemplate)References.ITEM_NAME.in(schema), (String)"components", (TypeTemplate)References.DATA_COMPONENTS.in(schema)), (Hook.HookFunction)UNPACK_PLAIN_ID, (Hook.HookFunction)Hook.HookFunction.IDENTITY));
    }
}

