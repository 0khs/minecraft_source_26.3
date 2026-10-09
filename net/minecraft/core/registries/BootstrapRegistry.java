/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Lifecycle
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.registries;

import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

public class BootstrapRegistry<T>
implements HolderLookup.RegistryLookup<T> {
    private final ResourceKey<? extends Registry<T>> key;
    private final Lifecycle lifecycle;
    private Storage<T> storage = new RegistrationStorage(this);

    public BootstrapRegistry(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle) {
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

    public void freeze() {
        this.storage = this.storage.freeze();
    }

    public void removeIf(Predicate<Holder.Reference<T>> predicate) {
        this.storage.removeIf(predicate);
    }

    @Override
    public Optional<Holder.Reference<T>> get(ResourceKey<T> id) {
        return Optional.ofNullable(this.storage.get(id));
    }

    @Override
    public Stream<Holder.Reference<T>> listElements() {
        return this.storage.listElements().stream();
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> id) {
        return Optional.ofNullable(this.storage.get(id));
    }

    @Override
    public Stream<HolderSet.Named<T>> listTags() {
        return this.storage.listTags().stream();
    }

    private class RegistrationStorage
    implements Storage<T> {
        private final Map<ResourceKey<T>, Holder.Reference<T>> holders;
        private final Map<TagKey<T>, HolderSet.Named<T>> holderSets;
        final /* synthetic */ BootstrapRegistry this$0;

        private RegistrationStorage(BootstrapRegistry bootstrapRegistry) {
            BootstrapRegistry bootstrapRegistry2 = bootstrapRegistry;
            Objects.requireNonNull(bootstrapRegistry2);
            this.this$0 = bootstrapRegistry2;
            this.holders = new HashMap();
            this.holderSets = new HashMap();
        }

        @Override
        public Storage<T> freeze() {
            return new FrozenStorage(this.holders, this.holderSets);
        }

        @Override
        public void removeIf(Predicate<Holder.Reference<T>> predicate) {
            this.holders.values().removeIf(predicate);
        }

        @Override
        public Holder.Reference<T> get(ResourceKey<T> id) {
            return this.holders.computeIfAbsent(id, key -> Holder.Reference.createStandAlone(this.this$0, key));
        }

        @Override
        public Collection<Holder.Reference<T>> listElements() {
            throw new UnsupportedOperationException("List is not available during bootstrap");
        }

        @Override
        public HolderSet.Named<T> get(TagKey<T> id) {
            return this.holderSets.computeIfAbsent(id, key -> HolderSet.emptyNamed(this.this$0, key));
        }

        @Override
        public Collection<HolderSet.Named<T>> listTags() {
            throw new UnsupportedOperationException("List is not available during bootstrap");
        }
    }

    private static interface Storage<T> {
        public Storage<T> freeze();

        public void removeIf(Predicate<Holder.Reference<T>> var1);

        public @Nullable Holder.Reference<T> get(ResourceKey<T> var1);

        public Collection<Holder.Reference<T>> listElements();

        public @Nullable HolderSet.Named<T> get(TagKey<T> var1);

        public Collection<HolderSet.Named<T>> listTags();
    }

    private static class FrozenStorage<T>
    implements Storage<T> {
        private final Map<ResourceKey<T>, Holder.Reference<T>> holders;
        private final Map<TagKey<T>, HolderSet.Named<T>> holderSets;

        private FrozenStorage(Map<ResourceKey<T>, Holder.Reference<T>> holders, Map<TagKey<T>, HolderSet.Named<T>> holderSets) {
            this.holders = Map.copyOf(holders);
            this.holderSets = Map.copyOf(holderSets);
        }

        @Override
        public Storage<T> freeze() {
            return this;
        }

        @Override
        public void removeIf(Predicate<Holder.Reference<T>> predicate) {
            throw new UnsupportedOperationException("Registry is already frozen");
        }

        @Override
        public @Nullable Holder.Reference<T> get(ResourceKey<T> id) {
            return this.holders.get(id);
        }

        @Override
        public Collection<Holder.Reference<T>> listElements() {
            return this.holders.values();
        }

        @Override
        public @Nullable HolderSet.Named<T> get(TagKey<T> id) {
            return this.holderSets.get(id);
        }

        @Override
        public Collection<HolderSet.Named<T>> listTags() {
            return this.holderSets.values();
        }
    }
}

