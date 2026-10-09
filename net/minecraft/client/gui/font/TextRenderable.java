/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.gui.font;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.ActiveArea;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4fc;

public interface TextRenderable {
    public void render(Matrix4fc var1, VertexConsumer var2, int var3, boolean var4);

    public RenderType renderType(Font.DisplayMode var1);

    public GpuTextureView textureView();

    public RenderPipeline guiPipeline();

    public float left();

    public float top();

    public float right();

    public float bottom();

    public static interface Styled
    extends TextRenderable,
    ActiveArea {
        @Override
        default public float activeLeft() {
            return this.left();
        }

        @Override
        default public float activeTop() {
            return this.top();
        }

        @Override
        default public float activeRight() {
            return this.right();
        }

        @Override
        default public float activeBottom() {
            return this.bottom();
        }
    }
}

