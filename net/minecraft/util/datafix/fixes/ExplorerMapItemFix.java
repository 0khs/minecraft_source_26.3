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
 *  com.mojang.serialization.Dynamic
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
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ExplorerMapItemFix
extends DataFix {
    private static final Map<String, String> DECORATION_TYPE_TO_ITEM_ID = Map.ofEntries(Map.entry("minecraft:mansion", "minecraft:woodland_explorer_map"), Map.entry("minecraft:monument", "minecraft:ocean_explorer_map"), Map.entry("minecraft:trial_chambers", "minecraft:trial_explorer_map"), Map.entry("minecraft:jungle_temple", "minecraft:jungle_explorer_map"), Map.entry("minecraft:swamp_hut", "minecraft:swamp_explorer_map"), Map.entry("minecraft:village_desert", "minecraft:desert_village_map"), Map.entry("minecraft:village_plains", "minecraft:plains_village_map"), Map.entry("minecraft:village_savanna", "minecraft:savanna_village_map"), Map.entry("minecraft:village_snowy", "minecraft:snowy_village_map"), Map.entry("minecraft:village_taiga", "minecraft:taiga_village_map"), Map.entry("minecraft:red_x", "minecraft:buried_treasure_map"));
    private static final Map<String, String> DECORATION_TYPE_TO_LEGACY_NAME_KEY = Map.ofEntries(Map.entry("minecraft:mansion", "filled_map.mansion"), Map.entry("minecraft:monument", "filled_map.monument"), Map.entry("minecraft:trial_chambers", "filled_map.trial_chambers"), Map.entry("minecraft:jungle_temple", "filled_map.explorer_jungle"), Map.entry("minecraft:swamp_hut", "filled_map.explorer_swamp"), Map.entry("minecraft:village_desert", "filled_map.village_desert"), Map.entry("minecraft:village_plains", "filled_map.village_plains"), Map.entry("minecraft:village_savanna", "filled_map.village_savanna"), Map.entry("minecraft:village_snowy", "filled_map.village_snowy"), Map.entry("minecraft:village_taiga", "filled_map.village_taiga"), Map.entry("minecraft:red_x", "filled_map.buried_treasure"));
    private static final String ITEM_NAME_KEY = "minecraft:item_name";
    private static final String DECORATION_KEY = "+";

    public ExplorerMapItemFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    private static <T> Dynamic<T> removeLegacyItemName(Dynamic<T> components, String legacyNameKey) {
        boolean holdsLegacyName = components.get(ITEM_NAME_KEY).result().flatMap(ExtraDataFixUtils::getPlainTranslationKey).filter(nameKey -> nameKey.equals(legacyNameKey)).isPresent();
        return holdsLegacyName ? components.remove(ITEM_NAME_KEY) : components;
    }

    public TypeRewriteRule makeRule() {
        Type itemStackType = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder idFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder componentsFinder = itemStackType.findField("components");
        return this.fixTypeEverywhereTyped("ExplorerMapItemFix", itemStackType, itemStack -> {
            Optional<String> id = itemStack.getOptional(idFinder).map(Pair::getSecond);
            if (id.filter(itemId -> itemId.equals("minecraft:filled_map")).isEmpty()) {
                return itemStack;
            }
            Optional components = itemStack.getOptionalTyped(componentsFinder);
            if (components.isEmpty()) {
                return itemStack;
            }
            Dynamic componentsData = (Dynamic)((Typed)components.get()).getOrCreate(DSL.remainderFinder());
            Optional mapDecorations = componentsData.get("minecraft:map_decorations").result();
            if (mapDecorations.isEmpty()) {
                return itemStack;
            }
            Optional explorerDecoration = ((Dynamic)mapDecorations.get()).get(DECORATION_KEY).result();
            if (explorerDecoration.isEmpty()) {
                return itemStack;
            }
            String decorationType = NamespacedSchema.ensureNamespaced(((Dynamic)explorerDecoration.get()).get("type").asString(""));
            String newItemId = DECORATION_TYPE_TO_ITEM_ID.get(decorationType);
            if (newItemId == null) {
                return itemStack;
            }
            String legacyNameKey = DECORATION_TYPE_TO_LEGACY_NAME_KEY.get(decorationType);
            return Util.writeAndReadTypedOrThrow(itemStack, itemStack.getType(), data -> data.set("id", data.createString(newItemId)).update("components", componentData -> ExplorerMapItemFix.removeLegacyItemName(componentData, legacyNameKey)));
        });
    }
}

