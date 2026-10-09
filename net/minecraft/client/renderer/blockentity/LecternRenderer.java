/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.LecternRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LecternRenderer
implements BlockEntityRenderer<LecternBlockEntity, LecternRenderState> {
    private final SpriteGetter sprites;
    private final BookModel bookModel;
    private static final BookModel.State BOOK_STATE = BookModel.State.forAnimation(0.0f, 0.1f, 0.9f, 1.2f);

    public LecternRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public LecternRenderState createRenderState() {
        return new LecternRenderState();
    }

    @Override
    public void extractRenderState(LecternBlockEntity blockEntity, LecternRenderState state, float partialTicks, Vec3 cameraPosition,  @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.hasBook = blockEntity.getBlockState().getValue(LecternBlock.HAS_BOOK);
        state.yRot = blockEntity.getBlockState().getValue(LecternBlock.FACING).getClockWise().toYRot();
    }

    @Override
    public void submit(LecternRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.hasBook) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 1.0625f, 0.5f);
        poseStack.rotateDegrees(Axis.YP, -state.yRot);
        poseStack.rotateDegrees(Axis.ZP, 67.5f);
        poseStack.translate(0.0f, -0.125f, 0.0f);
        submitNodeCollector.submitModel(this.bookModel, BOOK_STATE, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, EnchantTableRenderer.BOOK_TEXTURE, this.sprites, 0);
        if (state.breakProgress != null) {
            submitNodeCollector.order(1).submitCrumblingOverlay(this.bookModel, BOOK_STATE, poseStack, EnchantTableRenderer.BOOK_TEXTURE.renderType(this.bookModel.renderType()), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
        }
        poseStack.popPose();
    }
}

