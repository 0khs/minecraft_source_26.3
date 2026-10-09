/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loot;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContextAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

@FunctionalInterface
public interface LootTableSubProvider {
    public void run();

    public static interface Factory {
        public LootTableSubProvider create(Context var1);
    }

    public static interface Context
    extends BootstrapContextAccess {
        public Holder.Reference<LootTable> accept(ResourceKey<LootTable> var1, LootTable.Builder var2);
    }
}

