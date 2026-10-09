/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class SingleEntryContainerBase
extends UniformContainerBase {
    private final LootPoolEntry entry = new UniformContainerBase.EntryBase(this){
        final /* synthetic */ SingleEntryContainerBase this$0;
        {
            SingleEntryContainerBase singleEntryContainerBase = this$0;
            Objects.requireNonNull(singleEntryContainerBase);
            this.this$0 = singleEntryContainerBase;
            super(this$0);
        }

        @Override
        public void createItemStack(Consumer<ItemStack> output, LootContext context) {
            this.this$0.createItemStack(output, context);
        }
    };

    protected SingleEntryContainerBase(int weight, int quality, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(weight, quality, condition, modifier);
    }

    public abstract MapCodec<? extends SingleEntryContainerBase> codec();

    protected abstract void createItemStack(Consumer<ItemStack> var1, LootContext var2);

    @Override
    public final boolean expandRaw(LootContext context, Consumer<LootPoolEntry> output) {
        output.accept(this.entry);
        return true;
    }
}

