/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.block.model.properties.conditional;

import net.minecraft.client.renderer.block.model.properties.conditional.ConditionalBlockModelProperty;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.world.level.block.state.BlockState;

public class IsXmas
implements ConditionalBlockModelProperty {
    @Override
    public boolean get(BlockState blockState) {
        return ChestRenderer.xmasTextures();
    }
}

