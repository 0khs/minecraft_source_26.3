/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model.geometry;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;

@FunctionalInterface
public interface UnbakedGeometry {
    public static final UnbakedGeometry EMPTY = (textureSlots, modelBaker, modelState, modelDebugName) -> QuadCollection.EMPTY;

    public QuadCollection bake(TextureSlots var1, ModelBaker var2, ModelState var3, ModelDebugName var4);
}

