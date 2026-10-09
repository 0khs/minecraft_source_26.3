/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public record TagPredicate<T>(HolderSet<T> tag, boolean expected) {
    public static <T> Codec<TagPredicate<T>> codec(ResourceKey<? extends Registry<T>> registryKey) {
        return RecordCodecBuilder.create(i -> i.group((App)RegistryCodecs.holderSet(registryKey).fieldOf("id").forGetter(TagPredicate::tag), (App)Codec.BOOL.fieldOf("expected").forGetter(TagPredicate::expected)).apply((Applicative)i, TagPredicate::new));
    }

    public static <T> TagPredicate<T> is(HolderGetter<T> lookup, TagKey<T> tag) {
        return TagPredicate.is(lookup.getOrThrow(tag));
    }

    public static <T> TagPredicate<T> is(HolderSet<T> tag) {
        return new TagPredicate<T>(tag, true);
    }

    public static <T> TagPredicate<T> isNot(HolderGetter<T> lookup, TagKey<T> tag) {
        return TagPredicate.isNot(lookup.getOrThrow(tag));
    }

    public static <T> TagPredicate<T> isNot(HolderSet<T> tag) {
        return new TagPredicate<T>(tag, false);
    }

    public boolean matches(Holder<T> holder) {
        return this.tag.contains(holder) == this.expected;
    }
}

