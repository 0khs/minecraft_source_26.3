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
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EnchantTableRenderer
implements BlockEntityRenderer<EnchantingTableBlockEntity, EnchantTableRenderState> {
    public static final SpriteId BOOK_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("enchantment/enchanting_table_book");
    private final SpriteGetter sprites;
    private final BookModel bookModel;

    public EnchantTableRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public EnchantTableRenderState createRenderState() {
        return new EnchantTableRenderState();
    }

    @Override
    public void extractRenderState(EnchantingTableBlockEntity blockEntity, EnchantTableRenderState state, float partialTicks, Vec3 cameraPosition,  @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        float or;
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.flip = Mth.lerp(partialTicks, blockEntity.oFlip, blockEntity.flip);
        state.open = Mth.lerp(partialTicks, blockEntity.oOpen, blockEntity.open);
        state.time = (float)blockEntity.time + partialTicks;
        for (or = blockEntity.rot - blockEntity.oRot; or >= (float)Math.PI; or -= (float)Math.PI * 2) {
        }
        while (or < (float)(-Math.PI)) {
            or += (float)Math.PI * 2;
        }
        state.yRot = blockEntity.oRot + or * partialTicks;
    }

    @Override
    public void submit(EnchantTableRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.75f, 0.5f);
        poseStack.translate(0.0f, 0.1f + Mth.sin(state.time * 0.1f) * 0.01f, 0.0f);
        float yRot = state.yRot;
        poseStack.rotate(Axis.YP, -yRot);
        poseStack.rotateDegrees(Axis.ZP, 80.0f);
        float ff1 = Mth.frac(state.flip + 0.25f) * 1.6f - 0.3f;
        float ff2 = Mth.frac(state.flip + 0.75f) * 1.6f - 0.3f;
        BookModel.State bookState = BookModel.State.forAnimation(state.time, Mth.clamp(ff1, 0.0f, 1.0f), Mth.clamp(ff2, 0.0f, 1.0f), state.open);
        submitNodeCollector.submitModel(this.bookModel, bookState, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, BOOK_TEXTURE, this.sprites, 0);
        if (state.breakProgress != null) {
            submitNodeCollector.order(1).submitCrumblingOverlay(this.bookModel, bookState, poseStack, BOOK_TEXTURE.renderType(this.bookModel.renderType()), state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
        }
        poseStack.popPose();
    }
}

