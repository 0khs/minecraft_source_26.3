/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.BatchableSubmit;
import net.minecraft.client.renderer.rendertype.RenderType;

public class CustomFeatureRenderer
extends RenderTypeFeatureRenderer<Submit> {
    public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Custom");

    @Override
    protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
        for (Submit submit : submits) {
            VertexConsumer builder = this.getVertexBuilder(submit.renderType());
            submit.customGeometryRenderer().render(submit.pose(), builder);
        }
    }

    public record Submit(PoseStack.Pose pose, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer) implements BatchableSubmit
    {
        @Override
        public Object batchKey() {
            return this.renderType;
        }

        public FeatureRendererType<Submit> featureType() {
            return TYPE;
        }
    }
}

