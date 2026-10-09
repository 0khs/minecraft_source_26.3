/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.math.MatrixUtil;
import java.util.List;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4fc;

public class ItemFeatureRenderer
extends RenderTypeFeatureRenderer<Submit> {
    public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Item");
    public static final Identifier ENCHANTED_GLINT_ARMOR = Identifier.withDefaultNamespace("textures/misc/enchanted_glint_armor.png");
    public static final Identifier ENCHANTED_GLINT_ITEM = Identifier.withDefaultNamespace("textures/misc/enchanted_glint_item.png");
    private static final float SPECIAL_FOIL_UI_SCALE = 0.5f;
    private static final float SPECIAL_FOIL_FIRST_PERSON_SCALE = 0.75f;
    public static final float SPECIAL_FOIL_TEXTURE_SCALE = 0.0078125f;
    public static final int NO_TINT = -1;
    private final QuadInstance quadInstance = new QuadInstance();

    @Override
    protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
        for (Submit submit : submits) {
            this.prepareSubmit(submit);
        }
    }

    private void prepareSubmit(Submit submit) {
        if (submit.outlineColor() != 0) {
            this.prepareOutlineSubmit(submit);
        } else {
            this.prepareMainSubmit(submit);
        }
    }

    private void prepareMainSubmit(Submit submit) {
        this.quadInstance.setLightCoords(submit.lightCoords());
        this.quadInstance.setOverlayCoords(submit.overlayCoords());
        ItemStackRenderState.FoilType foilType = submit.foilType();
        PoseStack.Pose foilDecalPose = foilType == ItemStackRenderState.FoilType.SPECIAL ? ItemFeatureRenderer.computeFoilDecalPose(submit.displayContext(), submit.pose()) : null;
        for (BakedQuad quad : submit.quads()) {
            BakedQuad.MaterialInfo material = quad.materialInfo();
            RenderType renderType = switch (foilType) {
                default -> throw new MatchException(null, null);
                case ItemStackRenderState.FoilType.NONE -> material.itemRenderType();
                case ItemStackRenderState.FoilType.STANDARD -> material.itemGlintRenderType();
                case ItemStackRenderState.FoilType.SPECIAL -> material.itemGlintSpecialRenderType();
            };
            this.quadInstance.setColor(ItemFeatureRenderer.getLayerColorSafe(submit.tintLayers(), material));
            if (foilType == ItemStackRenderState.FoilType.SPECIAL) {
                this.getVertexBuilder(renderType).putBakedQuadWithGlint(submit.pose(), quad, this.quadInstance, foilDecalPose);
                continue;
            }
            this.getVertexBuilder(renderType).putBakedQuad(submit.pose(), quad, this.quadInstance);
        }
    }

    private void prepareOutlineSubmit(Submit submit) {
        for (BakedQuad quad : submit.quads()) {
            BakedQuad.MaterialInfo material = quad.materialInfo();
            RenderType renderType = material.itemRenderType().outline().orElse(null);
            if (renderType == null) continue;
            this.quadInstance.setColor(submit.outlineColor());
            this.getVertexBuilder(renderType).putBakedQuad(submit.pose(), quad, this.quadInstance);
        }
    }

    private static PoseStack.Pose computeFoilDecalPose(ItemDisplayContext type, PoseStack.Pose pose) {
        PoseStack.Pose foilDecalPose = pose.copy();
        if (type == ItemDisplayContext.GUI) {
            MatrixUtil.mulComponentWise(foilDecalPose.pose(), 0.5f);
        } else if (type.firstPerson()) {
            MatrixUtil.mulComponentWise(foilDecalPose.pose(), 0.75f);
        }
        return foilDecalPose;
    }

    private static int getLayerColorSafe(int[] layers, int layer) {
        if (layer < 0 || layer >= layers.length) {
            return -1;
        }
        return layers[layer];
    }

    private static int getLayerColorSafe(int[] tintLayers, BakedQuad.MaterialInfo material) {
        if (material.isTinted()) {
            return ItemFeatureRenderer.getLayerColorSafe(tintLayers, material.tintIndex());
        }
        return -1;
    }

    public record Submit(PoseStack.Pose pose, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, ItemStackRenderState.FoilType foilType) implements TranslucentSubmit
    {
        @Override
        public float distanceToCameraSq() {
            return TranslucentSubmit.computeDistanceToCameraSq((Matrix4fc)this.pose.pose());
        }

        public FeatureRendererType<Submit> featureType() {
            return TYPE;
        }
    }
}

