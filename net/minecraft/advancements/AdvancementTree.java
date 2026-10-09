/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.advancements;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class AdvancementTree {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<Identifier, AdvancementNode> nodes = new Object2ObjectLinkedOpenHashMap();

    private void remove(AdvancementNode node) {
        for (AdvancementNode child : node.children()) {
            this.remove(child);
        }
        LOGGER.info("Forgot about advancement {}", (Object)node.holder());
        this.nodes.remove(node.holder().id());
    }

    public void remove(Set<Identifier> ids) {
        for (Identifier id : ids) {
            AdvancementNode advancement = this.nodes.get(id);
            if (advancement == null) {
                LOGGER.warn("Told to remove advancement {} but I don't know what that is", (Object)id);
                continue;
            }
            this.remove(advancement);
        }
    }

    public void addAll(Iterable<AdvancementHolder> advancements) {
        ArrayList advancementsToAdd = Lists.newArrayList(advancements);
        while (!advancementsToAdd.isEmpty()) {
            if (advancementsToAdd.removeIf(this::tryInsert)) continue;
            LOGGER.error("Couldn't load advancements: {}", (Object)advancementsToAdd);
            break;
        }
        LOGGER.info("Loaded {} advancements", (Object)this.nodes.size());
    }

    private boolean tryInsert(AdvancementHolder holder) {
        AdvancementNode parentNode;
        Identifier parentId = holder.value().parent().orElse(null);
        if (parentId != null) {
            parentNode = this.nodes.get(parentId);
            if (parentNode == null) {
                return false;
            }
        } else {
            parentNode = null;
        }
        AdvancementNode node = new AdvancementNode(holder, parentNode);
        if (parentNode != null) {
            parentNode.addChild(node);
        }
        this.nodes.put(holder.id(), node);
        return true;
    }

    public void clear() {
        this.nodes.clear();
    }

    public Iterable<AdvancementNode> roots() {
        return Iterables.filter(this.nodes(), AdvancementNode::isRoot);
    }

    public Iterable<AdvancementNode> tasks() {
        return Iterables.filter(this.nodes(), AdvancementNode::isTask);
    }

    public Collection<AdvancementNode> nodes() {
        return this.nodes.values();
    }

    public @Nullable AdvancementNode get(Identifier id) {
        return this.nodes.get(id);
    }

    public @Nullable AdvancementNode get(AdvancementHolder advancement) {
        return this.nodes.get(advancement.id());
    }

    public void repositionNodes() {
        for (AdvancementNode root : this.roots()) {
            if (!root.holder().value().display().isPresent()) continue;
            TreeNodePosition.run(root);
        }
    }
}

