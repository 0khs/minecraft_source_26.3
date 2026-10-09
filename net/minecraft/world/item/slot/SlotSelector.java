/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item.slot;

import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;

public sealed interface SlotSelector {
    public static final SlotSelector ANY_SLOT = new FilteringSelector(null);
    public static final SlotSelector EMPTY_SLOTS = SlotSelector.selecting(ItemStack::isEmpty);
    public static final SlotSelector NON_EMPTY_SLOTS = SlotSelector.selecting(s -> !s.isEmpty());

    public boolean trySelectSlot(ItemStack var1);

    public SlotSelector filter(Predicate<? super ItemStack> var1);

    public SlotSelector limit(int var1);

    public static SlotSelector selecting(Predicate<? super ItemStack> predicate) {
        return new FilteringSelector(predicate);
    }

    public static SlotSelector tracking(SlotSelector slotSelector, MutableInt selectedCount) {
        return new TrackingSelector(slotSelector, Integer.MAX_VALUE, selectedCount);
    }

    public static SlotSelector tracking(MutableInt selectedCount) {
        return SlotSelector.tracking(ANY_SLOT, selectedCount);
    }

    public record FilteringSelector(@Nullable Predicate<? super ItemStack> filter) implements SlotSelector
    {
        @Override
        public boolean trySelectSlot(ItemStack itemInSlot) {
            return this.filter == null || this.filter.test(itemInSlot);
        }

        @Override
        public SlotSelector filter(Predicate<? super ItemStack> predicate) {
            if (this.filter == null) {
                return new FilteringSelector(predicate);
            }
            return new FilteringSelector(t -> this.filter.test((ItemStack)t) && predicate.test((ItemStack)t));
        }

        @Override
        public SlotSelector limit(int limit) {
            return new TrackingSelector(this, limit, new MutableInt());
        }
    }

    public record TrackingSelector(SlotSelector slotSelector, int limit, MutableInt selectedCount) implements SlotSelector
    {
        @Override
        public boolean trySelectSlot(ItemStack itemInSlot) {
            if (this.selectedCount.intValue() < this.limit && this.slotSelector.trySelectSlot(itemInSlot)) {
                this.selectedCount.increment();
                return true;
            }
            return false;
        }

        @Override
        public SlotSelector filter(Predicate<? super ItemStack> predicate) {
            return new TrackingSelector(this.slotSelector.filter(predicate), this.limit, this.selectedCount);
        }

        @Override
        public SlotSelector limit(int limit) {
            return limit < this.limit ? new TrackingSelector(this.slotSelector, limit, this.selectedCount) : this;
        }
    }
}

