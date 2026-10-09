/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class UnfilledBuriedTreasureMapFix
extends DataFix {
    private static final String NEW_TRANSLATION = "item.minecraft.buried_treasure_map";
    private static final String OLD_TRANSLATION = "filled_map.buried_treasure";

    public UnfilledBuriedTreasureMapFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type itemStackType = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder idFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder componentsFinder = itemStackType.findField("components");
        return this.fixTypeEverywhereTyped("UnfilledBuriedTreasureMapFix", itemStackType, itemStack -> {
            Optional<String> id = itemStack.getOptional(idFinder).map(Pair::getSecond);
            if (id.filter(itemId -> itemId.equals("minecraft:map")).isPresent()) {
                return itemStack.updateTyped(componentsFinder, UnfilledBuriedTreasureMapFix::fix);
            }
            return itemStack;
        });
    }

    private static <T> Typed<T> fix(Typed<T> typed) {
        return Util.writeAndReadTypedOrThrow(typed, typed.getType(), value -> {
            boolean holdsLegacyName = value.get("minecraft:item_name").result().flatMap(ExtraDataFixUtils::getPlainTranslationKey).filter(nameKey -> nameKey.equals(OLD_TRANSLATION)).isPresent();
            if (holdsLegacyName) {
                return value.update("minecraft:item_name", itemName -> itemName.set("translate", itemName.createString(NEW_TRANSLATION)));
            }
            return value;
        });
    }
}

