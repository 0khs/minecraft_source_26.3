/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.Iterables;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ClientAdvancements {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    private final WorldSessionTelemetryManager telemetryManager;
    private final AdvancementTree tree = new AdvancementTree();
    private final Map<AdvancementHolder, AdvancementProgress> progress = new Object2ObjectOpenHashMap();
    private @Nullable Listener listener;
    private @Nullable AdvancementHolder selectedTab;

    public ClientAdvancements(Minecraft minecraft, WorldSessionTelemetryManager telemetryManager) {
        this.minecraft = minecraft;
        this.telemetryManager = telemetryManager;
    }

    public void update(ClientboundUpdateAdvancementsPacket packet) {
        AdvancementNode node;
        if (packet.shouldReset()) {
            this.tree.clear();
            this.progress.clear();
            if (this.listener != null) {
                this.listener.onAdvancementsCleared();
            }
        }
        this.tree.remove(packet.removed());
        this.tree.addAll(Iterables.transform(packet.added(), ClientboundUpdateAdvancementsPacket.PositionedAdvancement::advancement));
        for (ClientboundUpdateAdvancementsPacket.PositionedAdvancement positionedAdvancement : packet.added()) {
            node = this.tree.get(positionedAdvancement.advancement());
            if (node == null) continue;
            node.setLocation(positionedAdvancement.x(), positionedAdvancement.y());
        }
        for (Map.Entry entry : packet.progress().entrySet()) {
            node = this.tree.get((Identifier)entry.getKey());
            if (node != null) {
                AdvancementProgress progress = (AdvancementProgress)entry.getValue();
                progress.update(node.advancement().requirements());
                this.progress.put(node.holder(), progress);
                if (packet.shouldReset() || !progress.isDone()) continue;
                if (this.minecraft.level != null) {
                    this.telemetryManager.onAdvancementDone(this.minecraft.level, node.holder());
                }
                Optional<DisplayInfo> display = node.advancement().display();
                if (!packet.showAdvancements() || !display.isPresent() || !display.get().showToast()) continue;
                this.minecraft.gui.toastManager().addToast(new AdvancementToast(node.holder()));
                continue;
            }
            LOGGER.warn("Server informed client about progress for unknown advancement {}", entry.getKey());
        }
        if (this.listener != null) {
            this.listener.onAdvancementsUpdated();
        }
    }

    public Map<AdvancementHolder, AdvancementProgress> progress() {
        return this.progress;
    }

    public AdvancementTree tree() {
        return this.tree;
    }

    public void setSelectedTab(@Nullable AdvancementHolder selectedTab, boolean tellServer) {
        ClientPacketListener connection = this.minecraft.getConnection();
        if (connection != null && selectedTab != null && tellServer) {
            connection.send(ServerboundSeenAdvancementsPacket.openedTab(selectedTab));
        }
        if (this.selectedTab != selectedTab) {
            this.selectedTab = selectedTab;
            if (this.listener != null) {
                this.listener.onSelectedTabChanged(selectedTab);
            }
        }
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onAdvancementsUpdated();
            listener.onSelectedTabChanged(this.selectedTab);
        }
    }

    public @Nullable AdvancementHolder get(Identifier id) {
        AdvancementNode node = this.tree.get(id);
        return node != null ? node.holder() : null;
    }

    public static interface Listener {
        public void onAdvancementsUpdated();

        public void onAdvancementsCleared();

        public void onSelectedTabChanged(@Nullable AdvancementHolder var1);
    }
}

