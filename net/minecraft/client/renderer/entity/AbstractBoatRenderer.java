/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public abstract class AbstractBoatRenderer
extends EntityRenderer<AbstractBoat, BoatRenderState> {
    protected final Identifier texture;

    public AbstractBoatRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context);
        this.texture = texture;
        this.shadowRadius = 0.8f;
    }

    @Override
    public void submit(BoatRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.375f, 0.0f);
        poseStack.rotateDegrees(Axis.YP, 180.0f - state.yRot);
        float hurt = state.hurtTime;
        if (hurt > 0.0f) {
            poseStack.rotateDegrees(Axis.XP, Mth.sin(hurt) * hurt * state.damageTime / 10.0f * (float)state.hurtDir);
        }
        if (!state.isUnderWater && !Mth.equal(state.bubbleAngle, 0.0f)) {
            poseStack.rotate((Quaternionfc)new Quaternionf().setAngleAxis(state.bubbleAngle * ((float)Math.PI / 180), 1.0f, 0.0f, 1.0f));
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.rotateDegrees(Axis.YP, 90.0f);
        submitNodeCollector.submitModel(this.model(), state, poseStack, this.texture, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        this.submitTypeAdditions(state, poseStack, submitNodeCollector, state.lightCoords);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    protected void submitTypeAdditions(BoatRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
    }

    protected abstract EntityModel<BoatRenderState> model();

    @Override
    public BoatRenderState createRenderState() {
        return new BoatRenderState();
    }

    @Override
    public void extractRenderState(AbstractBoat entity, BoatRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.hurtTime = (float)entity.getHurtTime() - partialTicks;
        state.hurtDir = entity.getHurtDir();
        state.damageTime = Math.max(entity.getDamage() - partialTicks, 0.0f);
        state.bubbleAngle = entity.getBubbleAngle(partialTicks);
        state.isUnderWater = entity.isUnderWater();
        state.rowingTimeLeft = entity.getRowingTime(0, partialTicks);
        state.rowingTimeRight = entity.getRowingTime(1, partialTicks);
    }
}

