/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.item.slot;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.item.slot.ContentsSlotSource;
import net.minecraft.world.item.slot.EmptySlotSource;
import net.minecraft.world.item.slot.FilteredSlotSource;
import net.minecraft.world.item.slot.GroupSlotSource;
import net.minecraft.world.item.slot.LimitSlotSource;
import net.minecraft.world.item.slot.RangeSlotSource;
import net.minecraft.world.item.slot.SlotSource;

public interface SlotSources {
    public static final Codec<SlotSource> TYPED_CODEC = BuiltInRegistries.SLOT_SOURCE_TYPE.byNameCodec().dispatch(SlotSource::codec, c -> c);
    public static final Codec<SlotSource> DIRECT_CODEC = Codec.lazyInitialized(() -> Codec.either(TYPED_CODEC, GroupSlotSource.INLINE_CODEC).xmap(typedOrInline -> (SlotSource)typedOrInline.map(e -> e, e -> e), slotSource -> {
        Either either;
        if (slotSource instanceof GroupSlotSource) {
            GroupSlotSource composite = (GroupSlotSource)slotSource;
            either = Either.right((Object)composite);
        } else {
            either = Either.left((Object)slotSource);
        }
        return either;
    }));
    public static final Codec<Holder<SlotSource>> CODEC = RegistryCodecs.holder(Registries.SLOT_SOURCE, DIRECT_CODEC);
    public static final Codec<HolderSet<SlotSource>> LIST_CODEC = RegistryCodecs.holderSet(Registries.SLOT_SOURCE, TYPED_CODEC);

    public static MapCodec<? extends SlotSource> bootstrap(Registry<MapCodec<? extends SlotSource>> registry) {
        Registry.register(registry, "group", GroupSlotSource.MAP_CODEC);
        Registry.register(registry, "filtered", FilteredSlotSource.MAP_CODEC);
        Registry.register(registry, "limit_slots", LimitSlotSource.MAP_CODEC);
        Registry.register(registry, "slot_range", RangeSlotSource.MAP_CODEC);
        Registry.register(registry, "contents", ContentsSlotSource.MAP_CODEC);
        return Registry.register(registry, "empty", EmptySlotSource.MAP_CODEC);
    }
}

