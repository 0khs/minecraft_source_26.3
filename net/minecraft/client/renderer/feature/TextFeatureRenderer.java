/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class TextFeatureRenderer
extends RenderTypeFeatureRenderer<Submit> {
    public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Text");

    @Override
    protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
        Font font = context.font();
        GlyphRenderer glyphRenderer = new GlyphRenderer(this);
        block4: for (Submit submit : submits) {
            Content content;
            glyphRenderer.pose.set(submit.pose());
            glyphRenderer.lightCoords = submit.lightCoords();
            glyphRenderer.displayMode = submit.displayMode();
            Objects.requireNonNull(submit.content());
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Content.Text.class, Content.StandaloneBackground.class}, (Content)content, n)) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    Content.Text text = (Content.Text)content;
                    TextFeatureRenderer.renderText(font, glyphRenderer, text);
                    continue block4;
                }
                case 1: 
            }
            Content.StandaloneBackground standaloneBackground = (Content.StandaloneBackground)content;
            glyphRenderer.acceptRenderable(font.prepareBackground(standaloneBackground.x0(), standaloneBackground.y0(), standaloneBackground.x1(), standaloneBackground.y1(), standaloneBackground.color()));
        }
    }

    private static void renderText(Font font, GlyphRenderer glyphRenderer, Content.Text content) {
        if (content.outlineColor() == 0) {
            Font.PreparedText text = font.prepareText(content.string(), content.x(), content.y(), content.color(), content.dropShadow(), false, content.backgroundColor());
            text.visit(glyphRenderer);
        } else {
            Font.PreparedText outline = font.prepare8xTextOutline(content.string(), content.x(), content.y(), content.outlineColor());
            Font.PreparedText text = font.prepareText(content.string(), content.x(), content.y(), content.color(), false, false, 0);
            glyphRenderer.displayMode = Font.DisplayMode.NORMAL;
            outline.visit(glyphRenderer);
            glyphRenderer.displayMode = Font.DisplayMode.POLYGON_OFFSET;
            text.visit(glyphRenderer);
        }
    }

    private class GlyphRenderer
    implements Font.GlyphVisitor {
        private final Matrix4f pose;
        private int lightCoords;
        private Font.DisplayMode displayMode;
        final /* synthetic */ TextFeatureRenderer this$0;

        private GlyphRenderer(TextFeatureRenderer textFeatureRenderer) {
            TextFeatureRenderer textFeatureRenderer2 = textFeatureRenderer;
            Objects.requireNonNull(textFeatureRenderer2);
            this.this$0 = textFeatureRenderer2;
            this.pose = new Matrix4f();
            this.lightCoords = 0xF000F0;
            this.displayMode = Font.DisplayMode.NORMAL;
        }

        @Override
        public void acceptRenderable(TextRenderable renderable) {
            VertexConsumer builder = this.this$0.getVertexBuilder(renderable.renderType(this.displayMode));
            renderable.render((Matrix4fc)this.pose, builder, this.lightCoords, false);
        }
    }

    public record Submit(Matrix4fc pose, Font.DisplayMode displayMode, int lightCoords, Content content) implements TranslucentSubmit
    {
        @Override
        public float distanceToCameraSq() {
            return TranslucentSubmit.computeDistanceToCameraSq(this.pose);
        }

        public FeatureRendererType<Submit> featureType() {
            return TYPE;
        }
    }

    public static sealed interface Content {

        public record StandaloneBackground(float x0, float y0, float x1, float y1, int color) implements Content
        {
        }

        public record Text(float x, float y, FormattedCharSequence string, boolean dropShadow, int color, int backgroundColor, int outlineColor) implements Content
        {
        }
    }
}

