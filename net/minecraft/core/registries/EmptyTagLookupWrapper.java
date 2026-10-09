/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.registries;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;

public record EmptyTagLookupWrapper<T>(HolderLookup.RegistryLookup<T> parent) implements HolderLookup.RegistryLookup.Delegate<T>
{
    public static <T> HolderLookup.RegistryLookup<T> wrap(HolderLookup.RegistryLookup<T> registryLookup) {
        if (registryLookup instanceof EmptyTagLookupWrapper) {
            return registryLookup;
        }
        return new EmptyTagLookupWrapper<T>(registryLookup);
    }

    public static HolderLookup.Provider wrap(HolderLookup.Provider provider) {
        return HolderLookup.Provider.create(provider.listRegistries().map(EmptyTagLookupWrapper::wrap));
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> id) {
        return Optional.of(this.getOrThrow(id));
    }

    @Override
    public HolderSet.Named<T> getOrThrow(TagKey<T> id) {
        return HolderSet.emptyNamed(this.parent, id);
    }

    @Override
    public boolean canSerialize(HolderOwner<T> owner) {
        return this.parent.canSerialize(owner);
    }

    @Override
    public Stream<HolderSet.Named<T>> listTags() {
        throw new UnsupportedOperationException("Tags are not available in datagen");
    }
}

