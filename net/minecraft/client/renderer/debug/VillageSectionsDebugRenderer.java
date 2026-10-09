/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.debug;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.SectionPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueAccess;

public class VillageSectionsDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    @Override
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
        debugValues.forEachBlock(DebugSubscriptions.VILLAGE_SECTIONS, (pos, ignored) -> {
            SectionPos villageSection = SectionPos.of(pos);
            Gizmos.cuboid(villageSection.center(), GizmoStyle.fill(ARGB.colorFromFloat(0.15f, 0.2f, 1.0f, 0.2f)));
        });
    }
}

