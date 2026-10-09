/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.debug;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;

public class EntityBlockIntersectionDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final float PADDING = 0.02f;

    @Override
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
        debugValues.forEachBlock(DebugSubscriptions.ENTITY_BLOCK_INTERSECTIONS, (pos, type) -> Gizmos.cuboid(pos, 0.02f, GizmoStyle.fill(type.color())));
    }
}

