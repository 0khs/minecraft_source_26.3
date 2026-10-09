/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class IronGolemFlowerLayer
extends RenderLayer<IronGolemRenderState, IronGolemModel> {
    public IronGolemFlowerLayer(RenderLayerParent<IronGolemRenderState, IronGolemModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, IronGolemRenderState state, float yRot, float xRot) {
        if (state.flowerBlock.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        ModelPart arm = ((IronGolemModel)this.getParentModel()).getFlowerHoldingArm();
        arm.translateAndRotate(poseStack);
        poseStack.translate(-1.1875f, 1.0625f, -0.9375f);
        poseStack.translate(0.5f, 0.5f, 0.5f);
        float s = 0.5f;
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.rotateDegrees(Axis.XP, -90.0f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        state.flowerBlock.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}

