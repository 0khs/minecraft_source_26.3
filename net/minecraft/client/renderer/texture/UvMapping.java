/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SpriteCoordinateExpander;

public interface UvMapping {
    public float getU(float var1);

    public float getV(float var1);

    default public VertexConsumer wrap(VertexConsumer buffer) {
        return new SpriteCoordinateExpander(buffer, this);
    }
}

