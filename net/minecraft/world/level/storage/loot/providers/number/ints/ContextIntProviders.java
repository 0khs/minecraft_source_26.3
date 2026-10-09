/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.providers.number.DispatcherProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.Average;
import net.minecraft.world.level.storage.loot.providers.number.ints.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConditionalValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.Difference;
import net.minecraft.world.level.storage.loot.providers.number.ints.FloorModulus;
import net.minecraft.world.level.storage.loot.providers.number.ints.FloorQuotient;
import net.minecraft.world.level.storage.loot.providers.number.ints.FromFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.Maximum;
import net.minecraft.world.level.storage.loot.providers.number.ints.Minimum;
import net.minecraft.world.level.storage.loot.providers.number.ints.Modulus;
import net.minecraft.world.level.storage.loot.providers.number.ints.Negate;
import net.minecraft.world.level.storage.loot.providers.number.ints.NumberDispatcher;
import net.minecraft.world.level.storage.loot.providers.number.ints.Product;
import net.minecraft.world.level.storage.loot.providers.number.ints.Quotient;
import net.minecraft.world.level.storage.loot.providers.number.ints.ScoreboardValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.Sum;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ints.WeightedListValue;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;

public class ContextIntProviders {
    public static final Codec<ContextIntProvider> DIRECT_CODEC = Codec.lazyInitialized(() -> {
        Codec typedCodec = BuiltInRegistries.CONTEXT_INT_PROVIDER_TYPE.byNameCodec().dispatch(ContextIntProvider::codec, c -> c);
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
    public static final Codec<Holder<ContextIntProvider>> CODEC = RegistryCodecs.holder(Registries.CONTEXT_INT_PROVIDER, DIRECT_CODEC);
    public static final Codec<HolderSet<ContextIntProvider>> LIST_CODEC = RegistryCodecs.holderSet(Registries.CONTEXT_INT_PROVIDER, DIRECT_CODEC);
    public static final ResourceKey<ContextIntProvider> COMPOSTABLE_LOW = ContextIntProviders.createKey("compostable/low");
    public static final ResourceKey<ContextIntProvider> COMPOSTABLE_LOW_MEDIUM = ContextIntProviders.createKey("compostable/low_medium");
    public static final ResourceKey<ContextIntProvider> COMPOSTABLE_MEDIUM = ContextIntProviders.createKey("compostable/medium");
    public static final ResourceKey<ContextIntProvider> COMPOSTABLE_MEDIUM_HIGH = ContextIntProviders.createKey("compostable/medium_high");
    public static final ResourceKey<ContextIntProvider> COMPOSTABLE_ALWAYS_ADD_ONE = ContextIntProviders.createKey("compostable/always_add_one");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_BAMBOO = ContextIntProviders.createKey("cooking/time_bamboo");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_WOOL_SLABS = ContextIntProviders.createKey("cooking/time_wool_slabs");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_WOOL_CARPETS = ContextIntProviders.createKey("cooking/time_wool_carpets");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_DRY_PLANTS = ContextIntProviders.createKey("cooking/time_dry_plants");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_WOOD_ITEMS_EXTRA_SMALL = ContextIntProviders.createKey("cooking/time_wood_items_extra_small");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_WOOL = ContextIntProviders.createKey("cooking/time_wool");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_WOOD_SLABS = ContextIntProviders.createKey("cooking/time_wood_slabs");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_WOOD_ITEMS_LARGE = ContextIntProviders.createKey("cooking/time_wood_items_large");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_WOOD_ITEMS_SMALL = ContextIntProviders.createKey("cooking/time_wood_items_small");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_ROOTS = ContextIntProviders.createKey("cooking/time_roots");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_WOOD_BLOCKS = ContextIntProviders.createKey("cooking/time_wood_blocks");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_HANGING_SIGNS = ContextIntProviders.createKey("cooking/time_hanging_signs");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_BOATS = ContextIntProviders.createKey("cooking/time_boats");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_COAL = ContextIntProviders.createKey("cooking/time_coal");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_BLAZE_ROD = ContextIntProviders.createKey("cooking/time_blaze_rod");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_DRIED_KELP_BLOCK = ContextIntProviders.createKey("cooking/time_dried_kelp_block");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_COAL_BLOCK = ContextIntProviders.createKey("cooking/time_coal_block");
    public static final ResourceKey<ContextIntProvider> COOKING_TIME_LAVA_BUCKET = ContextIntProviders.createKey("cooking/time_lava_bucket");
    public static final ResourceKey<ContextIntProvider> COOKING_NORMAL_BURN_TIME_REDUCTION_FACTOR = ContextIntProviders.createKey("cooking/normal_burn_time_reduction_factor");
    public static final ResourceKey<ContextIntProvider> COOKING_FAST_BURN_TIME_REDUCTION_FACTOR = ContextIntProviders.createKey("cooking/fast_burn_time_reduction_factor");
    public static final ResourceKey<ContextIntProvider> BREWING_DEFAULT_USES = ContextIntProviders.createKey("brewing/uses_default");

    private static ResourceKey<ContextIntProvider> createKey(String location) {
        return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Identifier.withDefaultNamespace(location));
    }

    public static void bootstrap(BootstrapContext<ContextIntProvider> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        HolderGetter<LootItemCondition> predicates = context.lookup(Registries.PREDICATE);
        Holder.Reference<ContextIntProvider> normalBurnTime = context.register(COOKING_NORMAL_BURN_TIME_REDUCTION_FACTOR, new ConstantValue(1));
        Holder.Reference<ContextIntProvider> fastBurnTime = context.register(COOKING_FAST_BURN_TIME_REDUCTION_FACTOR, new ConstantValue(2));
        context.register(COMPOSTABLE_LOW, ContextIntProviders.compostable(blocks, 30));
        context.register(COMPOSTABLE_LOW_MEDIUM, ContextIntProviders.compostable(blocks, 50));
        context.register(COMPOSTABLE_MEDIUM, ContextIntProviders.compostable(blocks, 65));
        context.register(COMPOSTABLE_MEDIUM_HIGH, ContextIntProviders.compostable(blocks, 85));
        context.register(COMPOSTABLE_ALWAYS_ADD_ONE, ContextIntProviders.compostable(blocks, 100));
        context.register(COOKING_TIME_BAMBOO, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 50));
        context.register(COOKING_TIME_WOOL_SLABS, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 50));
        context.register(COOKING_TIME_WOOL_CARPETS, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 67));
        context.register(COOKING_TIME_DRY_PLANTS, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 100));
        context.register(COOKING_TIME_WOOD_ITEMS_EXTRA_SMALL, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 100));
        context.register(COOKING_TIME_WOOL, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 100));
        context.register(COOKING_TIME_WOOD_SLABS, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 150));
        context.register(COOKING_TIME_WOOD_ITEMS_LARGE, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 200));
        context.register(COOKING_TIME_ROOTS, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 300));
        context.register(COOKING_TIME_WOOD_BLOCKS, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 300));
        context.register(COOKING_TIME_WOOD_ITEMS_SMALL, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 300));
        context.register(COOKING_TIME_HANGING_SIGNS, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 800));
        context.register(COOKING_TIME_BOATS, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 1200));
        context.register(COOKING_TIME_COAL, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 1600));
        context.register(COOKING_TIME_BLAZE_ROD, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 2400));
        context.register(COOKING_TIME_DRIED_KELP_BLOCK, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 4001));
        context.register(COOKING_TIME_COAL_BLOCK, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 16000));
        context.register(COOKING_TIME_LAVA_BUCKET, ContextIntProviders.cooking(predicates, normalBurnTime, fastBurnTime, 20000));
        context.register(BREWING_DEFAULT_USES, new ConstantValue(20));
    }

    private static ContextIntProvider compostable(HolderGetter<Block> blocks, int layerIncreaseChance) {
        if (layerIncreaseChance >= 100) {
            return new ConstantValue(1);
        }
        DispatcherProvider.Case<ContextIntProvider> emptyCase = new DispatcherProvider.Case<ContextIntProvider>(Holder.direct(MatchBlock.blockMatches(blocks, Blocks.COMPOSTER, StatePropertiesPredicate.Builder.properties().hasProperty(ComposterBlock.LEVEL, false)).build()), ContextIntProviders.exactly(1));
        WeightedList<Holder<ContextIntProvider>> cases = WeightedList.builder().add(ContextIntProviders.exactly(1), layerIncreaseChance).add(ContextIntProviders.exactly(0), 100 - layerIncreaseChance).build();
        return new NumberDispatcher(List.of(emptyCase), ContextIntProviders.weighted(cases));
    }

    private static ContextIntProvider cooking(HolderGetter<LootItemCondition> predicates, Holder.Reference<ContextIntProvider> normalBurnTimeDivisor, Holder.Reference<ContextIntProvider> fastBurnTimeDivisor, int timeSeconds) {
        Holder.Reference<LootItemCondition> fasterCookingBlocks = predicates.getOrThrow(LootPredicates.FAST_FURNACE);
        ConditionalValue fastConditional = new ConditionalValue(fasterCookingBlocks, fastBurnTimeDivisor, normalBurnTimeDivisor);
        return ContextIntProviders.div(ContextIntProviders.exactly(timeSeconds), Holder.direct(fastConditional)).value();
    }

    public static Holder<ContextIntProvider> fromFloat(Holder<ContextFloatProvider> input) {
        return Holder.direct(new FromFloat(input));
    }

    public static Holder<ContextIntProvider> binomial(int n, float p) {
        return Holder.direct(new BinomialDistributionGenerator(ContextIntProviders.exactly(n), ContextFloatProviders.exactly(p)));
    }

    public static Holder<ContextIntProvider> exactly(int value) {
        return Holder.direct(new ConstantValue(value));
    }

    public static Holder<ContextIntProvider> fromScoreboard(LootContext.EntityTarget entityTarget, String score) {
        return Holder.direct(new ScoreboardValue(ContextScoreboardNameProvider.forTarget(entityTarget), score, ContextIntProviders.exactly(0)));
    }

    @SafeVarargs
    public static Holder<ContextIntProvider> avg(Holder<ContextIntProvider> ... inputs) {
        return Holder.direct(new Average(HolderSet.direct(inputs)));
    }

    public static Holder<ContextIntProvider> sub(Holder<ContextIntProvider> left, Holder<ContextIntProvider> right) {
        return Holder.direct(new Difference(left, right));
    }

    @SafeVarargs
    public static Holder<ContextIntProvider> max(Holder<ContextIntProvider> ... inputs) {
        return Holder.direct(new Maximum(HolderSet.direct(inputs)));
    }

    @SafeVarargs
    public static Holder<ContextIntProvider> min(Holder<ContextIntProvider> ... inputs) {
        return Holder.direct(new Minimum(HolderSet.direct(inputs)));
    }

    public static Holder<ContextIntProvider> mod(Holder<ContextIntProvider> left, Holder<ContextIntProvider> right) {
        return Holder.direct(new Modulus(left, right));
    }

    public static Holder<ContextIntProvider> floorMod(Holder<ContextIntProvider> left, Holder<ContextIntProvider> right) {
        return Holder.direct(new FloorModulus(left, right));
    }

    public static Holder<ContextIntProvider> negate(Holder<ContextIntProvider> input) {
        return Holder.direct(new Negate(input));
    }

    @SafeVarargs
    public static Holder<ContextIntProvider> mul(Holder<ContextIntProvider> ... inputs) {
        return Holder.direct(new Product(HolderSet.direct(inputs)));
    }

    public static Holder<ContextIntProvider> div(Holder<ContextIntProvider> left, Holder<ContextIntProvider> right) {
        return Holder.direct(new Quotient(left, right));
    }

    public static Holder<ContextIntProvider> floorDiv(Holder<ContextIntProvider> left, Holder<ContextIntProvider> right) {
        return Holder.direct(new FloorQuotient(left, right));
    }

    @SafeVarargs
    public static Holder<ContextIntProvider> add(Holder<ContextIntProvider> ... inputs) {
        return Holder.direct(new Sum(HolderSet.direct(inputs)));
    }

    public static Holder<ContextIntProvider> between(int min, int max) {
        return Holder.direct(new UniformGenerator(ContextIntProviders.exactly(min), ContextIntProviders.exactly(max)));
    }

    public static Holder<ContextIntProvider> weighted(WeightedList<Holder<ContextIntProvider>> cases) {
        return Holder.direct(new WeightedListValue(cases));
    }
}

