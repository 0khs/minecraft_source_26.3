/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

public class WolfCollarLayer
extends RenderLayer<WolfRenderState, WolfModel> {
    private static final Identifier WOLF_COLLAR_LOCATION = Identifier.withDefaultNamespace("textures/entity/wolf/wolf_collar.png");
    private static final Identifier WOLF_BABY_COLLAR_LOCATION = Identifier.withDefaultNamespace("textures/entity/wolf/wolf_collar_baby.png");

    public WolfCollarLayer(RenderLayerParent<WolfRenderState, WolfModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, WolfRenderState state, float yRot, float xRot) {
        DyeColor collarColor = state.collarColor;
        if (collarColor == null || state.isInvisible) {
            return;
        }
        int color = collarColor.getTextureDiffuseColor();
        Identifier collarLocation = state.isBaby ? WOLF_BABY_COLLAR_LOCATION : WOLF_COLLAR_LOCATION;
        submitNodeCollector.order(1).submitModel(this.getParentModel(), state, poseStack, RenderTypes.entityCutout(collarLocation), lightCoords, OverlayTexture.NO_OVERLAY, color, (UvMapping)null, state.outlineColor);
    }
}

