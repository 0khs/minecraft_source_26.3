/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ChestRenderState
extends BlockEntityRenderState {
    public ChestType type = ChestType.SINGLE;
    public float open;
    public Direction facing = Direction.SOUTH;
    public ChestMaterialType material = ChestMaterialType.REGULAR;

    public static enum ChestMaterialType {
        ENDER_CHEST,
        CHRISTMAS,
        TRAPPED,
        COPPER_UNAFFECTED,
        COPPER_EXPOSED,
        COPPER_WEATHERED,
        COPPER_OXIDIZED,
        REGULAR;

    }
}

