/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class PotDecorationsComponentUnflatteningFix
extends DataFix {
    public PotDecorationsComponentUnflatteningFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead("Pot decoration structure fix", this.getInputSchema().getType(References.DATA_COMPONENTS), this.getOutputSchema().getType(References.DATA_COMPONENTS), components -> components.update("minecraft:pot_decorations", PotDecorationsComponentUnflatteningFix::unpackList));
    }

    public static <T> Dynamic<T> unpackList(Dynamic<T> original) {
        Optional decorationIds = original.asStreamOpt().result();
        if (decorationIds.isEmpty()) {
            return original;
        }
        List<Optional> decorationIdList = ((Stream)decorationIds.get()).map(s -> s.asString().result()).toList();
        HashMap<Dynamic, Dynamic> result = new HashMap<Dynamic, Dynamic>(4);
        for (int i = 0; i < 4; ++i) {
            String decorationId = i < decorationIdList.size() ? decorationIdList.get(i).orElse("minecraft:brick") : "minecraft:brick";
            if (decorationId.isEmpty()) {
                return original;
            }
            String sideName = switch (i) {
                case 0 -> "back";
                case 1 -> "left";
                case 2 -> "right";
                case 3 -> "front";
                default -> throw new IndexOutOfBoundsException();
            };
            Map<Dynamic, Dynamic> newStack = Map.of(original.createString("id"), original.createString(decorationId));
            result.put(original.createString(sideName), original.createMap(newStack));
        }
        return original.createMap(result);
    }
}

