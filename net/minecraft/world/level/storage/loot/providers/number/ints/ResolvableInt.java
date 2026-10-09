/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

public sealed interface ResolvableInt {
    public static final Codec<ResolvableInt> CODEC = Codec.either(Constant.CODEC, Reference.CODEC).xmap(Either::unwrap, ResolvableInt::wrap);
    public static final StreamCodec<ByteBuf, ResolvableInt> STREAM_CODEC = ByteBufCodecs.either(Constant.STREAM_CODEC, Reference.STREAM_CODEC).map(Either::unwrap, ResolvableInt::wrap);

    private static Either<Constant, Reference> wrap(ResolvableInt resolvableNumber) {
        ResolvableInt resolvableInt = resolvableNumber;
        Objects.requireNonNull(resolvableInt);
        ResolvableInt resolvableInt2 = resolvableInt;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Constant.class, Reference.class}, (ResolvableInt)resolvableInt2, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Constant constant = (Constant)resolvableInt2;
                yield Either.left((Object)constant);
            }
            case 1 -> {
                Reference reference = (Reference)resolvableInt2;
                yield Either.right((Object)reference);
            }
        };
    }

    public int get(LootContext var1, int var2);

    public static ResolvableInt fromKey(ResourceKey<ContextIntProvider> key) {
        return new Reference(key);
    }

    public static <T> int getFromItem(ItemStack itemStack, DataComponentType<T> componentType, Function<T, ResolvableInt> getter, LootContext context, int defaultValue) {
        T component = itemStack.get(componentType);
        return component != null ? getter.apply(component).get(context, defaultValue) : defaultValue;
    }

    public record Constant(int value) implements ResolvableInt
    {
        private static final Codec<Constant> CODEC = Codec.INT.xmap(Constant::new, Constant::value);
        private static final StreamCodec<ByteBuf, Constant> STREAM_CODEC = ByteBufCodecs.INT.map(Constant::new, Constant::value);

        @Override
        public int get(LootContext context, int defaultValue) {
            return this.value;
        }
    }

    public record Reference(ResourceKey<ContextIntProvider> key) implements ResolvableInt
    {
        private static final Codec<Reference> CODEC = ResourceKey.codec(Registries.CONTEXT_INT_PROVIDER).xmap(Reference::new, Reference::key);
        private static final StreamCodec<ByteBuf, Reference> STREAM_CODEC = ResourceKey.streamCodec(Registries.CONTEXT_INT_PROVIDER).map(Reference::new, Reference::key);

        @Override
        public int get(LootContext context, int defaultValue) {
            return this.getProvider(context).map(provider -> provider.getInt(context)).orElse(defaultValue);
        }

        private Optional<ContextIntProvider> getProvider(LootContext context) {
            return context.getResolver().lookupOrThrow(Registries.CONTEXT_INT_PROVIDER).get(this.key).map(Holder.Reference::value);
        }
    }
}

