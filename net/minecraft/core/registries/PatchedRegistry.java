/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Lifecycle
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.registries;

import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Cloner;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class PatchedRegistry<T>
implements HolderLookup.RegistryLookup<T> {
    private final ResourceKey<? extends Registry<? extends T>> key;
    private final Lifecycle lifecycle;
    private final Map<ResourceKey<T>, Holder.Reference<T>> entries = new HashMap<ResourceKey<T>, Holder.Reference<T>>();

    private PatchedRegistry(ResourceKey<? extends Registry<? extends T>> key, Lifecycle lifecycle) {
        this.key = key;
        this.lifecycle = lifecycle;
    }

    @Override
    public ResourceKey<? extends Registry<? extends T>> key() {
        return this.key;
    }

    @Override
    public Lifecycle registryLifecycle() {
        return this.lifecycle;
    }

    @Override
    public Stream<Holder.Reference<T>> listElements() {
        return this.entries.values().stream();
    }

    @Override
    public Optional<Holder.Reference<T>> get(ResourceKey<T> id) {
        return Optional.ofNullable(this.entries.get(id));
    }

    @Override
    public Stream<HolderSet.Named<T>> listTags() {
        throw new UnsupportedOperationException("Tags cloning is not supported");
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> id) {
        return Optional.of(HolderSet.emptyNamed(this, id));
    }

    private static <T> HolderLookup.RegistryLookup<T> createLazyFullPatchedRegistries(Cloner.Factory clonerFactory, ResourceKey<? extends Registry<? extends T>> registryKey, HolderLookup.Provider baseProvider, HolderLookup.Provider patchProvider, MutableObject<HolderLookup.Provider> clonedRegistriesProvider) {
        Cloner cloner = clonerFactory.cloner(registryKey);
        if (cloner == null) {
            throw new NullPointerException("No cloner for " + String.valueOf(registryKey.identifier()));
        }
        HolderGetter patchContents = patchProvider.lookupOrThrow(registryKey);
        HolderGetter baseContents = baseProvider.lookupOrThrow(registryKey);
        Lifecycle lifecycle = patchContents.registryLifecycle().add(baseContents.registryLifecycle());
        PatchedRegistry result = new PatchedRegistry(registryKey, lifecycle);
        patchContents.listElements().forEach(elementHolder -> {
            ResourceKey elementKey = elementHolder.key();
            LazyHolder holder = new LazyHolder(result, elementKey);
            holder.supplier = () -> cloner.clone(elementHolder.value(), patchProvider, (HolderLookup.Provider)clonedRegistriesProvider.get());
            result.entries.put(elementKey, holder);
        });
        baseContents.listElements().forEach(elementHolder -> {
            ResourceKey elementKey = elementHolder.key();
            result.entries.computeIfAbsent(elementKey, key -> {
                LazyHolder holder = new LazyHolder(result, elementKey);
                holder.supplier = () -> cloner.clone(elementHolder.value(), baseProvider, (HolderLookup.Provider)clonedRegistriesProvider.get());
                return holder;
            });
        });
        return result;
    }

    public static HolderLookup.Provider applyPatches(HolderLookup.Provider context, HolderLookup.Provider baseRegistries, HolderLookup.Provider patchRegistries, Cloner.Factory clonerFactory, Set<ResourceKey<? extends Registry<?>>> registriesToClone) {
        MutableObject resultHolder = new MutableObject();
        List lazyFullRegistries = registriesToClone.stream().map(registryKey -> PatchedRegistry.createLazyFullPatchedRegistries(clonerFactory, registryKey, baseRegistries, patchRegistries, (MutableObject<HolderLookup.Provider>)resultHolder)).collect(Collectors.toUnmodifiableList());
        HolderLookup.Provider result = HolderLookup.Provider.create(Stream.concat(context.listRegistries(), lazyFullRegistries.stream()));
        resultHolder.setValue((Object)result);
        return result;
    }

    private static class LazyHolder<T>
    extends Holder.Reference<T> {
        private @Nullable Supplier<T> supplier;

        protected LazyHolder(HolderOwner<T> owner, @Nullable ResourceKey<T> key) {
            super(Holder.Reference.Type.STAND_ALONE, owner, key, null);
        }

        @Override
        protected void bindValue(T value) {
            super.bindValue(value);
            this.supplier = null;
        }

        @Override
        public T value() {
            if (this.supplier != null) {
                this.bindValue(this.supplier.get());
            }
            return super.value();
        }
    }
}

