/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

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
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;

public sealed interface ResolvableFloat {
    public static final Codec<ResolvableFloat> CODEC = Codec.either(Constant.CODEC, Reference.CODEC).xmap(Either::unwrap, ResolvableFloat::wrap);
    public static final StreamCodec<ByteBuf, ResolvableFloat> STREAM_CODEC = ByteBufCodecs.either(Constant.STREAM_CODEC, Reference.STREAM_CODEC).map(Either::unwrap, ResolvableFloat::wrap);

    private static Either<Constant, Reference> wrap(ResolvableFloat resolvableNumber) {
        ResolvableFloat resolvableFloat = resolvableNumber;
        Objects.requireNonNull(resolvableFloat);
        ResolvableFloat resolvableFloat2 = resolvableFloat;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Constant.class, Reference.class}, (ResolvableFloat)resolvableFloat2, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Constant constant = (Constant)resolvableFloat2;
                yield Either.left((Object)constant);
            }
            case 1 -> {
                Reference reference = (Reference)resolvableFloat2;
                yield Either.right((Object)reference);
            }
        };
    }

    public float get(LootContext var1, float var2);

    public static ResolvableFloat fromKey(ResourceKey<ContextFloatProvider> key) {
        return new Reference(key);
    }

    public static <T> float getFromItem(ItemStack itemStack, DataComponentType<T> componentType, Function<T, ResolvableFloat> getter, LootContext context, float defaultValue) {
        T component = itemStack.get(componentType);
        return component != null ? getter.apply(component).get(context, defaultValue) : defaultValue;
    }

    public record Constant(float value) implements ResolvableFloat
    {
        private static final Codec<Constant> CODEC = Codec.FLOAT.xmap(Constant::new, Constant::value);
        private static final StreamCodec<ByteBuf, Constant> STREAM_CODEC = ByteBufCodecs.FLOAT.map(Constant::new, Constant::value);

        @Override
        public float get(LootContext context, float defaultValue) {
            return this.value;
        }
    }

    public record Reference(ResourceKey<ContextFloatProvider> key) implements ResolvableFloat
    {
        private static final Codec<Reference> CODEC = ResourceKey.codec(Registries.CONTEXT_FLOAT_PROVIDER).xmap(Reference::new, Reference::key);
        private static final StreamCodec<ByteBuf, Reference> STREAM_CODEC = ResourceKey.streamCodec(Registries.CONTEXT_FLOAT_PROVIDER).map(Reference::new, Reference::key);

        @Override
        public float get(LootContext context, float defaultValue) {
            return this.getProvider(context).map(provider -> Float.valueOf(provider.getFloat(context))).orElse(Float.valueOf(defaultValue)).floatValue();
        }

        private Optional<ContextFloatProvider> getProvider(LootContext context) {
            return context.getResolver().lookupOrThrow(Registries.CONTEXT_FLOAT_PROVIDER).get(this.key).map(Holder.Reference::value);
        }
    }
}

