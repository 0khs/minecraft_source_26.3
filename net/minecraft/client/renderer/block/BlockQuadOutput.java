/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.resources.model.geometry.BakedQuad;

@FunctionalInterface
public interface BlockQuadOutput {
    public void put(float var1, float var2, float var3, BakedQuad var4, QuadInstance var5);
}

