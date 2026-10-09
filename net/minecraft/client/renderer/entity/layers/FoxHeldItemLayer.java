/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.fox.FoxModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class FoxHeldItemLayer
extends RenderLayer<FoxRenderState, FoxModel> {
    public FoxHeldItemLayer(RenderLayerParent<FoxRenderState, FoxModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, FoxRenderState state, float yRot, float xRot) {
        ItemStackRenderState item = state.heldItem;
        if (item.isEmpty()) {
            return;
        }
        boolean sleeping = state.isSleeping;
        boolean isBaby = state.isBaby;
        poseStack.pushPose();
        poseStack.translate(((FoxModel)this.getParentModel()).head.x / 16.0f, ((FoxModel)this.getParentModel()).head.y / 16.0f, ((FoxModel)this.getParentModel()).head.z / 16.0f);
        if (isBaby) {
            float hs = 0.75f;
            poseStack.scale(0.75f, 0.75f, 0.75f);
        }
        poseStack.rotate(Axis.ZP, state.headRollAngle);
        poseStack.rotateDegrees(Axis.YP, yRot);
        poseStack.rotateDegrees(Axis.XP, xRot);
        if (state.isBaby) {
            if (sleeping) {
                poseStack.translate(0.4f, 0.26f, 0.15f);
            } else {
                poseStack.translate(0.06f, 0.26f, -0.5f);
            }
        } else if (sleeping) {
            poseStack.translate(0.46f, 0.26f, 0.22f);
        } else {
            poseStack.translate(0.06f, 0.27f, -0.5f);
        }
        poseStack.rotateDegrees(Axis.XP, 90.0f);
        if (sleeping) {
            poseStack.rotateDegrees(Axis.ZP, 90.0f);
        }
        item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}

