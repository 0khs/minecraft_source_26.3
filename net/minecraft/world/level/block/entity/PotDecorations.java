/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.block.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.references.ItemIds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record PotDecorations(Optional<ItemStackTemplate> back, Optional<ItemStackTemplate> left, Optional<ItemStackTemplate> right, Optional<ItemStackTemplate> front) implements TooltipProvider
{
    public static final PotDecorations EMPTY = new PotDecorations(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    public static final Codec<PotDecorations> CODEC = RecordCodecBuilder.create(i -> i.group((App)ItemStackTemplate.CODEC.optionalFieldOf("back").forGetter(PotDecorations::back), (App)ItemStackTemplate.CODEC.optionalFieldOf("left").forGetter(PotDecorations::left), (App)ItemStackTemplate.CODEC.optionalFieldOf("right").forGetter(PotDecorations::right), (App)ItemStackTemplate.CODEC.optionalFieldOf("front").forGetter(PotDecorations::front)).apply((Applicative)i, PotDecorations::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PotDecorations> STREAM_CODEC = StreamCodec.composite(ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs::optional), PotDecorations::back, ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs::optional), PotDecorations::left, ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs::optional), PotDecorations::right, ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs::optional), PotDecorations::front, PotDecorations::new);

    public static PotDecorations allBrick(HolderGetter<Item> items) {
        Optional<ItemStackTemplate> brick = Optional.of(new ItemStackTemplate(items.getOrThrow(ItemIds.BRICK), 1, DataComponentPatch.EMPTY));
        return new PotDecorations(brick, brick, brick, brick);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        if (this.equals(EMPTY)) {
            return;
        }
        consumer.accept(CommonComponents.EMPTY);
        PotDecorations.addSideDetailsToTooltip(consumer, this.front);
        PotDecorations.addSideDetailsToTooltip(consumer, this.left);
        PotDecorations.addSideDetailsToTooltip(consumer, this.right);
        PotDecorations.addSideDetailsToTooltip(consumer, this.back);
    }

    private static void addSideDetailsToTooltip(Consumer<Component> consumer, Optional<ItemStackTemplate> side) {
        side.ifPresent(itemStackTemplate -> consumer.accept(itemStackTemplate.create().getHoverName().plainCopy().withStyle(ChatFormatting.GRAY)));
    }
}

