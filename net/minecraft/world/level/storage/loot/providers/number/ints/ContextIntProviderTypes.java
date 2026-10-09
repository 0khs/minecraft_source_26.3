/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.providers.number.ints.Absolute;
import net.minecraft.world.level.storage.loot.providers.number.ints.Average;
import net.minecraft.world.level.storage.loot.providers.number.ints.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConditionalValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.Difference;
import net.minecraft.world.level.storage.loot.providers.number.ints.EnvironmentAttributeValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.FloorModulus;
import net.minecraft.world.level.storage.loot.providers.number.ints.FloorQuotient;
import net.minecraft.world.level.storage.loot.providers.number.ints.FromFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.Maximum;
import net.minecraft.world.level.storage.loot.providers.number.ints.Minimum;
import net.minecraft.world.level.storage.loot.providers.number.ints.Modulus;
import net.minecraft.world.level.storage.loot.providers.number.ints.Negate;
import net.minecraft.world.level.storage.loot.providers.number.ints.NumberDispatcher;
import net.minecraft.world.level.storage.loot.providers.number.ints.Power;
import net.minecraft.world.level.storage.loot.providers.number.ints.Product;
import net.minecraft.world.level.storage.loot.providers.number.ints.Quotient;
import net.minecraft.world.level.storage.loot.providers.number.ints.ScoreboardValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.StorageValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.Sum;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ints.WeightedListValue;

public class ContextIntProviderTypes {
    public static MapCodec<? extends ContextIntProvider> bootstrap(Registry<MapCodec<? extends ContextIntProvider>> registry) {
        Registry.register(registry, "abs", Absolute.MAP_CODEC);
        Registry.register(registry, "avg", Average.MAP_CODEC);
        Registry.register(registry, "binomial", BinomialDistributionGenerator.MAP_CODEC);
        Registry.register(registry, "conditional", ConditionalValue.MAP_CODEC);
        Registry.register(registry, "constant", ConstantValue.MAP_CODEC);
        Registry.register(registry, "sub", Difference.MAP_CODEC);
        Registry.register(registry, "environment_attribute", EnvironmentAttributeValue.MAP_CODEC);
        Registry.register(registry, "from_float", FromFloat.MAP_CODEC);
        Registry.register(registry, "max", Maximum.MAP_CODEC);
        Registry.register(registry, "min", Minimum.MAP_CODEC);
        Registry.register(registry, "floor_mod", FloorModulus.MAP_CODEC);
        Registry.register(registry, "floor_div", FloorQuotient.MAP_CODEC);
        Registry.register(registry, "mod", Modulus.MAP_CODEC);
        Registry.register(registry, "div", Quotient.MAP_CODEC);
        Registry.register(registry, "negate", Negate.MAP_CODEC);
        Registry.register(registry, "number_dispatcher", NumberDispatcher.MAP_CODEC);
        Registry.register(registry, "pow", Power.MAP_CODEC);
        Registry.register(registry, "mul", Product.MAP_CODEC);
        Registry.register(registry, "score", ScoreboardValue.MAP_CODEC);
        Registry.register(registry, "storage", StorageValue.MAP_CODEC);
        Registry.register(registry, "add", Sum.MAP_CODEC);
        Registry.register(registry, "uniform", UniformGenerator.MAP_CODEC);
        return Registry.register(registry, "weighted_list", WeightedListValue.MAP_CODEC);
    }
}

