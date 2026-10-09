/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
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
import net.minecraft.world.level.storage.loot.providers.number.floats.Floor;
import net.minecraft.world.level.storage.loot.providers.number.floats.FromInt;
import net.minecraft.world.level.storage.loot.providers.number.floats.Length;
import net.minecraft.world.level.storage.loot.providers.number.floats.Maximum;
import net.minecraft.world.level.storage.loot.providers.number.floats.Minimum;
import net.minecraft.world.level.storage.loot.providers.number.floats.Modulus;
import net.minecraft.world.level.storage.loot.providers.number.floats.Negate;
import net.minecraft.world.level.storage.loot.providers.number.floats.NumberDispatcher;
import net.minecraft.world.level.storage.loot.providers.number.floats.Power;
import net.minecraft.world.level.storage.loot.providers.number.floats.Product;
import net.minecraft.world.level.storage.loot.providers.number.floats.Quotient;
import net.minecraft.world.level.storage.loot.providers.number.floats.Round;
import net.minecraft.world.level.storage.loot.providers.number.floats.Sine;
import net.minecraft.world.level.storage.loot.providers.number.floats.SquareRoot;
import net.minecraft.world.level.storage.loot.providers.number.floats.StorageValue;
import net.minecraft.world.level.storage.loot.providers.number.floats.Sum;
import net.minecraft.world.level.storage.loot.providers.number.floats.Truncate;
import net.minecraft.world.level.storage.loot.providers.number.floats.UniformGenerator;
import net.minecraft.world.level.storage.loot.providers.number.floats.WeightedListValue;

public class ContextFloatProviderTypes {
    public static MapCodec<? extends ContextFloatProvider> bootstrap(Registry<MapCodec<? extends ContextFloatProvider>> registry) {
        Registry.register(registry, "abs", Absolute.MAP_CODEC);
        Registry.register(registry, "avg", Average.MAP_CODEC);
        Registry.register(registry, "ceil", Ceiling.MAP_CODEC);
        Registry.register(registry, "conditional", ConditionalValue.MAP_CODEC);
        Registry.register(registry, "constant", ConstantValue.MAP_CODEC);
        Registry.register(registry, "cos", Cosine.MAP_CODEC);
        Registry.register(registry, "sub", Difference.MAP_CODEC);
        Registry.register(registry, "enchantment_level", EnchantmentLevelProvider.MAP_CODEC);
        Registry.register(registry, "environment_attribute", EnvironmentAttributeValue.MAP_CODEC);
        Registry.register(registry, "floor", Floor.MAP_CODEC);
        Registry.register(registry, "from_int", FromInt.MAP_CODEC);
        Registry.register(registry, "length", Length.MAP_CODEC);
        Registry.register(registry, "max", Maximum.MAP_CODEC);
        Registry.register(registry, "min", Minimum.MAP_CODEC);
        Registry.register(registry, "mod", Modulus.MAP_CODEC);
        Registry.register(registry, "negate", Negate.MAP_CODEC);
        Registry.register(registry, "number_dispatcher", NumberDispatcher.MAP_CODEC);
        Registry.register(registry, "pow", Power.MAP_CODEC);
        Registry.register(registry, "mul", Product.MAP_CODEC);
        Registry.register(registry, "div", Quotient.MAP_CODEC);
        Registry.register(registry, "round", Round.MAP_CODEC);
        Registry.register(registry, "sin", Sine.MAP_CODEC);
        Registry.register(registry, "sqrt", SquareRoot.MAP_CODEC);
        Registry.register(registry, "storage", StorageValue.MAP_CODEC);
        Registry.register(registry, "add", Sum.MAP_CODEC);
        Registry.register(registry, "truncate", Truncate.MAP_CODEC);
        Registry.register(registry, "uniform", UniformGenerator.MAP_CODEC);
        return Registry.register(registry, "weighted_list", WeightedListValue.MAP_CODEC);
    }
}

