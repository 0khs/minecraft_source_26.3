/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity.state;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityWithBoundingBoxRenderState;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;

public class TestInstanceRenderState
extends BlockEntityRenderState {
    public BeaconRenderState beaconRenderState;
    public BlockEntityWithBoundingBoxRenderState blockEntityWithBoundingBoxRenderState;
    public final List<TestInstanceBlockEntity.ErrorMarker> errorMarkers = new ArrayList<TestInstanceBlockEntity.ErrorMarker>();
}

