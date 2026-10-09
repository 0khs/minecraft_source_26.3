/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import net.minecraft.world.item.ArrayItemProvider;
import net.minecraft.world.item.ItemStack;

public interface ItemProvider {
    public boolean hasNext();

    public void restart();

    public ItemStack next() throws NoSuchElementException;

    public ItemStack peek() throws NoSuchElementException;

    public boolean findNext(Predicate<ItemStack> var1);

    default public boolean findNextNonEmpty() {
        return this.findNext(s -> !s.isEmpty());
    }

    default public ItemProvider orElseProvide(ItemStack fallbackItem) {
        return new FallbackItemProvider(this, fallbackItem);
    }

    public static ItemProvider of(ItemStack ... items) {
        return new ArrayItemProvider(items);
    }

    public static ItemProvider of(Collection<ItemStack> items) {
        return ItemProvider.of(items.toArray(new ItemStack[0]));
    }

    public static ItemProvider cycle(ItemStack ... items) {
        return new ArrayItemProvider.Cycled(items);
    }

    public static ItemProvider cycle(Collection<ItemStack> items) {
        return ItemProvider.cycle(items.toArray(new ItemStack[0]));
    }

    public static class FallbackItemProvider
    implements ItemProvider {
        private final ItemProvider itemProvider;
        private final ItemStack fallbackItem;
        private boolean isUsingFallback;

        public FallbackItemProvider(ItemProvider itemProvider, ItemStack fallbackItem) {
            this.itemProvider = itemProvider;
            this.fallbackItem = fallbackItem;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public void restart() {
            this.itemProvider.restart();
            this.isUsingFallback = false;
        }

        @Override
        public ItemProvider orElseProvide(ItemStack fallbackItem) {
            return this;
        }

        @Override
        public ItemStack peek() {
            if (!this.isUsingFallback) {
                if (this.itemProvider.hasNext()) {
                    return this.itemProvider.peek();
                }
                this.isUsingFallback = true;
            }
            return this.fallbackItem.copy();
        }

        @Override
        public ItemStack next() {
            if (!this.isUsingFallback) {
                if (this.itemProvider.hasNext()) {
                    return this.itemProvider.next();
                }
                this.isUsingFallback = true;
            }
            return this.fallbackItem.copy();
        }

        @Override
        public boolean findNext(Predicate<ItemStack> predicate) {
            if (this.isUsingFallback) {
                return predicate.test(this.fallbackItem);
            }
            if (this.itemProvider.findNext(predicate)) {
                return true;
            }
            if (predicate.test(this.fallbackItem)) {
                this.isUsingFallback = true;
                return true;
            }
            return false;
        }
    }
}

