/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.FireworkRocketRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionfc;

public class FireworkEntityRenderer
extends EntityRenderer<FireworkRocketEntity, FireworkRocketRenderState> {
    private final ItemModelResolver itemModelResolver;

    public FireworkEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void submit(FireworkRocketRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate((Quaternionfc)camera.orientation);
        if (state.isShotAtAngle) {
            poseStack.rotateDegrees(Axis.ZP, 180.0f);
            poseStack.rotateDegrees(Axis.YP, 180.0f);
            poseStack.rotateDegrees(Axis.XP, 90.0f);
        }
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public FireworkRocketRenderState createRenderState() {
        return new FireworkRocketRenderState();
    }

    @Override
    public void extractRenderState(FireworkRocketEntity entity, FireworkRocketRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isShotAtAngle = entity.isShotAtAngle();
        this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
    }
}

