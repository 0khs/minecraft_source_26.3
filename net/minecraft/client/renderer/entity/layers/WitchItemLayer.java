/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.state.WitchRenderState;

public class WitchItemLayer
extends CrossedArmsItemLayer<WitchRenderState, WitchModel> {
    public WitchItemLayer(RenderLayerParent<WitchRenderState, WitchModel> renderer) {
        super(renderer);
    }

    @Override
    protected void applyTranslation(WitchRenderState state, PoseStack poseStack) {
        if (state.isHoldingPotion) {
            ((WitchModel)this.getParentModel()).root().translateAndRotate(poseStack);
            ((WitchModel)this.getParentModel()).translateToHead(poseStack);
            ((WitchModel)this.getParentModel()).getNose().translateAndRotate(poseStack);
            poseStack.translate(0.0625f, 0.25f, 0.0f);
            poseStack.rotateDegrees(Axis.ZP, 180.0f);
            poseStack.rotateDegrees(Axis.XP, 140.0f);
            poseStack.rotateDegrees(Axis.ZP, 10.0f);
            poseStack.rotateDegrees(Axis.XP, 180.0f);
            return;
        }
        super.applyTranslation(state, poseStack);
    }
}

