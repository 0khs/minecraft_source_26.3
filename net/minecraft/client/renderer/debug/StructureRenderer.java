/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.debug;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugStructureInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;

public class StructureRenderer
implements DebugRenderer.SimpleDebugRenderer {
    @Override
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
        debugValues.forEachChunk(DebugSubscriptions.STRUCTURES, (chunkPos, structures) -> {
            for (DebugStructureInfo structure : structures) {
                Gizmos.cuboid(AABB.of(structure.boundingBox()), GizmoStyle.stroke(ARGB.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f)));
                for (DebugStructureInfo.Piece piece : structure.pieces()) {
                    if (piece.isStart()) {
                        Gizmos.cuboid(AABB.of(piece.boundingBox()), GizmoStyle.stroke(ARGB.colorFromFloat(1.0f, 0.0f, 1.0f, 0.0f)));
                        continue;
                    }
                    Gizmos.cuboid(AABB.of(piece.boundingBox()), GizmoStyle.stroke(ARGB.colorFromFloat(1.0f, 0.0f, 0.0f, 1.0f)));
                }
            }
        });
    }
}

