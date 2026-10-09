/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.BatchableSubmit;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public class ModelFeatureRenderer
extends RenderTypeFeatureRenderer<Submit<?>> {
    public static final FeatureRendererType<Submit<?>> TYPE = FeatureRendererType.create("Entity Model");
    private final PoseStack poseStack = new PoseStack();

    @Override
    protected void buildGroup(FeatureFrameContext context, List<Submit<?>> submits) {
        for (Submit<?> submit : submits) {
            this.prepareModel(submit);
        }
    }

    private <S> void prepareModel(Submit<S> submit) {
        this.poseStack.last().set(submit.pose());
        VertexConsumer buffer = this.getVertexBuilder(submit.renderType());
        if (submit.sheetedDecalPose() != null) {
            buffer = new SheetedDecalTextureGenerator(buffer, submit.sheetedDecalPose(), 1.0f);
        } else if (submit.uvMapping() != null) {
            buffer = submit.uvMapping().wrap(buffer);
        }
        Model<S> model = submit.model();
        model.setupAnim(submit.state());
        model.renderToBuffer(this.poseStack, buffer, submit.lightCoords(), submit.overlayCoords(), submit.tintedColor());
    }

    public record Submit<S>(RenderType renderType, PoseStack.Pose pose, Model<? super S> model, S state, int lightCoords, int overlayCoords, int tintedColor, @Nullable UvMapping uvMapping, @Nullable PoseStack.Pose sheetedDecalPose) implements BatchableSubmit,
    TranslucentSubmit
    {
        @Override
        public Object batchKey() {
            return this.renderType;
        }

        @Override
        public float distanceToCameraSq() {
            return TranslucentSubmit.computeDistanceToCameraSq((Matrix4fc)this.pose.pose());
        }

        public FeatureRendererType<Submit<S>> featureType() {
            return TYPE;
        }
    }

    public record CrumblingOverlay(int progress, PoseStack.Pose cameraPose) {
    }
}

