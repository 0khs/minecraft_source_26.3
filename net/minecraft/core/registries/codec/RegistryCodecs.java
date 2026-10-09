/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.core.registries.codec;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.codec.HolderSetCodec;
import net.minecraft.core.registries.codec.RegistryFileCodec;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {
    public static <E> Codec<Holder<E>> holder(ResourceKey<? extends Registry<E>> registryKey) {
        return RegistryFixedCodec.create(registryKey);
    }

    public static <E> Codec<Holder<E>> holder(ResourceKey<? extends Registry<E>> registryKey, Codec<E> elementCodec) {
        return RegistryCodecs.holder(registryKey, elementCodec, true);
    }

    public static <E> Codec<Holder<E>> holder(ResourceKey<? extends Registry<E>> registryKey, Codec<E> elementCodec, boolean allowInline) {
        return RegistryFileCodec.create(registryKey, elementCodec, allowInline);
    }

    public static <E> Codec<HolderSet<E>> holderSet(ResourceKey<? extends Registry<E>> registryKey, Codec<E> elementCodec) {
        return RegistryCodecs.holderSet(registryKey, elementCodec, false);
    }

    public static <E> Codec<HolderSet<E>> holderSet(ResourceKey<? extends Registry<E>> registryKey, Codec<E> elementCodec, boolean alwaysUseList) {
        return HolderSetCodec.create(registryKey, RegistryCodecs.holder(registryKey, elementCodec), alwaysUseList);
    }

    public static <E> Codec<HolderSet<E>> holderSet(ResourceKey<? extends Registry<E>> registryKey) {
        return RegistryCodecs.holderSet(registryKey, false);
    }

    public static <E> Codec<HolderSet<E>> holderSet(ResourceKey<? extends Registry<E>> registryKey, boolean alwaysUseList) {
        return HolderSetCodec.create(registryKey, RegistryCodecs.holder(registryKey), alwaysUseList);
    }
}

