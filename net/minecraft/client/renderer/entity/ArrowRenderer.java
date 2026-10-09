/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.projectile.ArrowModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

public abstract class ArrowRenderer<T extends AbstractArrow, S extends ArrowRenderState>
extends EntityRenderer<T, S> {
    private final ArrowModel model;

    public ArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ArrowModel(context.bakeLayer(ModelLayers.ARROW));
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, ((ArrowRenderState)state).yRot - 90.0f);
        poseStack.rotateDegrees(Axis.ZP, ((ArrowRenderState)state).xRot);
        submitNodeCollector.submitModel(this.model, state, poseStack, this.getTextureLocation(state), ((ArrowRenderState)state).lightCoords, OverlayTexture.NO_OVERLAY, ((ArrowRenderState)state).outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    protected abstract Identifier getTextureLocation(S var1);

    @Override
    public void extractRenderState(T entity, S state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ((ArrowRenderState)state).xRot = ((Entity)entity).getXRot(partialTicks);
        ((ArrowRenderState)state).yRot = ((Entity)entity).getYRot(partialTicks);
        ((ArrowRenderState)state).shake = (float)((AbstractArrow)entity).shakeTime - partialTicks;
    }
}

