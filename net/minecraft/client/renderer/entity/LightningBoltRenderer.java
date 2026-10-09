/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LightningBoltRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class LightningBoltRenderer
extends EntityRenderer<LightningBolt, LightningBoltRenderState> {
    private static final float BOLT_RED = 0.45f;
    private static final float BOLT_GREEN = 0.45f;
    private static final float BOLT_BLUE = 0.5f;
    private static final int SEGMENT_COUNT = 8;
    private static final int LAYER_COUNT = 4;
    private static final int BRANCH_COUNT = 3;
    private static final int BRANCH_SEGMENT_COUNT = 3;

    public LightningBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(LightningBoltRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        float[] xOffsets = new float[8];
        float[] zOffsets = new float[8];
        float xOffset = 0.0f;
        float zOffset = 0.0f;
        RandomSource random = RandomSource.createThreadLocalInstance(state.seed);
        for (int heightSegmentIndex = 7; heightSegmentIndex >= 0; --heightSegmentIndex) {
            xOffsets[heightSegmentIndex] = xOffset;
            zOffsets[heightSegmentIndex] = zOffset;
            xOffset += (float)(random.nextInt(11) - 5);
            zOffset += (float)(random.nextInt(11) - 5);
        }
        float finalXOff = xOffset;
        float finalZOff = zOffset;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            Matrix4f poseMatrix = pose.pose();
            for (int layer = 0; layer < 4; ++layer) {
                RandomSource random = RandomSource.createThreadLocalInstance(state.seed);
                for (int branchNumber = 0; branchNumber < 3; ++branchNumber) {
                    boolean isTrunkBranch = branchNumber == 0;
                    int branchStartSegment = 7 - branchNumber;
                    int branchEndSegment = isTrunkBranch ? 0 : branchStartSegment - 3 + 1;
                    float segmentStartX = xOffsets[branchStartSegment] - finalXOff;
                    float segmentStartZ = zOffsets[branchStartSegment] - finalZOff;
                    for (int currentSegment = branchStartSegment; currentSegment >= branchEndSegment; --currentSegment) {
                        float topRadius;
                        float segmentEndX = segmentStartX;
                        float segmentEndZ = segmentStartZ;
                        if (isTrunkBranch) {
                            segmentStartX += (float)(random.nextInt(11) - 5);
                            segmentStartZ += (float)(random.nextInt(11) - 5);
                        } else {
                            segmentStartX += (float)(random.nextInt(31) - 15);
                            segmentStartZ += (float)(random.nextInt(31) - 15);
                        }
                        float bottomRadius = topRadius = 0.1f + (float)layer * 0.2f;
                        if (isTrunkBranch) {
                            topRadius *= (float)currentSegment * 0.1f + 1.0f;
                            bottomRadius *= (float)(currentSegment - 1) * 0.1f + 1.0f;
                        }
                        LightningBoltRenderer.quad((Matrix4fc)poseMatrix, buffer, segmentStartX, segmentStartZ, segmentEndX, segmentEndZ, currentSegment, topRadius, bottomRadius, false, false, true, false);
                        LightningBoltRenderer.quad((Matrix4fc)poseMatrix, buffer, segmentStartX, segmentStartZ, segmentEndX, segmentEndZ, currentSegment, topRadius, bottomRadius, true, false, true, true);
                        LightningBoltRenderer.quad((Matrix4fc)poseMatrix, buffer, segmentStartX, segmentStartZ, segmentEndX, segmentEndZ, currentSegment, topRadius, bottomRadius, true, true, false, true);
                        LightningBoltRenderer.quad((Matrix4fc)poseMatrix, buffer, segmentStartX, segmentStartZ, segmentEndX, segmentEndZ, currentSegment, topRadius, bottomRadius, false, true, false, false);
                    }
                }
            }
        });
    }

    private static void quad(Matrix4fc pose, VertexConsumer buffer, float segmentStartX, float segmentStartZ, float segmentEndX, float segmentEndZ, int currentSegment, float topRadius, float bottomRadius, boolean rightXPositive, boolean rightZPositive, boolean leftXPositive, boolean leftZPositive) {
        buffer.addVertex(pose, segmentStartX + (rightXPositive ? bottomRadius : -bottomRadius), (float)(currentSegment * 16), segmentStartZ + (rightZPositive ? bottomRadius : -bottomRadius)).setColor(0.45f, 0.45f, 0.5f, 0.3f);
        buffer.addVertex(pose, segmentEndX + (rightXPositive ? topRadius : -topRadius), (float)((currentSegment + 1) * 16), segmentEndZ + (rightZPositive ? topRadius : -topRadius)).setColor(0.45f, 0.45f, 0.5f, 0.3f);
        buffer.addVertex(pose, segmentEndX + (leftXPositive ? topRadius : -topRadius), (float)((currentSegment + 1) * 16), segmentEndZ + (leftZPositive ? topRadius : -topRadius)).setColor(0.45f, 0.45f, 0.5f, 0.3f);
        buffer.addVertex(pose, segmentStartX + (leftXPositive ? bottomRadius : -bottomRadius), (float)(currentSegment * 16), segmentStartZ + (leftZPositive ? bottomRadius : -bottomRadius)).setColor(0.45f, 0.45f, 0.5f, 0.3f);
    }

    @Override
    public LightningBoltRenderState createRenderState() {
        return new LightningBoltRenderState();
    }

    @Override
    public void extractRenderState(LightningBolt entity, LightningBoltRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.seed = entity.seed;
    }

    @Override
    protected boolean affectedByCulling(LightningBolt entity) {
        return false;
    }
}

