/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.component;

import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.slot.SlotCollection;

public interface ContainerComponent<T extends ContainerComponent<T>> {
    public Stream<ItemStack> itemCopies();

    public int size();

    public T copyWithContents(Stream<ItemStack> var1);

    public Mutable<T> asMutable();

    public static interface Mutable<T>
    extends SlotCollection {
        public T toImmutable();
    }
}

