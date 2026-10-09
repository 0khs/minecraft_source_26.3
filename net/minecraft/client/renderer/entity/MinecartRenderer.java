/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public class MinecartRenderer
extends AbstractMinecartRenderer<AbstractMinecart, MinecartRenderState> {
    public MinecartRenderer(EntityRendererProvider.Context context, ModelLayerLocation model) {
        super(context, model);
    }

    @Override
    public MinecartRenderState createRenderState() {
        return new MinecartRenderState();
    }
}

