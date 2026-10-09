/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;

public interface SubmitNodeCollector
extends OrderedSubmitNodeCollector {
    public OrderedSubmitNodeCollector order(int var1);

    public static interface CustomGeometryRenderer {
        public void render(PoseStack.Pose var1, VertexConsumer var2);
    }
}

