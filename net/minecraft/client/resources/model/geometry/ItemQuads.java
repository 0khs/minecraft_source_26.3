/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model.geometry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public record ItemQuads(List<BakedQuad> all, List<BakedQuad> solid, List<BakedQuad> translucent) {
    public static final ItemQuads EMPTY = new ItemQuads(List.of(), List.of(), List.of());

    public static ItemQuads split(List<BakedQuad> quads) {
        List<BakedQuad> all;
        if (quads.isEmpty()) {
            return EMPTY;
        }
        ArrayList<BakedQuad> solid = new ArrayList<BakedQuad>();
        ArrayList<BakedQuad> translucent = new ArrayList<BakedQuad>();
        for (BakedQuad quad : quads) {
            if (quad.materialInfo().itemRenderType().hasBlending()) {
                translucent.add(quad);
                continue;
            }
            solid.add(quad);
        }
        if (translucent.isEmpty()) {
            all = List.copyOf(quads);
            return new ItemQuads(all, all, List.of());
        }
        if (solid.isEmpty()) {
            all = List.copyOf(quads);
            return new ItemQuads(all, List.of(), all);
        }
        return new ItemQuads(List.copyOf(quads), List.copyOf(solid), List.copyOf(translucent));
    }

    public boolean isEmpty() {
        return this.all.isEmpty();
    }
}

