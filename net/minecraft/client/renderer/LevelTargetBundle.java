/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class LevelTargetBundle
implements PostChain.TargetBundle {
    public static final Identifier MAIN_TARGET_ID = PostChain.MAIN_TARGET_ID;
    public static final Identifier ENTITY_OUTLINE_TARGET_ID = Identifier.withDefaultNamespace("entity_outline");
    public static final Set<Identifier> MAIN_TARGETS = Set.of(MAIN_TARGET_ID);
    public static final Set<Identifier> OUTLINE_TARGETS = Set.of(MAIN_TARGET_ID, ENTITY_OUTLINE_TARGET_ID);
    public ResourceHandle<RenderTarget> main = ResourceHandle.invalid();
    public ResourceHandle<RenderTarget> alwaysOnTopDepth = ResourceHandle.invalid();
    public ResourceHandle<RenderTarget> depthBounds = ResourceHandle.invalid();
    public ResourceHandle<RenderTarget> depthBoundsCulled = ResourceHandle.invalid();
    public final List<ResourceHandle<RenderTarget>> transmittance = new ArrayList<ResourceHandle<RenderTarget>>();
    public ResourceHandle<RenderTarget> accumulate = ResourceHandle.invalid();
    public ResourceHandle<RenderTarget> oitCloudDepth = ResourceHandle.invalid();
    public ResourceHandle<RenderTarget> oitTerrainWithWaterPatchDepth = ResourceHandle.invalid();
    public @Nullable ResourceHandle<RenderTarget> entityOutline;

    public LevelTargetBundle() {
        for (int i = 0; i < LevelRenderer.OIT_TRANSMITTANCE_TARGET_COUNT; ++i) {
            this.transmittance.add(ResourceHandle.invalid());
        }
    }

    @Override
    public void replace(Identifier id, ResourceHandle<RenderTarget> handle) {
        if (id.equals(MAIN_TARGET_ID)) {
            this.main = handle;
        } else if (id.equals(ENTITY_OUTLINE_TARGET_ID)) {
            this.entityOutline = handle;
        } else {
            throw new IllegalArgumentException("No target with id " + String.valueOf(id));
        }
    }

    @Override
    public @Nullable ResourceHandle<RenderTarget> get(Identifier id) {
        if (id.equals(MAIN_TARGET_ID)) {
            return this.main;
        }
        if (id.equals(ENTITY_OUTLINE_TARGET_ID)) {
            return this.entityOutline;
        }
        return null;
    }

    public void clear() {
        this.main = ResourceHandle.invalid();
        this.entityOutline = null;
    }
}

