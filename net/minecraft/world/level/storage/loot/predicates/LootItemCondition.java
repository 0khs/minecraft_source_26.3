/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;

public interface LootItemCondition
extends LootContextUser,
Predicate<LootContext> {
    public static final Codec<LootItemCondition> DIRECT_CODEC = BuiltInRegistries.LOOT_CONDITION_TYPE.byNameCodec().dispatch(LootItemCondition::codec, c -> c);
    public static final Codec<Holder<LootItemCondition>> CODEC = RegistryCodecs.holder(Registries.PREDICATE, DIRECT_CODEC);
    public static final Codec<HolderSet<LootItemCondition>> LIST_CODEC = RegistryCodecs.holderSet(Registries.PREDICATE, DIRECT_CODEC);

    public MapCodec<? extends LootItemCondition> codec();

    @FunctionalInterface
    public static interface Builder {
        public LootItemCondition build();

        default public Builder invert() {
            return InvertedLootItemCondition.invert(this);
        }

        default public AnyOfCondition.Builder or(Builder other) {
            return AnyOfCondition.anyOf(this, other);
        }

        default public AllOfCondition.Builder and(Builder other) {
            return AllOfCondition.allOf(this, other);
        }
    }
}

