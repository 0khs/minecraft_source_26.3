/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.floats.Absolute;
import net.minecraft.world.level.storage.loot.providers.number.floats.Average;
import net.minecraft.world.level.storage.loot.providers.number.floats.Ceiling;
import net.minecraft.world.level.storage.loot.providers.number.floats.ConditionalValue;
import net.minecraft.world.level.storage.loot.providers.number.floats.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.Cosine;
import net.minecraft.world.level.storage.loot.providers.number.floats.Difference;
import net.minecraft.world.level.storage.loot.providers.number.floats.EnchantmentLevelProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.EnvironmentAttributeValue;
import net.minecraft.world.level.storage.loot.providers.number.floats.Length;
import net.minecraft.world.level.storage.loot.providers.number.floats.Maximum;
import net.minecraft.world.level.storage.loot.providers.number.floats.Minimum;
import net.minecraft.world.level.storage.loot.providers.number.floats.Modulus;
import net.minecraft.world.level.storage.loot.providers.number.floats.Negate;
import net.minecraft.world.level.storage.loot.providers.number.floats.Power;
import net.minecraft.world.level.storage.loot.providers.number.floats.Product;
import net.minecraft.world.level.storage.loot.providers.number.floats.Quotient;
import net.minecraft.world.level.storage.loot.providers.number.floats.Round;
import net.minecraft.world.level.storage.loot.providers.number.floats.Sine;
import net.minecraft.world.level.storage.loot.providers.number.floats.SquareRoot;
import net.minecraft.world.level.storage.loot.providers.number.floats.Sum;
import net.minecraft.world.level.storage.loot.providers.number.floats.Truncate;
import net.minecraft.world.level.storage.loot.providers.number.floats.UniformGenerator;

public class ContextFloatProviders {
    public static final Codec<ContextFloatProvider> DIRECT_CODEC = Codec.lazyInitialized(() -> {
        Codec typedCodec = BuiltInRegistries.CONTEXT_FLOAT_PROVIDER_TYPE.byNameCodec().dispatch(ContextFloatProvider::codec, c -> c);
        return Codec.either(ConstantValue.INLINE_CODEC, (Codec)typedCodec).xmap(Either::unwrap, provider -> {
            Either either;
            if (provider instanceof ConstantValue) {
                ConstantValue constant = (ConstantValue)provider;
                either = Either.left((Object)constant);
            } else {
                either = Either.right((Object)provider);
            }
            return either;
        });
    });
    public static final Codec<Holder<ContextFloatProvider>> CODEC = RegistryCodecs.holder(Registries.CONTEXT_FLOAT_PROVIDER, DIRECT_CODEC);
    public static final Codec<HolderSet<ContextFloatProvider>> LIST_CODEC = RegistryCodecs.holderSet(Registries.CONTEXT_FLOAT_PROVIDER, DIRECT_CODEC);
    public static final ResourceKey<ContextFloatProvider> COOKING_DEFAULT_SPEED_MULTIPLIER = ContextFloatProviders.createKey("cooking/speed_default");
    public static final ResourceKey<ContextFloatProvider> COOKING_NORMAL_SPEED_MULTIPLIER = ContextFloatProviders.createKey("cooking/normal_speed_multiplier");
    public static final ResourceKey<ContextFloatProvider> COOKING_FAST_SPEED_MULTIPLIER = ContextFloatProviders.createKey("cooking/fast_speed_multiplier");
    public static final ResourceKey<ContextFloatProvider> BREWING_DEFAULT_SPEED_MULTIPLIER = ContextFloatProviders.createKey("brewing/speed_default");

    private static ResourceKey<ContextFloatProvider> createKey(String location) {
        return ResourceKey.create(Registries.CONTEXT_FLOAT_PROVIDER, Identifier.withDefaultNamespace(location));
    }

    public static void bootstrap(BootstrapContext<ContextFloatProvider> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        HolderGetter<LootItemCondition> predicates = context.lookup(Registries.PREDICATE);
        Holder.Reference<ContextFloatProvider> normalSpeed = context.register(COOKING_NORMAL_SPEED_MULTIPLIER, new ConstantValue(1.0f));
        Holder.Reference<ContextFloatProvider> fastSpeed = context.register(COOKING_FAST_SPEED_MULTIPLIER, new ConstantValue(2.0f));
        context.register(BREWING_DEFAULT_SPEED_MULTIPLIER, new ConstantValue(1.0f));
        Holder.Reference<LootItemCondition> fasterCookingBlocks = predicates.getOrThrow(LootPredicates.FAST_FURNACE);
        ConditionalValue cookingSpeed = new ConditionalValue(fasterCookingBlocks, fastSpeed, normalSpeed);
        context.register(COOKING_DEFAULT_SPEED_MULTIPLIER, cookingSpeed);
    }

    public static Holder<ContextFloatProvider> abs(Holder<ContextFloatProvider> input) {
        return Holder.direct(new Absolute(input));
    }

    @SafeVarargs
    public static Holder<ContextFloatProvider> avg(Holder<ContextFloatProvider> ... inputs) {
        return Holder.direct(new Average(HolderSet.direct(inputs)));
    }

    public static Holder<ContextFloatProvider> ceiling(Holder<ContextFloatProvider> input) {
        return Holder.direct(new Ceiling(input));
    }

    public static Holder<ContextFloatProvider> exactly(float value) {
        return Holder.direct(new ConstantValue(value));
    }

    public static Holder<ContextFloatProvider> cos(Holder<ContextFloatProvider> input) {
        return Holder.direct(new Cosine(input));
    }

    public static Holder<ContextFloatProvider> sub(Holder<ContextFloatProvider> left, Holder<ContextFloatProvider> right) {
        return Holder.direct(new Difference(left, right));
    }

    public static Holder<ContextFloatProvider> forEnchantmentLevel(LevelBasedValue amount) {
        return Holder.direct(new EnchantmentLevelProvider(amount));
    }

    public static Holder<ContextFloatProvider> forEnvironmentAttribute(EnvironmentAttribute<?> attribute) {
        return Holder.direct(new EnvironmentAttributeValue(attribute));
    }

    @SafeVarargs
    public static Holder<ContextFloatProvider> length(Holder<ContextFloatProvider> ... inputs) {
        return Holder.direct(new Length(HolderSet.direct(inputs)));
    }

    @SafeVarargs
    public static Holder<ContextFloatProvider> max(Holder<ContextFloatProvider> ... inputs) {
        return Holder.direct(new Maximum(HolderSet.direct(inputs)));
    }

    @SafeVarargs
    public static Holder<ContextFloatProvider> min(Holder<ContextFloatProvider> ... inputs) {
        return Holder.direct(new Minimum(HolderSet.direct(inputs)));
    }

    public static Holder<ContextFloatProvider> mod(Holder<ContextFloatProvider> left, Holder<ContextFloatProvider> right) {
        return Holder.direct(new Modulus(left, right));
    }

    public static Holder<ContextFloatProvider> negate(Holder<ContextFloatProvider> input) {
        return Holder.direct(new Negate(input));
    }

    public static Holder<ContextFloatProvider> pow(Holder<ContextFloatProvider> base, Holder<ContextFloatProvider> exponent) {
        return Holder.direct(new Power(base, exponent));
    }

    @SafeVarargs
    public static Holder<ContextFloatProvider> mul(Holder<ContextFloatProvider> ... inputs) {
        return Holder.direct(new Product(HolderSet.direct(inputs)));
    }

    public static Holder<ContextFloatProvider> div(Holder<ContextFloatProvider> left, Holder<ContextFloatProvider> right) {
        return Holder.direct(new Quotient(left, right));
    }

    public static Holder<ContextFloatProvider> round(Holder<ContextFloatProvider> input) {
        return Holder.direct(new Round(input));
    }

    public static Holder<ContextFloatProvider> sin(Holder<ContextFloatProvider> input) {
        return Holder.direct(new Sine(input));
    }

    public static Holder<ContextFloatProvider> sqrt(Holder<ContextFloatProvider> input) {
        return Holder.direct(new SquareRoot(input));
    }

    @SafeVarargs
    public static Holder<ContextFloatProvider> add(Holder<ContextFloatProvider> ... inputs) {
        return Holder.direct(new Sum(HolderSet.direct(inputs)));
    }

    public static Holder<ContextFloatProvider> trunc(Holder<ContextFloatProvider> input) {
        return Holder.direct(new Truncate(input));
    }

    public static Holder<ContextFloatProvider> between(float min, float max) {
        return Holder.direct(new UniformGenerator(ContextFloatProviders.exactly(min), ContextFloatProviders.exactly(max)));
    }
}

