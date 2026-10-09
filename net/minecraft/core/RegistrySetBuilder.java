/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Cloner;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BootstrapRegistry;
import net.minecraft.core.registries.EmptyTagLookupWrapper;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.PatchedRegistry;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public class RegistrySetBuilder {
    private final List<RegistryStub> entries = new ArrayList<RegistryStub>();

    private static RegistryStub placeholderStub(final ResourceKey<? extends Registry<?>> key) {
        return new RegistryStub(){

            @Override
            public Stream<ResourceKey<? extends Registry<?>>> requiredRegistries() {
                return Stream.of(key);
            }

            @Override
            public void apply(BuildState state) {
            }
        };
    }

    public <T> RegistrySetBuilder add(final ResourceKey<? extends Registry<T>> key, final SingleRegistryBootstrap<T> bootstrap) {
        this.entries.add(new RegistryStub(){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public Stream<ResourceKey<? extends Registry<?>>> requiredRegistries() {
                return Stream.of(key);
            }

            @Override
            public void apply(BuildState state) {
                bootstrap.run(state.createBootstrap(key));
            }
        });
        return this;
    }

    public RegistrySetBuilder add(final MultiRegistryBootstrap bootstrap) {
        this.entries.add(new RegistryStub(){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public Stream<ResourceKey<? extends Registry<?>>> requiredRegistries() {
                return bootstrap.requestedRegistries().stream();
            }

            @Override
            public void apply(BuildState state) {
                bootstrap.run(state::createBootstrap);
            }
        });
        return this;
    }

    private static HolderLookup.Provider buildProviderWithContext(HolderLookup.Provider context, Stream<? extends HolderLookup.RegistryLookup<?>> newRegistries) {
        HashMap lookups = new HashMap();
        context.listRegistries().forEach(contextRegistry -> lookups.put(contextRegistry.key(), EmptyTagLookupWrapper.wrap(contextRegistry)));
        newRegistries.forEach(newRegistry -> lookups.put(newRegistry.key(), EmptyTagLookupWrapper.wrap(newRegistry)));
        return HolderLookup.Provider.create(lookups.values().stream());
    }

    public HolderLookup.Provider build(HolderLookup.Provider context) {
        BuildState state = BuildState.createAndApply(context, this.entries);
        ArrayList bootstrappedRegistries = new ArrayList(state.bootstrappedRegistries.size());
        for (BootstrappedRegistryState<?> newRegistry : state.bootstrappedRegistries.values()) {
            newRegistry.bindHolders();
            newRegistry.freeze();
            newRegistry.errorOnMissingHolders(state);
            bootstrappedRegistries.add(newRegistry.registry());
        }
        state.throwOnError();
        return RegistrySetBuilder.buildProviderWithContext(context, bootstrappedRegistries.stream());
    }

    private static Set<ResourceKey<? extends Registry<?>>> findRegistriesMissingFromPatch(HolderLookup.Provider contextRegistries, HolderLookup.Provider baseRegistries, List<RegistryStub> entries) {
        Set existingKeys = Stream.concat(RegistrySetBuilder.newRegistryKeys(entries.stream()), contextRegistries.listRegistryKeys()).collect(Collectors.toSet());
        return baseRegistries.listRegistryKeys().filter(e -> !existingKeys.contains(e)).collect(Collectors.toSet());
    }

    public PatchedRegistries buildPatch(HolderLookup.Provider context, HolderLookup.Provider fallbackProvider, Cloner.Factory clonerFactory) {
        Set<ResourceKey<Registry<?>>> missingFromPatch = RegistrySetBuilder.findRegistriesMissingFromPatch(context, fallbackProvider, this.entries);
        List<RegistryStub> expandedEntries = Stream.concat(this.entries.stream(), missingFromPatch.stream().map(RegistrySetBuilder::placeholderStub)).toList();
        BuildState state = BuildState.createAndApply(context, expandedEntries);
        ArrayList bootstrappedRegistries = new ArrayList(state.bootstrappedRegistries.size());
        for (BootstrappedRegistryState<?> newRegistry : state.bootstrappedRegistries.values()) {
            newRegistry.bindHolders();
            newRegistry.validatePatchHolders(state, fallbackProvider);
            newRegistry.freeze();
            bootstrappedRegistries.add(newRegistry.registry());
        }
        HolderLookup.Provider patchOnlyRegistries = RegistrySetBuilder.buildProviderWithContext(context, bootstrappedRegistries.stream());
        state.throwOnError();
        HolderLookup.Provider fullPatchedRegistries = EmptyTagLookupWrapper.wrap(PatchedRegistry.applyPatches(context, fallbackProvider, patchOnlyRegistries, clonerFactory, state.bootstrappedRegistries.keySet()));
        return new PatchedRegistries(fullPatchedRegistries, patchOnlyRegistries);
    }

    private static <T> ResourceKey<? extends Registry<T>> eyerollCast(ResourceKey<? extends Registry<? extends T>> registryKey) {
        return registryKey;
    }

    private static Stream<ResourceKey<? extends Registry<?>>> newRegistryKeys(Stream<RegistryStub> entries) {
        return entries.flatMap(RegistryStub::requiredRegistries).distinct();
    }

    private record BuildState(HolderLookup.Provider contextRegistries, HolderLookup.Provider allRegistries, Map<ResourceKey<? extends Registry<?>>, BootstrappedRegistryState<?>> bootstrappedRegistries, List<RuntimeException> errors) {
        public static BuildState createAndApply(HolderLookup.Provider context, List<RegistryStub> entries) {
            BuildState state = BuildState.create(context, entries);
            entries.forEach(e -> e.apply(state));
            return state;
        }

        private static BuildState create(HolderLookup.Provider context, List<RegistryStub> entries) {
            ArrayList<RuntimeException> errors = new ArrayList<RuntimeException>();
            ImmutableMap.Builder allRegistries = ImmutableMap.builder();
            ImmutableMap.Builder bootstrappedRegistries = ImmutableMap.builder();
            context.listRegistries().forEach(contextRegistry -> allRegistries.put(contextRegistry.key(), EmptyTagLookupWrapper.wrap(contextRegistry)));
            RegistrySetBuilder.newRegistryKeys(entries.stream()).forEach(newRegistryKey -> {
                BootstrappedRegistryState newRegistryEntry = BootstrappedRegistryState.create(RegistrySetBuilder.eyerollCast(newRegistryKey), Lifecycle.stable());
                BootstrapRegistry newRegistry = newRegistryEntry.registry();
                bootstrappedRegistries.put(newRegistry.key(), newRegistryEntry);
                allRegistries.put(newRegistry.key(), newRegistry);
            });
            return new BuildState(context, HolderLookup.Provider.create(allRegistries.build().values().stream()), (Map<ResourceKey<? extends Registry<?>>, BootstrappedRegistryState<?>>)bootstrappedRegistries.build(), (List<RuntimeException>)errors);
        }

        public void throwOnError() {
            if (!this.errors.isEmpty()) {
                IllegalStateException result = new IllegalStateException("Errors during registry creation");
                for (RuntimeException error : this.errors) {
                    result.addSuppressed(error);
                }
                throw result;
            }
        }

        public <T> BootstrapContext<T> createBootstrap(ResourceKey<? extends Registry<T>> key) {
            BootstrappedRegistryState<?> targetRegistry = Objects.requireNonNull(this.bootstrappedRegistries.get(key), () -> "No registry named " + String.valueOf(key.identifier()));
            return targetRegistry.createBootstrapContext(this);
        }
    }

    private record BootstrappedRegistryState<T>(BootstrapRegistry<T> registry, Map<ResourceKey<T>, T> registeredValues) {
        public static <T> BootstrappedRegistryState<T> create(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle) {
            BootstrapRegistry newRegistry = new BootstrapRegistry(key, lifecycle);
            return new BootstrappedRegistryState(newRegistry, new HashMap());
        }

        public BootstrapContext<T> createBootstrapContext(final BuildState state) {
            return new BootstrapContext<T>(this){
                final /* synthetic */ BootstrappedRegistryState this$0;
                {
                    BootstrappedRegistryState bootstrappedRegistryState = this$0;
                    Objects.requireNonNull(bootstrappedRegistryState);
                    this.this$0 = bootstrappedRegistryState;
                }

                @Override
                public Holder.Reference<T> register(ResourceKey<T> key, T value) {
                    Object previousValue = this.this$0.registeredValues.put(key, value);
                    if (previousValue != null) {
                        state.errors.add(new IllegalStateException("Duplicate registration for " + String.valueOf(key) + ", new=" + String.valueOf(value) + ", old=" + String.valueOf(previousValue)));
                    }
                    return this.this$0.registry.getOrThrow(key);
                }

                @Override
                public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
                    return state.allRegistries.lookupOrThrow(key);
                }

                @Override
                public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
                    return state.contextRegistries.lookupOrThrow(key).listElements();
                }
            };
        }

        public void bindHolders() {
            this.registeredValues.forEach((key, value) -> this.registry.getOrThrow((ResourceKey<T>)key).bindValue(value));
        }

        public void freeze() {
            this.registry.freeze();
        }

        public void errorOnMissingHolders(BuildState state) {
            this.registry.listElements().forEach(element -> {
                if (!element.isBound()) {
                    state.errors().add(new IllegalStateException("No value registered for key " + String.valueOf(element.key().identifier())));
                }
            });
        }

        public void validatePatchHolders(BuildState state, HolderLookup.Provider fallback) {
            HolderGetter baseRegistry = fallback.lookupOrThrow(this.registry.key());
            this.registry.removeIf(arg_0 -> BootstrappedRegistryState.lambda$validatePatchHolders$0((HolderLookup)baseRegistry, state, arg_0));
        }

        private static /* synthetic */ boolean lambda$validatePatchHolders$0(HolderLookup baseRegistry, BuildState state, Holder.Reference element) {
            if (element.isBound()) {
                return false;
            }
            if (baseRegistry.get(element.key()).isEmpty()) {
                state.errors().add(new IllegalStateException("Value " + String.valueOf(element.key().identifier()) + " referenced by patched element is not present in base"));
            }
            return true;
        }
    }

    public record PatchedRegistries(HolderLookup.Provider full, HolderLookup.Provider patches) {
    }

    private static interface RegistryStub {
        public Stream<ResourceKey<? extends Registry<?>>> requiredRegistries();

        public void apply(BuildState var1);
    }
}

