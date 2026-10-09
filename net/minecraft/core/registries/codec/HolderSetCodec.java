/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.core.registries.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec<E>
implements Codec<HolderSet<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<Either<TagKey<E>, List<Holder<E>>>> tagKeyOrValuesCodec;

    private static <E> Codec<List<Holder<E>>> directCodec(Codec<Holder<E>> elementCodec, boolean alwaysUseList) {
        Codec listCodec = elementCodec.listOf();
        if (alwaysUseList) {
            return listCodec;
        }
        return ExtraCodecs.compactListCodec(elementCodec, listCodec);
    }

    public static <E> Codec<HolderSet<E>> create(ResourceKey<? extends Registry<E>> registryKey, Codec<Holder<E>> elementCodec, boolean alwaysUseList) {
        return new HolderSetCodec<E>(registryKey, elementCodec, alwaysUseList);
    }

    private HolderSetCodec(ResourceKey<? extends Registry<E>> registryKey, Codec<Holder<E>> elementCodec, boolean alwaysUseList) {
        this.registryKey = registryKey;
        this.tagKeyOrValuesCodec = Codec.either(TagKey.hashedCodec(registryKey), HolderSetCodec.directCodec(elementCodec, alwaysUseList));
    }

    public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> ops, T input) {
        return this.tagKeyOrValuesCodec.decode(ops, input).flatMap(tagKeyOrValues -> {
            DataResult result = (DataResult)((Either)tagKeyOrValues.getFirst()).map(tagKey -> {
                if (ops instanceof RegistryOps) {
                    RegistryOps registryOps = (RegistryOps)ops;
                    Optional maybeRegistry = registryOps.getter(this.registryKey);
                    if (maybeRegistry.isPresent()) {
                        return HolderSetCodec.lookupTag(maybeRegistry.get(), tagKey);
                    }
                    return DataResult.error(() -> "Registry " + String.valueOf(this.registryKey.identifier()) + " is not available in this context");
                }
                return DataResult.error(() -> "Registries are not available in this context");
            }, values -> DataResult.success(HolderSet.direct(values)));
            return result.map(holders -> Pair.of((Object)holders, (Object)tagKeyOrValues.getSecond()));
        });
    }

    private static <E> DataResult<HolderSet<E>> lookupTag(HolderGetter<E> registry, TagKey<E> key) {
        return registry.get(key).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Missing tag: '" + String.valueOf(key.location()) + "' in '" + String.valueOf(key.registry().identifier()) + "'"));
    }

    public <T> DataResult<T> encode(HolderSet<E> input, DynamicOps<T> ops, T prefix) {
        if (input instanceof HolderSet.Named) {
            HolderSet.Named named = (HolderSet.Named)input;
            if (ops instanceof RegistryOps) {
                RegistryOps registryOps = (RegistryOps)ops;
                Optional maybeOwner = registryOps.getter(this.registryKey);
                if (maybeOwner.isPresent()) {
                    if (!named.canSerializeIn(maybeOwner.get())) {
                        return DataResult.error(() -> "HolderSet " + String.valueOf(named) + " is not valid in current registry set");
                    }
                    return this.tagKeyOrValuesCodec.encode((Object)Either.left(named.key()), ops, prefix);
                }
                return DataResult.error(() -> "Registry " + String.valueOf(this.registryKey.identifier()) + " is not available in this context");
            }
        }
        return this.tagKeyOrValuesCodec.encode((Object)Either.right(input.stream().toList()), ops, prefix);
    }
}

