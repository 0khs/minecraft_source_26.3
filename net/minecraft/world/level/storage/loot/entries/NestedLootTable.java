/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.ExpandableContainerBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class NestedLootTable
extends ExpandableContainerBase {
    public static final MapCodec<NestedLootTable> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)LootTable.LIST_CODEC.fieldOf("value").forGetter(e -> e.value)).and(NestedLootTable.expandableFields(i)).apply((Applicative)i, NestedLootTable::new));
    private final HolderSet<LootTable> value;

    private NestedLootTable(HolderSet<LootTable> value, boolean expand, int weight, int quality, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(expand, weight, quality, condition, modifier);
        this.value = value;
    }

    public MapCodec<NestedLootTable> codec() {
        return MAP_CODEC;
    }

    @Override
    protected boolean addExpandedEntries(Consumer<LootPoolEntry> output) {
        for (final Holder holder : this.value) {
            output.accept(new UniformContainerBase.EntryBase(this){
                {
                    Objects.requireNonNull(this$0);
                    super(this$0);
                }

                @Override
                public void createItemStack(Consumer<ItemStack> output, LootContext context) {
                    ((LootTable)holder.value()).getRandomItemsRaw(context, output);
                }
            });
        }
        return true;
    }

    @Override
    protected boolean addUnexpandedEntry(Consumer<LootPoolEntry> output) {
        output.accept(new UniformContainerBase.EntryBase(this){
            final /* synthetic */ NestedLootTable this$0;
            {
                NestedLootTable nestedLootTable = this$0;
                Objects.requireNonNull(nestedLootTable);
                this.this$0 = nestedLootTable;
                super(this$0);
            }

            @Override
            public void createItemStack(Consumer<ItemStack> output, LootContext context) {
                this.this$0.value.forEach(t -> ((LootTable)t.value()).getRandomItemsRaw(context, output));
            }
        });
        return true;
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        Validatable.validateHolderSet(context, "value", this.value);
    }

    public static UniformContainerBase.Builder<?> lootTableReference(Holder<LootTable> table) {
        return NestedLootTable.simpleBuilder((weight, quality, conditions, functions) -> new NestedLootTable(HolderSet.direct(table), false, weight, quality, conditions, functions));
    }

    public static UniformContainerBase.Builder<?> inlineLootTable(LootTable table) {
        return NestedLootTable.lootTableReference(Holder.direct(table));
    }
}

