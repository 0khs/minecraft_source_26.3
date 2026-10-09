/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity.state;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class EndPortalRenderState
extends BlockEntityRenderState {
    public final Set<Direction> facesToShow = EnumSet.noneOf(Direction.class);
}

