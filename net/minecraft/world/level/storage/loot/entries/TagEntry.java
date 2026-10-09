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
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.ExpandableContainerBase;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry
extends ExpandableContainerBase {
    public static final MapCodec<TagEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)RegistryCodecs.holderSet(Registries.ITEM).fieldOf("items").forGetter(e -> e.tag)).and(TagEntry.expandableFields(i)).apply((Applicative)i, TagEntry::new));
    private final HolderSet<Item> tag;

    private TagEntry(HolderSet<Item> tag, boolean expand, int weight, int quality, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(expand, weight, quality, condition, modifier);
        this.tag = tag;
    }

    public MapCodec<TagEntry> codec() {
        return MAP_CODEC;
    }

    @Override
    protected boolean addExpandedEntries(Consumer<LootPoolEntry> output) {
        for (final Holder holder : this.tag) {
            output.accept(new UniformContainerBase.EntryBase(this){
                {
                    Objects.requireNonNull(this$0);
                    super(this$0);
                }

                @Override
                public void createItemStack(Consumer<ItemStack> output, LootContext context) {
                    output.accept(new ItemStack(holder));
                }
            });
        }
        return true;
    }

    @Override
    protected boolean addUnexpandedEntry(Consumer<LootPoolEntry> output) {
        output.accept(new UniformContainerBase.EntryBase(this){
            final /* synthetic */ TagEntry this$0;
            {
                TagEntry tagEntry = this$0;
                Objects.requireNonNull(tagEntry);
                this.this$0 = tagEntry;
                super(this$0);
            }

            @Override
            public void createItemStack(Consumer<ItemStack> output, LootContext context) {
                this.this$0.tag.forEach(item -> output.accept(new ItemStack((Holder<Item>)item)));
            }
        });
        return true;
    }

    public static UniformContainerBase.Builder<?> tagContents(HolderSet<Item> tag) {
        return TagEntry.simpleBuilder((weight, quality, conditions, functions) -> new TagEntry(tag, false, weight, quality, conditions, functions));
    }

    public static UniformContainerBase.Builder<?> expandTag(HolderSet<Item> tag) {
        return TagEntry.simpleBuilder((weight, quality, conditions, functions) -> new TagEntry(tag, true, weight, quality, conditions, functions));
    }
}

