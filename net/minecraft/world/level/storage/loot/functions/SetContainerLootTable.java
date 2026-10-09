/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable
extends LootItemConditionalFunction {
    public static final Codec<Holder.Reference<LootTable>> ID_ONLY_CODEC = LootTable.CODEC.comapFlatMap(holder -> {
        DataResult dataResult;
        if (holder instanceof Holder.Reference) {
            Holder.Reference tag = (Holder.Reference)holder;
            dataResult = DataResult.success((Object)tag);
        } else {
            dataResult = DataResult.error(() -> "Only tag names supported");
        }
        return dataResult;
    }, holder -> holder);
    public static final MapCodec<SetContainerLootTable> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> SetContainerLootTable.commonFields(i).and(i.group((App)ID_ONLY_CODEC.fieldOf("loot_table_id").forGetter(f -> f.lootTableId), (App)Codec.LONG.optionalFieldOf("seed", (Object)0L).forGetter(f -> f.seed))).apply((Applicative)i, SetContainerLootTable::new));
    private final Holder.Reference<LootTable> lootTableId;
    private final long seed;

    private SetContainerLootTable(Optional<Holder<LootItemCondition>> condition, Holder.Reference<LootTable> lootTableId, long seed) {
        super(condition);
        this.lootTableId = lootTableId;
        this.seed = seed;
    }

    public MapCodec<SetContainerLootTable> codec() {
        return MAP_CODEC;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext context) {
        if (itemStack.isEmpty()) {
            return itemStack;
        }
        itemStack.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.lootTableId.key(), this.seed));
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> withLootTable(Holder.Reference<LootTable> value) {
        return SetContainerLootTable.simpleBuilder(conditions -> new SetContainerLootTable((Optional<Holder<LootItemCondition>>)conditions, value, 0L));
    }

    public static LootItemConditionalFunction.Builder<?> withLootTable(Holder.Reference<LootTable> value, long seed) {
        return SetContainerLootTable.simpleBuilder(conditions -> new SetContainerLootTable((Optional<Holder<LootItemCondition>>)conditions, value, seed));
    }
}

