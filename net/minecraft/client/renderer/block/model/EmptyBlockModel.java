/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fc;

public class EmptyBlockModel
implements BlockModel {
    public static final BlockModel INSTANCE = new EmptyBlockModel();

    @Override
    public void update(BlockModelRenderState output, BlockState blockState, BlockDisplayContext displayContext, long seed) {
    }

    public record Unbaked() implements BlockModel.Unbaked
    {
        @Override
        public BlockModel bake(BlockModel.BakingContext context, Matrix4fc transformation) {
            return INSTANCE;
        }
    }
}

