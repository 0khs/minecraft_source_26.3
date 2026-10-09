/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data.models;

import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.world.item.Item;

public interface ItemModelOutput {
    default public void accept(Item item, ItemModel.Unbaked generator) {
        this.accept(item, generator, ClientItem.Properties.DEFAULT);
    }

    public void accept(Item var1, ItemModel.Unbaked var2, ClientItem.Properties var3);

    public void copy(Item var1, Item var2);
}

