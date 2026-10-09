/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.PotDecorationsComponentUnflatteningFix;
import net.minecraft.util.datafix.fixes.References;

public class PotDecorationsBlockEntityUnflatteningFix
extends NamedEntityFix {
    public PotDecorationsBlockEntityUnflatteningFix(Schema outputSchema) {
        super(outputSchema, true, "PotDecorationsBlockEntityUnflatteningFix", References.BLOCK_ENTITY, "minecraft:decorated_pot");
    }

    @Override
    protected Typed<?> fix(Typed<?> entity) {
        Type newType = (Type)this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY).types().get("minecraft:decorated_pot");
        return Util.writeAndReadTypedOrThrow(entity, newType, contents -> {
            Dynamic original = contents.get("sherds").orElseEmptyList();
            return contents.set("sherds", PotDecorationsComponentUnflatteningFix.unpackList(original));
        });
    }
}

