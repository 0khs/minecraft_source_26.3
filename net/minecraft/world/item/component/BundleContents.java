/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  org.apache.commons.lang3.math.Fraction
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item.component;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.item.component.ContainerComponent;
import net.minecraft.world.item.component.GrowableMutableContainer;
import net.minecraft.world.item.slot.SlotSelector;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

public final class BundleContents
implements ContainerComponent<BundleContents>,
TooltipComponent {
    public static final BundleContents EMPTY = new BundleContents(List.of());
    public static final Codec<BundleContents> CODEC = ItemStackTemplate.CODEC.listOf().xmap(BundleContents::new, contents -> contents.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, BundleContents> STREAM_CODEC = ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).map(BundleContents::new, contents -> contents.items);
    private static final Fraction BUNDLE_IN_BUNDLE_WEIGHT = Fraction.getFraction((int)1, (int)16);
    private static final int NO_STACK_INDEX = -1;
    public static final int NO_SELECTED_ITEM_INDEX = -1;
    public static final DataResult<Fraction> BEEHIVE_WEIGHT = DataResult.success((Object)Fraction.ONE);
    private final List<ItemStackTemplate> items;
    private final int selectedItem;
    private final Supplier<DataResult<Fraction>> weight;

    private BundleContents(List<ItemStackTemplate> items, int selectedItem) {
        this.items = items;
        this.selectedItem = selectedItem;
        this.weight = Suppliers.memoize(() -> BundleContents.computeContentWeight(this.items));
    }

    public BundleContents(List<ItemStackTemplate> items) {
        this(items, -1);
    }

    private static DataResult<Fraction> computeContentWeight(List<? extends ItemInstance> items) {
        try {
            Fraction weight = Fraction.ZERO;
            for (ItemInstance itemInstance : items) {
                DataResult<Fraction> itemWeight = BundleContents.getWeight(itemInstance);
                if (itemWeight.isError()) {
                    return itemWeight;
                }
                weight = weight.add(((Fraction)itemWeight.getOrThrow()).multiplyBy(Fraction.getFraction((int)itemInstance.count(), (int)1)));
            }
            return DataResult.success((Object)weight);
        }
        catch (ArithmeticException exception) {
            return DataResult.error(() -> "Excessive total bundle weight");
        }
    }

    private static DataResult<Fraction> getWeight(ItemInstance item) {
        BundleContents bundle = item.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            return bundle.weight().map(nestedWeight -> nestedWeight.add(BUNDLE_IN_BUNDLE_WEIGHT));
        }
        List<BeehiveBlockEntity.Occupant> bees = item.getOrDefault(DataComponents.BEES, Bees.EMPTY).bees();
        if (!bees.isEmpty()) {
            return BEEHIVE_WEIGHT;
        }
        return DataResult.success((Object)Fraction.getFraction((int)1, (int)item.getMaxStackSize()));
    }

    public static boolean canItemBeInBundle(ItemStack itemToAdd) {
        return !itemToAdd.isEmpty() && itemToAdd.getItem().canFitInsideContainerItems();
    }

    public int getNumberOfItemsToShow() {
        int numberOfItemStacks = this.size();
        int availableItemsToShow = numberOfItemStacks > 12 ? 11 : 12;
        int itemsOnNonFullRow = numberOfItemStacks % 4;
        int emptySpaceOnNonFullRow = itemsOnNonFullRow == 0 ? 0 : 4 - itemsOnNonFullRow;
        return Math.min(numberOfItemStacks, availableItemsToShow - emptySpaceOnNonFullRow);
    }

    @Override
    public Stream<ItemStack> itemCopies() {
        return this.items.stream().map(ItemStackTemplate::create);
    }

    public List<ItemStackTemplate> items() {
        return this.items;
    }

    @Override
    public int size() {
        return this.items.size();
    }

    public DataResult<Fraction> weight() {
        return this.weight.get();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public int getSelectedItemIndex() {
        return this.selectedItem;
    }

    public @Nullable ItemStackTemplate getSelectedItem() {
        if (this.selectedItem == -1) {
            return null;
        }
        return this.items.get(this.selectedItem);
    }

    @Override
    public BundleContents copyWithContents(Stream<ItemStack> newContents) {
        Mutable builder = new Mutable();
        newContents.forEach(builder::tryInsert);
        return builder.toImmutable();
    }

    public Mutable asMutable() {
        DataResult<Fraction> currentWeight = this.weight.get();
        if (currentWeight.isError()) {
            return new Mutable();
        }
        ArrayList<ItemStack> itemsList = new ArrayList<ItemStack>(this.items.size());
        for (ItemStackTemplate item : this.items) {
            itemsList.add(item.create());
        }
        return new Mutable(itemsList, (Fraction)currentWeight.getOrThrow(), this.selectedItem);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BundleContents) {
            BundleContents contents = (BundleContents)obj;
            return this.items.equals(contents.items);
        }
        return false;
    }

    public int hashCode() {
        return this.items.hashCode();
    }

    public String toString() {
        return "BundleContents" + String.valueOf(this.items);
    }

    public static class Mutable
    extends GrowableMutableContainer<BundleContents> {
        private Fraction weight;
        private int selectedItem;
        private boolean needsFlattening;

        private Mutable(List<ItemStack> items, Fraction weight, int selectedItem) {
            super(items);
            this.weight = weight;
            this.selectedItem = selectedItem;
        }

        public Mutable() {
            this(new ArrayList<ItemStack>(), Fraction.ZERO, -1);
        }

        public Mutable clearItems() {
            this.items.clear();
            this.weight = Fraction.ZERO;
            this.selectedItem = -1;
            return this;
        }

        private int findStackIndexWithinRange(ItemStack itemsToAdd, int minInclusive, int maxExclusive) {
            if (!itemsToAdd.isStackable()) {
                return -1;
            }
            int startIndex = Math.max(minInclusive, 0);
            int endIndex = Math.min(maxExclusive, this.items.size());
            for (int i = startIndex; i < endIndex; ++i) {
                if (!ItemStack.isSameItemSameComponents((ItemStack)this.items.get(i), itemsToAdd)) continue;
                return i;
            }
            return -1;
        }

        private int findStackIndex(ItemStack itemsToAdd) {
            return this.findStackIndexWithinRange(itemsToAdd, 0, this.items.size());
        }

        private int getMaxAmountToAdd(Fraction itemWeight) {
            Fraction remainingWeight = Fraction.ONE.subtract(this.weight);
            return Math.max(remainingWeight.divideBy(itemWeight).intValue(), 0);
        }

        public int tryInsert(ItemStack itemsToAdd) {
            if (!BundleContents.canItemBeInBundle(itemsToAdd)) {
                return 0;
            }
            DataResult<Fraction> maybeItemWeight = BundleContents.getWeight(itemsToAdd);
            if (maybeItemWeight.isError()) {
                return 0;
            }
            Fraction itemWeight = (Fraction)maybeItemWeight.getOrThrow();
            int amountToAdd = Math.min(itemsToAdd.getCount(), this.getMaxAmountToAdd(itemWeight));
            if (amountToAdd == 0) {
                return 0;
            }
            this.weight = this.weight.add(Mutable.getStackedWeight(itemWeight, amountToAdd));
            int stackIndex = this.findStackIndex(itemsToAdd);
            if (stackIndex != -1) {
                ItemStack removedStack = (ItemStack)this.items.remove(stackIndex);
                ItemStack mergedStack = removedStack.copyWithCount(removedStack.getCount() + amountToAdd);
                itemsToAdd.shrink(amountToAdd);
                this.items.addFirst(mergedStack);
            } else {
                this.items.addFirst(itemsToAdd.split(amountToAdd));
            }
            return amountToAdd;
        }

        public int tryTransfer(Slot slot, Player player) {
            ItemStack other = slot.getItem();
            DataResult<Fraction> itemWeight = BundleContents.getWeight(other);
            if (itemWeight.isError()) {
                return 0;
            }
            int maxAmount = this.getMaxAmountToAdd((Fraction)itemWeight.getOrThrow());
            return BundleContents.canItemBeInBundle(other) ? this.tryInsert(slot.safeTake(other.getCount(), maxAmount, player)) : 0;
        }

        public void toggleSelectedItem(int selectedItem) {
            this.selectedItem = this.selectedItem == selectedItem || this.indexIsOutsideAllowedBounds(selectedItem) ? -1 : selectedItem;
        }

        private boolean indexIsOutsideAllowedBounds(int selectedItem) {
            return selectedItem < 0 || selectedItem >= this.items.size();
        }

        public @Nullable ItemStack removeOne() {
            if (this.items.isEmpty()) {
                return null;
            }
            int removeIndex = this.indexIsOutsideAllowedBounds(this.selectedItem) ? 0 : this.selectedItem;
            ItemStack stack = ((ItemStack)this.items.remove(removeIndex)).copy();
            this.weight = this.weight.subtract(Mutable.getStackedWeight(stack));
            this.toggleSelectedItem(-1);
            return stack;
        }

        private static Fraction getStackedWeight(Fraction weight, int count) {
            return weight.multiplyBy(Fraction.getFraction((int)count, (int)1));
        }

        private static Fraction getStackedWeight(ItemStack stack) {
            return Mutable.getStackedWeight((Fraction)BundleContents.getWeight(stack).getOrThrow(), stack.getCount());
        }

        public Fraction weight() {
            return this.weight;
        }

        @Override
        public int replaceSlotItems(ItemProvider newItems, SlotSelector slotSelector) {
            this.mergeIdenticalStacks();
            return super.replaceSlotItems(newItems, slotSelector);
        }

        @Override
        public void modifySlots(Consumer<? super SlotAccess> consumer, SlotSelector slotSelector) {
            this.mergeIdenticalStacks();
            super.modifySlots(consumer, slotSelector);
        }

        @Override
        protected boolean setItem(int slot, ItemStack itemStack) {
            Fraction newWeight;
            ItemStack currentItem = (ItemStack)this.items.get(slot);
            Fraction adjustedWeight = currentItem.isEmpty() ? this.weight : this.weight.subtract(Mutable.getStackedWeight(currentItem));
            Fraction fraction = newWeight = itemStack.isEmpty() ? adjustedWeight : Mutable.getWeightWithAddedItems(adjustedWeight, itemStack);
            if (newWeight != null && super.setItem(slot, itemStack)) {
                this.weight = newWeight;
                this.needsFlattening = true;
                return true;
            }
            return false;
        }

        @Override
        protected boolean addSlotWithItem(ItemProvider newItems) {
            if (!newItems.findNextNonEmpty()) {
                return false;
            }
            Fraction newWeight = Mutable.getWeightWithAddedItems(this.weight, newItems.peek());
            if (newWeight != null && super.addSlotWithItem(newItems)) {
                this.weight = newWeight;
                this.needsFlattening = true;
                return true;
            }
            return false;
        }

        private static @Nullable Fraction getWeightWithAddedItems(Fraction weight, ItemStack itemsToAdd) {
            if (BundleContents.canItemBeInBundle(itemsToAdd)) {
                DataResult<Fraction> itemWeight = BundleContents.getWeight(itemsToAdd);
                if (itemWeight.isError()) {
                    return null;
                }
                Fraction newWeight = weight.add(Mutable.getStackedWeight((Fraction)itemWeight.getOrThrow(), itemsToAdd.getCount()));
                if (newWeight.compareTo(Fraction.ONE) <= 0) {
                    return newWeight;
                }
            }
            return null;
        }

        @Override
        public boolean canInsertNewSlots() {
            return this.weight.compareTo(Fraction.ONE) < 0;
        }

        private void mergeIdenticalStacks() {
            if (!this.needsFlattening) {
                return;
            }
            for (int index = 0; index < this.items.size(); ++index) {
                ItemStack itemStack = (ItemStack)this.items.get(index);
                if (itemStack.isEmpty()) {
                    this.items.remove(index--);
                    continue;
                }
                int stackIndex = this.findStackIndexWithinRange(itemStack, index + 1, this.items.size());
                if (stackIndex == -1) continue;
                ItemStack targetStack = (ItemStack)this.items.get(stackIndex);
                int mergedCount = itemStack.getCount() + targetStack.getCount();
                this.items.set(stackIndex, itemStack.copyWithCount(mergedCount));
                this.items.remove(index--);
            }
            this.needsFlattening = false;
        }

        @Override
        public BundleContents toImmutable() {
            this.mergeIdenticalStacks();
            ImmutableList.Builder builder = ImmutableList.builder();
            for (ItemStack item : this.items) {
                builder.add((Object)ItemStackTemplate.fromNonEmptyStack(item));
            }
            return new BundleContents((List<ItemStackTemplate>)builder.build(), this.selectedItem);
        }
    }
}

