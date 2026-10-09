/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P5
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class ExpandableContainerBase
extends UniformContainerBase {
    protected final boolean expand;

    protected static <T extends ExpandableContainerBase> Products.P5<RecordCodecBuilder.Mu<T>, Boolean, Integer, Integer, Optional<Holder<LootItemCondition>>, Optional<Holder<LootItemFunction>>> expandableFields(RecordCodecBuilder.Instance<T> i) {
        return i.group((App)Codec.BOOL.optionalFieldOf("expand", (Object)false).forGetter(e -> e.expand)).and(ExpandableContainerBase.uniformFields(i));
    }

    protected ExpandableContainerBase(boolean expand, int weight, int quality, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(weight, quality, condition, modifier);
        this.expand = expand;
    }

    public abstract MapCodec<? extends ExpandableContainerBase> codec();

    @Override
    public final boolean expandRaw(LootContext context, Consumer<LootPoolEntry> output) {
        return this.expand ? this.addExpandedEntries(output) : this.addUnexpandedEntry(output);
    }

    protected abstract boolean addExpandedEntries(Consumer<LootPoolEntry> var1);

    protected abstract boolean addUnexpandedEntry(Consumer<LootPoolEntry> var1);
}

