/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity.state;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class BeaconRenderState
extends BlockEntityRenderState {
    public float animationTime;
    public float beamRadiusScale;
    public List<Section> sections = new ArrayList<Section>();

    public record Section(int color, int height) {
    }
}

