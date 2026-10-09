/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class ScreenEffectRenderer {
    private static final Identifier UNDERWATER_LOCATION = Identifier.withDefaultNamespace("textures/misc/underwater.png");
    private final GameRenderer gameRenderer;
    private final SpriteGetter sprites;

    public ScreenEffectRenderer(GameRenderer gameRenderer, SpriteGetter sprites) {
        this.gameRenderer = gameRenderer;
        this.sprites = sprites;
    }

    public void submit(float partialTicks, SubmitNodeCollector submitNodeCollector, PlayerRenderState playerRenderState, CameraRenderState cameraRenderState, boolean hideGui) {
        PoseStack poseStack = new PoseStack();
        AvatarRenderState avatarRenderState = playerRenderState.avatarRenderState;
        if (!cameraRenderState.entityRenderState.isSleeping && playerRenderState.hasPlayer && avatarRenderState != null) {
            if (playerRenderState.blockOverlay != null) {
                ScreenEffectRenderer.submitBlockSprite(playerRenderState.blockOverlay.atlasLocation(), playerRenderState.blockOverlay.u0(), playerRenderState.blockOverlay.v0(), playerRenderState.blockOverlay.u1(), playerRenderState.blockOverlay.v1(), poseStack, submitNodeCollector, -15132391);
            }
            if (cameraRenderState.isFirstPerson && !avatarRenderState.isSpectator) {
                if (playerRenderState.waterOverlay != null) {
                    ScreenEffectRenderer.submitWater(playerRenderState.waterOverlay, poseStack, submitNodeCollector);
                }
                if (playerRenderState.isOnFire) {
                    TextureAtlasSprite fireSprite = this.sprites.get(ModelBakery.FIRE_1);
                    ScreenEffectRenderer.submitFire(poseStack, submitNodeCollector, fireSprite);
                }
            }
        }
        if (!hideGui) {
            this.renderItemActivationAnimation(playerRenderState, poseStack, partialTicks, submitNodeCollector);
        }
    }

    private void renderItemActivationAnimation(PlayerRenderState playerRenderState, PoseStack poseStack, float partialTicks, SubmitNodeCollector submitNodeCollector) {
        PlayerRenderState.ItemActivationRenderState itemActivation = playerRenderState.itemActivation;
        if (itemActivation == null) {
            return;
        }
        int tick = 40 - itemActivation.ticks;
        float scale = ((float)tick + partialTicks) / 40.0f;
        float ts = scale * scale;
        float tc = scale * ts;
        float smoothScale = 10.25f * tc * ts - 24.95f * ts * ts + 25.5f * tc - 13.8f * ts + 4.0f * scale;
        float piScale = smoothScale * (float)Math.PI;
        WindowRenderState windowState = this.gameRenderer.gameRenderState().windowRenderState;
        float aspectRatio = (float)windowState.width / (float)windowState.height;
        float offX = itemActivation.offX * 0.3f * aspectRatio;
        float offY = itemActivation.offY * 0.3f;
        poseStack.pushPose();
        poseStack.translate(offX * Mth.abs(Mth.sin(piScale * 2.0f)), offY * Mth.abs(Mth.sin(piScale * 2.0f)), -10.0f + 9.0f * Mth.sin(piScale));
        float size = 0.8f;
        poseStack.scale(0.8f, 0.8f, 0.8f);
        poseStack.rotateDegrees(Axis.YP, 900.0f * Mth.abs(Mth.sin(piScale)));
        poseStack.rotateDegrees(Axis.XP, 6.0f * Mth.cos(scale * 8.0f));
        poseStack.rotateDegrees(Axis.ZP, 6.0f * Mth.cos(scale * 8.0f));
        this.gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
        itemActivation.itemState.submit(poseStack, submitNodeCollector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private static void submitBlockSprite(Identifier atlasLocation, float u0, float v0, float u1, float v1, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int color) {
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.blockScreenEffect(atlasLocation), (pose, builder) -> ScreenEffectRenderer.buildQuad(builder, pose.pose(), -1.0f, -1.0f, 1.0f, 1.0f, -0.5f, u1, v1, u0, v0, color));
    }

    private static void submitWater(PlayerRenderState.WaterOverlay waterOverlay, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.blockScreenEffect(UNDERWATER_LOCATION), (pose, builder) -> {
            float uvSize = 4.0f;
            ScreenEffectRenderer.buildQuad(builder, pose.pose(), -1.0f, -1.0f, 1.0f, 1.0f, -0.5f, waterOverlay.uOffset() + 4.0f, waterOverlay.vOffset() + 4.0f, waterOverlay.uOffset(), waterOverlay.vOffset(), waterOverlay.color());
        });
    }

    private static void submitFire(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, TextureAtlasSprite sprite) {
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.fireScreenEffect(sprite.atlasLocation()), (basePose, builder) -> {
            Matrix4f pose = new Matrix4f();
            pose.set((Matrix4fc)basePose.pose());
            pose.translate(0.24f, -0.3f, 0.0f);
            pose.rotateY(-0.17453292f);
            ScreenEffectRenderer.buildFireQuad(sprite, builder, pose);
            pose.set((Matrix4fc)basePose.pose());
            pose.translate(-0.24f, -0.3f, 0.0f);
            pose.rotateY(0.17453292f);
            ScreenEffectRenderer.buildFireQuad(sprite, builder, pose);
        });
    }

    private static void buildFireQuad(TextureAtlasSprite sprite, VertexConsumer builder, Matrix4f pose) {
        float size = 1.0f;
        ScreenEffectRenderer.buildSpriteQuad(builder, pose, sprite, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -436207617);
    }

    private static void buildSpriteQuad(VertexConsumer builder, Matrix4f pose, TextureAtlasSprite sprite, float x0, float y0, float x1, float y1, float z, int color) {
        ScreenEffectRenderer.buildQuad(builder, pose, x0, y0, x1, y1, z, sprite.getU1(), sprite.getV1(), sprite.getU0(), sprite.getV0(), color);
    }

    private static void buildQuad(VertexConsumer builder, Matrix4f pose, float x0, float y0, float x1, float y1, float z, float u0, float v0, float u1, float v1, int color) {
        builder.addVertex((Matrix4fc)pose, x0, y0, z).setUv(u0, v0).setColor(color);
        builder.addVertex((Matrix4fc)pose, x1, y0, z).setUv(u1, v0).setColor(color);
        builder.addVertex((Matrix4fc)pose, x1, y1, z).setUv(u1, v1).setColor(color);
        builder.addVertex((Matrix4fc)pose, x0, y1, z).setUv(u0, v1).setColor(color);
    }
}

