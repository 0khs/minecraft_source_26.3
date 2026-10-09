/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHoglinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Zoglin;

public class ZoglinRenderer
extends AbstractHoglinRenderer<Zoglin> {
    private static final Identifier ZOGLIN_LOCATION = Identifier.withDefaultNamespace("textures/entity/hoglin/zoglin.png");
    private static final Identifier BABY_ZOGLIN_LOCATION = Identifier.withDefaultNamespace("textures/entity/hoglin/zoglin_baby.png");

    public ZoglinRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.ZOGLIN, ModelLayers.ZOGLIN_BABY, 0.7f);
    }

    @Override
    public Identifier getTextureLocation(HoglinRenderState state) {
        return state.isBaby ? BABY_ZOGLIN_LOCATION : ZOGLIN_LOCATION;
    }
}

