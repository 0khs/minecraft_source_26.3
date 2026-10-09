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
 *  com.mojang.datafixers.types.templates.List$ListType
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.jspecify.annotations.Nullable;

public class MergeTerrainChunkStatusFix
extends DataFix {
    private static final String BEFORE_TERRAIN_STATUS = "minecraft:biomes";
    private static final String OLD_FINAL_TERRAIN_STATUS = "minecraft:carvers";
    private static final Set<String> OLD_INTERMEDIATE_TERRAIN_STATUSES = Set.of("minecraft:noise", "minecraft:surface");

    public MergeTerrainChunkStatusFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type chunkType = this.getInputSchema().getType(References.CHUNK);
        OpticFinder sectionsF = chunkType.findField("sections");
        Type sectionType = ((List.ListType)sectionsF.type()).getElement();
        Type biomesType = sectionType.findFieldType("biomes");
        Type blockStatesType = sectionType.findFieldType("block_states");
        return this.makeRule(chunkType, sectionsF, sectionType, biomesType, blockStatesType);
    }

    private <Biomes, BlockStates> TypeRewriteRule makeRule(Type<?> chunkType, OpticFinder<?> sectionsF, Type<?> sectionType, Type<Biomes> biomesType, Type<BlockStates> blockStatesType) {
        Type expectedSectionType = DSL.and((Type)DSL.optional((Type)DSL.field((String)"biomes", biomesType)), (Type)DSL.optional((Type)DSL.field((String)"block_states", blockStatesType)), (Type)DSL.remainderType());
        if (!Objects.equals(sectionType, expectedSectionType)) {
            throw new IllegalStateException(String.valueOf(sectionType) + " did not match " + String.valueOf(expectedSectionType));
        }
        OpticFinder sectionF = DSL.typeFinder((Type)expectedSectionType);
        return this.fixTypeEverywhereTyped("MergeTerrainChunkStatusFix", chunkType, chunk -> {
            boolean targetStatusIntermediate;
            FixedAndStatuses statuses = this.fixStatuses((Typed<?>)chunk);
            chunk = statuses.fixed;
            boolean statusIntermediate = OLD_INTERMEDIATE_TERRAIN_STATUSES.contains(statuses.oldStatus);
            boolean bl = targetStatusIntermediate = statuses.oldTargetStatus != null && OLD_INTERMEDIATE_TERRAIN_STATUSES.contains(statuses.oldTargetStatus);
            if (statusIntermediate || targetStatusIntermediate) {
                boolean onlyRemoveBlocksBelowZero = statuses.oldTargetStatus != null && !targetStatusIntermediate;
                chunk = MergeTerrainChunkStatusFix.removeBlockStates(chunk, sectionsF, sectionF, onlyRemoveBlocksBelowZero);
            }
            return chunk;
        });
    }

    private static <Biomes, BlockStates> Typed<?> removeBlockStates(Typed<?> chunk, OpticFinder<?> sectionsF, OpticFinder<Pair<Either<Biomes, Unit>, Pair<Either<BlockStates, Unit>, Dynamic<?>>>> sectionF, boolean onlyBelowZero) {
        return chunk.update(DSL.remainderFinder(), remainder -> remainder.remove("Heightmaps").remove("blending_data")).updateTyped(sectionsF, sections -> sections.update(sectionF, section -> {
            byte y = ((Dynamic)((Pair)section.getSecond()).getSecond()).get("Y").asByte((byte)0);
            if (y >= 0 && onlyBelowZero) {
                return section;
            }
            return section.mapSecond(blockStatesAndRemainder -> blockStatesAndRemainder.mapFirst(either -> Either.right((Object)Unit.INSTANCE)).mapSecond(sectionRemainder -> sectionRemainder.remove("block_states")));
        }));
    }

    private FixedAndStatuses fixStatuses(Typed<?> chunk) {
        String oldTargetStatus;
        Dynamic remainder = (Dynamic)chunk.getOrCreate(DSL.remainderFinder());
        String oldStatus = MergeTerrainChunkStatusFix.unwrapStatus(remainder.get("Status"));
        Optional maybeBelowZeroRetrogen = (remainder = remainder.set("Status", remainder.createString(MergeTerrainChunkStatusFix.fixStatus(oldStatus)))).get("below_zero_retrogen").result();
        if (maybeBelowZeroRetrogen.isPresent()) {
            Dynamic belowZeroRetrogen = (Dynamic)maybeBelowZeroRetrogen.get();
            oldTargetStatus = MergeTerrainChunkStatusFix.unwrapStatus(belowZeroRetrogen.get("target_status"));
            belowZeroRetrogen = belowZeroRetrogen.set("target_status", remainder.createString(MergeTerrainChunkStatusFix.fixStatus(oldTargetStatus)));
            remainder = remainder.set("below_zero_retrogen", belowZeroRetrogen);
        } else {
            oldTargetStatus = null;
        }
        return new FixedAndStatuses(chunk.set(DSL.remainderFinder(), (Object)remainder), oldStatus, oldTargetStatus);
    }

    private static String unwrapStatus(OptionalDynamic<?> status) {
        return NamespacedSchema.ensureNamespaced(status.asString("minecraft:empty"));
    }

    private static String fixStatus(String status) {
        if (OLD_FINAL_TERRAIN_STATUS.equals(status)) {
            return "minecraft:terrain";
        }
        if (OLD_INTERMEDIATE_TERRAIN_STATUSES.contains(status)) {
            return BEFORE_TERRAIN_STATUS;
        }
        return status;
    }

    private record FixedAndStatuses(Typed<?> fixed, String oldStatus, @Nullable String oldTargetStatus) {
    }
}

