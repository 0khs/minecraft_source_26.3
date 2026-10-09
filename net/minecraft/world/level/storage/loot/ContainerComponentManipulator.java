/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ContainerComponent;
import net.minecraft.world.item.slot.SlotCollection;
import net.minecraft.world.item.slot.SlotSelector;

public record ContainerComponentManipulator<T extends ContainerComponent<T>>(DataComponentType<T> type, T empty) {
    public void setContents(ItemStack itemStack, T defaultValue, Stream<ItemStack> newContents) {
        ContainerComponent currentValue = (ContainerComponent)itemStack.getOrDefault(this.type(), defaultValue);
        Object newValue = currentValue.copyWithContents(newContents);
        itemStack.set(this.type(), newValue);
    }

    public void setContents(ItemStack itemStack, Stream<ItemStack> newContents) {
        this.setContents(itemStack, this.empty(), newContents);
    }

    public void modifyItems(ItemStack itemStack, UnaryOperator<ItemStack> modifier) {
        ContainerComponent contents = (ContainerComponent)itemStack.get(this.type());
        if (contents != null) {
            UnaryOperator nonEmptyModifier = currentItemStack -> {
                if (currentItemStack.isEmpty()) {
                    return currentItemStack;
                }
                ItemStack newItemStack = (ItemStack)modifier.apply((ItemStack)currentItemStack);
                newItemStack.limitSize(newItemStack.getMaxStackSize());
                return newItemStack;
            };
            this.setContents(itemStack, contents.itemCopies().map(nonEmptyModifier));
        }
    }

    public SlotCollection getSlots(ItemStack itemStack) {
        return new ComponentSlotCollection(itemStack, this);
    }

    private record ComponentSlotCollection<T extends ContainerComponent<T>>(ItemStack itemStack, ContainerComponentManipulator<T> component) implements SlotCollection
    {
        @Override
        public Stream<ItemStack> itemCopies() {
            ContainerComponent contents = (ContainerComponent)this.itemStack.get(this.component.type());
            return contents != null ? contents.itemCopies() : Stream.empty();
        }

        @Override
        public int size() {
            ContainerComponent contents = (ContainerComponent)this.itemStack.get(this.component.type());
            return contents != null ? contents.size() : 0;
        }

        @Override
        public int replaceSlotItems(ItemProvider items, SlotSelector slotSelector) {
            ContainerComponent currentValue = (ContainerComponent)this.itemStack.getOrDefault(this.component.type(), this.component.empty());
            ContainerComponent.Mutable mutable = currentValue.asMutable();
            int slotsReplaced = mutable.replaceSlotItems(items, slotSelector);
            this.itemStack.set(this.component.type(), (ContainerComponent)mutable.toImmutable());
            return slotsReplaced;
        }

        @Override
        public void modifySlots(Consumer<? super SlotAccess> consumer, SlotSelector slotSelector) {
            ContainerComponent currentValue = (ContainerComponent)this.itemStack.getOrDefault(this.component.type(), this.component.empty());
            ContainerComponent.Mutable mutable = currentValue.asMutable();
            mutable.modifySlots(consumer, slotSelector);
            this.itemStack.set(this.component.type(), (ContainerComponent)mutable.toImmutable());
        }
    }
}

