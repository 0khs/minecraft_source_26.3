/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class CreativeInventoryListener
implements ContainerListener {
    private final Minecraft minecraft;

    public CreativeInventoryListener(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack itemStack) {
        this.minecraft.gameMode.handleCreativeModeItemAdd(itemStack, slotIndex);
    }

    @Override
    public void dataChanged(AbstractContainerMenu container, int id, int value) {
    }
}

