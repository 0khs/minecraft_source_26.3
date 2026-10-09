/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.SingleEntryContainerBase;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EmptyLootItem
extends SingleEntryContainerBase {
    public static final MapCodec<EmptyLootItem> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> EmptyLootItem.uniformFields(i).apply((Applicative)i, EmptyLootItem::new));

    private EmptyLootItem(int weight, int quality, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(weight, quality, condition, modifier);
    }

    public MapCodec<EmptyLootItem> codec() {
        return MAP_CODEC;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> output, LootContext context) {
    }

    public static UniformContainerBase.Builder<?> emptyItem() {
        return EmptyLootItem.simpleBuilder(EmptyLootItem::new);
    }
}

