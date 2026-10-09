/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

public record TransmuteResult(Optional<Holder<Item>> item, int count, DataComponentPatch components) {
    public static final MapCodec<TransmuteResult> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Item.CODEC.optionalFieldOf("id").forGetter(TransmuteResult::item), (App)ExtraCodecs.intRange(1, 99).optionalFieldOf("count", (Object)1).forGetter(TransmuteResult::count), (App)DataComponentPatch.CODEC.optionalFieldOf("components", (Object)DataComponentPatch.EMPTY).forGetter(TransmuteResult::components)).apply((Applicative)i, TransmuteResult::new));
    public static final Codec<TransmuteResult> CODEC = Codec.withAlternative((Codec)MAP_CODEC.codec(), Item.CODEC, TransmuteResult::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, TransmuteResult> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.optional(Item.STREAM_CODEC), TransmuteResult::item, ByteBufCodecs.VAR_INT, TransmuteResult::count, DataComponentPatch.STREAM_CODEC, TransmuteResult::components, TransmuteResult::new);
    public static final TransmuteResult KEEP_INPUT_ITEM = new TransmuteResult(Optional.empty(), 1, DataComponentPatch.EMPTY);

    public TransmuteResult(Holder<Item> item) {
        this(Optional.of(item), 1, DataComponentPatch.EMPTY);
    }

    public TransmuteResult(Item item) {
        this(item.builtInRegistryHolder());
    }

    public static TransmuteResult fromTemplate(ItemStackTemplate template) {
        return new TransmuteResult(Optional.of(template.item()), template.count(), template.components());
    }

    public ItemStackTemplate resolve(Holder<Item> inputItem) {
        return this.resolve(inputItem, this.count);
    }

    public ItemStackTemplate resolve(Holder<Item> inputItem, int count) {
        return new ItemStackTemplate(this.item.orElse(inputItem), count, this.components);
    }
}

