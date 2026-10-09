/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.slot;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.slot.SlotCollection;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.slot.SlotSources;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class CompositeSlotSource
implements SlotSource {
    protected final HolderSet<SlotSource> terms;
    private final Function<LootContext, SlotCollection> compositeSlotSource;

    protected CompositeSlotSource(HolderSet<SlotSource> terms) {
        this.terms = terms;
        this.compositeSlotSource = CompositeSlotSource.group(terms);
    }

    private static Function<LootContext, SlotCollection> group(HolderSet<SlotSource> terms) {
        if (!terms.isBound()) {
            return context -> {
                ArrayList<SlotCollection> collections = new ArrayList<SlotCollection>();
                for (Holder term : terms) {
                    collections.add(((SlotSource)term.value()).provide((LootContext)context));
                }
                return SlotCollection.concat(collections);
            };
        }
        return switch (terms.size()) {
            case 0 -> lootContext -> SlotCollection.EMPTY;
            case 1 -> {
                Holder<SlotSource> term = terms.get(0);
                yield context -> ((SlotSource)term.value()).provide((LootContext)context);
            }
            case 2 -> {
                Holder<SlotSource> first = terms.get(0);
                Holder<SlotSource> second = terms.get(1);
                yield context -> SlotCollection.concat(((SlotSource)first.value()).provide((LootContext)context), ((SlotSource)second.value()).provide((LootContext)context));
            }
            default -> context -> {
                ArrayList<SlotCollection> collections = new ArrayList<SlotCollection>();
                for (Holder term : terms) {
                    collections.add(((SlotSource)term.value()).provide((LootContext)context));
                }
                return SlotCollection.concat(collections);
            };
        };
    }

    protected static <T extends CompositeSlotSource> MapCodec<T> createCodec(Function<HolderSet<SlotSource>, T> factory) {
        return RecordCodecBuilder.mapCodec(i -> i.group((App)SlotSources.LIST_CODEC.fieldOf("terms").forGetter(t -> t.terms)).apply((Applicative)i, factory));
    }

    protected static <T extends CompositeSlotSource> Codec<T> createInlineCodec(Function<HolderSet<SlotSource>, T> factory) {
        return SlotSources.LIST_CODEC.xmap(factory, t -> t.terms);
    }

    public abstract MapCodec<? extends CompositeSlotSource> codec();

    @Override
    public SlotCollection provide(LootContext context) {
        return this.compositeSlotSource.apply(context);
    }

    @Override
    public void validate(ValidationContext context) {
        SlotSource.super.validate(context);
        Validatable.validateHolderSet(context, "terms", this.terms);
    }
}

