/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.feature;

import com.mojang.renderpearl.api.commands.RenderPass;
import java.util.List;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.oit.OitStage;
import org.jspecify.annotations.Nullable;

public interface FeatureRenderer<Submit extends SubmitNode>
extends AutoCloseable {
    default public void beginPrepare(FeatureFrameContext context) {
    }

    public void prepareGroup(FeatureFrameContext var1, List<Submit> var2, boolean var3);

    default public void finishPrepare(FeatureFrameContext context) {
    }

    public void executeGroup(FeatureFrameContext var1, @Nullable OitStage var2, RenderPass var3, int var4, List<Submit> var5, boolean var6);

    default public void finishExecute(FeatureFrameContext context) {
    }

    @Override
    default public void close() {
    }
}

