/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  org.slf4j.Logger
 */
package net.minecraft.world.item.component;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ContainerComponent;
import net.minecraft.world.item.component.GrowableMutableContainer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import org.slf4j.Logger;

public record ChargedProjectiles(List<ItemStackTemplate> items) implements ContainerComponent<ChargedProjectiles>,
TooltipProvider
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SIZE = 1024;
    public static final ChargedProjectiles EMPTY = new ChargedProjectiles(List.of());
    public static final Codec<ChargedProjectiles> CODEC = ItemStackTemplate.CODEC.sizeLimitedListOf(1024).xmap(ChargedProjectiles::new, projectiles -> projectiles.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargedProjectiles> STREAM_CODEC = ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list(1024)).map(ChargedProjectiles::new, projectiles -> projectiles.items);

    public ChargedProjectiles {
        if (items.size() > 1024) {
            throw new IllegalArgumentException("Got " + items.size() + " items, but maximum is 1024");
        }
    }

    public static ChargedProjectiles of(ItemStackTemplate stack) {
        return new ChargedProjectiles(List.of(stack));
    }

    public static ChargedProjectiles ofNonEmpty(List<ItemStack> items) {
        List<ItemStackTemplate> list = items.stream().filter(i -> !i.isEmpty()).map(ItemStackTemplate::fromStack).limit(1024L).toList();
        if (list.size() != items.size()) {
            LOGGER.warn("Tried to load invalid items as charged projectiles");
        }
        return new ChargedProjectiles(list);
    }

    public boolean contains(Item item) {
        for (ItemStackTemplate projectile : this.items) {
            if (!projectile.is(item)) continue;
            return true;
        }
        return false;
    }

    @Override
    public Stream<ItemStack> itemCopies() {
        return this.items.stream().map(ItemStackTemplate::create);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public ChargedProjectiles copyWithContents(Stream<ItemStack> newContents) {
        return new ChargedProjectiles(newContents.filter(s -> !s.isEmpty()).map(ItemStackTemplate::fromNonEmptyStack).toList());
    }

    public Mutable asMutable() {
        ArrayList<ItemStack> itemsList = new ArrayList<ItemStack>(this.items.size());
        for (ItemStackTemplate item : this.items) {
            itemsList.add(item.create());
        }
        return new Mutable(itemsList);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        ItemStack current = null;
        int count = 0;
        for (ItemStackTemplate projectileTemplate : this.items) {
            ItemStack projectile = projectileTemplate.create();
            if (current == null) {
                current = projectile;
                count = 1;
                continue;
            }
            if (ItemStack.matches(current, projectile)) {
                ++count;
                continue;
            }
            ChargedProjectiles.addProjectileTooltip(context, consumer, current, count);
            current = projectile;
            count = 1;
        }
        if (current != null) {
            ChargedProjectiles.addProjectileTooltip(context, consumer, current, count);
        }
    }

    private static void addProjectileTooltip(Item.TooltipContext context, Consumer<Component> consumer, ItemStack projectile, int count) {
        if (count == 1) {
            consumer.accept(Component.translatable("item.minecraft.crossbow.projectile.single", projectile.getDisplayName()));
        } else {
            consumer.accept(Component.translatable("item.minecraft.crossbow.projectile.multiple", count, projectile.getHoverName()));
        }
        TooltipDisplay projectileDisplay = projectile.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        projectile.addDetailsToTooltip(context, projectileDisplay, null, TooltipFlag.NORMAL, line -> consumer.accept(Component.literal("  ").append((Component)line).withStyle(ChatFormatting.GRAY)));
    }

    public static class Mutable
    extends GrowableMutableContainer<ChargedProjectiles> {
        private Mutable(List<ItemStack> items) {
            super(items);
        }

        @Override
        protected boolean addSlotWithItem(ItemProvider newItems) {
            return newItems.findNextNonEmpty() && super.addSlotWithItem(newItems);
        }

        @Override
        public boolean canInsertNewSlots() {
            return this.items.size() < 1024;
        }

        @Override
        public ChargedProjectiles toImmutable() {
            ArrayList<ItemStackTemplate> nonEmptyItems = new ArrayList<ItemStackTemplate>(this.items.size());
            for (ItemStack item : this.items) {
                if (item.isEmpty()) continue;
                nonEmptyItems.add(ItemStackTemplate.fromNonEmptyStack(item));
            }
            return new ChargedProjectiles(nonEmptyItems);
        }
    }
}

