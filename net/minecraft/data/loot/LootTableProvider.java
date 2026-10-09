/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 */
package net.minecraft.data.loot;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableProvider
implements SingleRegistryBootstrap<LootTable> {
    private final Set<ResourceKey<LootTable>> requiredTables;
    private final List<SubProviderEntry> subProviders;

    public LootTableProvider(Set<ResourceKey<LootTable>> requiredTables, List<SubProviderEntry> subProviders) {
        this.subProviders = subProviders;
        this.requiredTables = requiredTables;
    }

    @Override
    public void run(BootstrapContext<LootTable> context) {
        Object2ObjectOpenHashMap randomSequenceSeeds = new Object2ObjectOpenHashMap();
        HolderGetter<LootTable> lootTables = context.lookup(Registries.LOOT_TABLE);
        this.requiredTables.forEach(lootTables::get);
        this.subProviders.forEach(arg_0 -> this.lambda$run$0((Map)randomSequenceSeeds, context, arg_0));
    }

    private static Identifier sequenceIdForLootTable(ResourceKey<LootTable> id) {
        return id.identifier();
    }

    private /* synthetic */ void lambda$run$0(final Map randomSequenceSeeds, final BootstrapContext context, final SubProviderEntry subProvider) {
        subProvider.bootstrap().create(new LootTableSubProvider.Context(){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public Holder.Reference<LootTable> accept(ResourceKey<LootTable> key, LootTable.Builder lootTable) {
                Identifier sequenceId = LootTableProvider.sequenceIdForLootTable(key);
                Identifier previous = randomSequenceSeeds.put(RandomSequence.seedForKey(sequenceId), sequenceId);
                if (previous != null) {
                    Util.logAndPauseIfInIde("Loot table random sequence seed collision on " + String.valueOf(previous) + " and " + String.valueOf(key.identifier()));
                }
                LootTable table = lootTable.setRandomSequence(sequenceId).setParamSet(subProvider.paramSet).build();
                return context.register(key, table);
            }

            @Override
            public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
                return context.lookup(key);
            }

            @Override
            @Deprecated
            public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
                return context.listContextElements(key);
            }
        }).run();
    }

    public record SubProviderEntry(LootTableSubProvider.Factory bootstrap, ContextKeySet paramSet) {
    }
}

