/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerAdvancementManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<Identifier, AdvancementHolder> advancements;
    private final AdvancementTree tree;

    public ServerAdvancementManager(HolderLookup.Provider registries) {
        HolderGetter advancements = registries.lookupOrThrow(Registries.ADVANCEMENT);
        ImmutableMap.Builder builder = ImmutableMap.builder();
        advancements.listElements().forEach(advancement -> {
            ServerAdvancementManager.validate(registries, advancement);
            builder.put((Object)advancement.key().identifier(), (Object)new AdvancementHolder(advancement.key().identifier(), (Advancement)advancement.value()));
        });
        this.advancements = builder.buildOrThrow();
        AdvancementTree tree = new AdvancementTree();
        tree.addAll(this.advancements.values());
        tree.repositionNodes();
        this.tree = tree;
    }

    private static void validate(HolderLookup.Provider registries, Holder.Reference<Advancement> advancement) {
        ProblemReporter.Collector problemCollector = new ProblemReporter.Collector();
        advancement.value().validate(problemCollector, registries);
        if (!problemCollector.isEmpty()) {
            LOGGER.warn("Found validation problems in advancement {}: \n{}", (Object)advancement.key().identifier(), (Object)problemCollector.getReport());
        }
    }

    public @Nullable AdvancementHolder get(Identifier id) {
        return this.advancements.get(id);
    }

    public AdvancementTree tree() {
        return this.tree;
    }

    public Collection<AdvancementHolder> getAllAdvancements() {
        return this.advancements.values();
    }
}

