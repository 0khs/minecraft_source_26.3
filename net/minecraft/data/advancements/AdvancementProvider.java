/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.advancements;

import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;

public class AdvancementProvider
implements SingleRegistryBootstrap<Advancement> {
    private final List<AdvancementSubProvider.Factory> subProviders;

    public AdvancementProvider(List<AdvancementSubProvider.Factory> subProviders) {
        this.subProviders = subProviders;
    }

    @Override
    public void run(BootstrapContext<Advancement> output) {
        for (AdvancementSubProvider.Factory subProvider : this.subProviders) {
            subProvider.create(output).generate();
        }
    }
}

