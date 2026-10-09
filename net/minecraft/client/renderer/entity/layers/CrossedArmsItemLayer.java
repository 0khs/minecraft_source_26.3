/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class CrossedArmsItemLayer<S extends HoldingEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    public CrossedArmsItemLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot) {
        ItemStackRenderState item = ((HoldingEntityRenderState)state).heldItem;
        if (item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.applyTranslation(state, poseStack);
        item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, ((HoldingEntityRenderState)state).outlineColor);
        poseStack.popPose();
    }

    protected void applyTranslation(S state, PoseStack poseStack) {
        ((VillagerLikeModel)this.getParentModel()).translateToArms(state, poseStack);
        poseStack.rotate(Axis.XP, 0.75f);
        poseStack.scale(1.07f, 1.07f, 1.07f);
        poseStack.translate(0.0f, 0.13f, -0.34f);
        poseStack.rotate(Axis.XP, (float)Math.PI);
    }
}

