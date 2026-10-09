/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.component;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ContainerComponent;
import net.minecraft.world.item.slot.SlotSelector;

public abstract class SimpleMutableContainer<T>
implements ContainerComponent.Mutable<T> {
    protected final List<ItemStack> items;

    public SimpleMutableContainer(List<ItemStack> items) {
        this.items = items;
    }

    @Override
    public Stream<ItemStack> itemCopies() {
        return this.items.stream().map(ItemStack::copy);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public int replaceSlotItems(ItemProvider newItems, SlotSelector slotSelector) {
        int successCount = 0;
        for (int index = 0; index < this.items.size() && newItems.hasNext(); ++index) {
            boolean success;
            ItemStack currentItem = this.items.get(index);
            if (!slotSelector.trySelectSlot(currentItem) || !(success = this.setItem(index, newItems.next()))) continue;
            ++successCount;
        }
        return successCount;
    }

    @Override
    public void modifySlots(Consumer<? super SlotAccess> consumer, SlotSelector slotSelector) {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack currentItem = this.items.get(i);
            if (!slotSelector.trySelectSlot(currentItem)) continue;
            int slot = i;
            consumer.accept(SlotAccess.of(() -> this.items.get(slot), stack -> this.setItem(slot, (ItemStack)stack)));
        }
    }

    protected boolean setItem(int slot, ItemStack itemStack) {
        this.items.set(slot, itemStack);
        return true;
    }
}

