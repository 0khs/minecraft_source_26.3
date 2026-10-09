/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class TrackingItemStackRenderState
extends ItemStackRenderState {
    private final List<Object> modelIdentityElements = new ArrayList<Object>();

    @Override
    public void appendModelIdentityElement(Object element) {
        this.modelIdentityElements.add(element);
    }

    public Object getModelIdentity() {
        return this.modelIdentityElements;
    }
}

