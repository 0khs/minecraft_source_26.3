/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.EnumMap;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.cushion.CushionModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CushionRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.item.DyeColor;

public class CushionRenderer
extends EntityRenderer<Cushion, CushionRenderState> {
    private static final EnumMap<DyeColor, Identifier> TEXTURES_BY_COLOR = Util.make(new EnumMap(DyeColor.class), textures -> {
        for (DyeColor color : DyeColor.values()) {
            textures.put(color, Identifier.withDefaultNamespace("textures/entity/cushion/" + color.getName() + "_cushion.png"));
        }
    });
    private final CushionModel model;

    public CushionRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CushionModel(context.bakeLayer(ModelLayers.CUSHION));
    }

    @Override
    public void extractRenderState(Cushion cushion, CushionRenderState state, float partialTicks) {
        super.extractRenderState(cushion, state, partialTicks);
        state.direction = Direction.fromYRot(cushion.getYRot());
        state.texture = TEXTURES_BY_COLOR.get(cushion.getColor());
    }

    @Override
    public void submit(CushionRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, 180.0f - state.direction.toYRot());
        poseStack.rotateDegrees(Axis.XP, 180.0f);
        poseStack.translate(0.0, -0.25, 0.0);
        submitNodeCollector.submitModel(this.model, state, poseStack, this.model.renderType(state.texture), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public CushionRenderState createRenderState() {
        return new CushionRenderState();
    }
}

