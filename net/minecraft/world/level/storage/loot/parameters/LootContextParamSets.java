/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootContextParamSets {
    public static final Codec<ContextKeySet> CODEC = BuiltInRegistries.CONTEXT_KEY_SET.byNameCodec();
    public static final ContextKeySet EMPTY = LootContextParamSets.register("empty", (ContextKeySet.Builder builder) -> {});
    public static final ContextKeySet ALL_PARAMS = LootContextParamSets.register("generic", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.LAST_DAMAGE_PLAYER).required(LootContextParams.DAMAGE_SOURCE).required(LootContextParams.ATTACKING_ENTITY).required(LootContextParams.DIRECT_ATTACKING_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE).required(LootContextParams.BLOCK_ENTITY).required(LootContextParams.TOOL).required(LootContextParams.EXPLOSION_RADIUS).required(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED).required(LootContextParams.CONTAINER).required(LootContextParams.INTERACTING_ENTITY).required(LootContextParams.TARGET_ENTITY).required(LootContextParams.ENCHANTMENT_ACTIVE).required(LootContextParams.ENCHANTMENT_LEVEL));
    public static final ContextKeySet CHEST = LootContextParamSets.register("chest", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
    public static final ContextKeySet COMMAND = LootContextParamSets.register("command", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY));
    public static final ContextKeySet COMMAND_SLOT_SOURCE = LootContextParamSets.register("command_slot_source", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.CONTAINER).optional(LootContextParams.THIS_ENTITY));
    public static final ContextKeySet COMMAND_COMPUTE_DEFAULT = LootContextParamSets.register("command_compute_default", (ContextKeySet.Builder builder) -> builder.optional(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN));
    public static final ContextKeySet COMMAND_COMPUTE_POSITION = LootContextParamSets.register("command_compute_position", (ContextKeySet.Builder builder) -> builder.optional(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE).optional(LootContextParams.BLOCK_ENTITY));
    public static final ContextKeySet COMMAND_COMPUTE_ENTITY = LootContextParamSets.register("command_compute_entity", (ContextKeySet.Builder builder) -> builder.optional(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.TARGET_ENTITY));
    public static final ContextKeySet SELECTOR = LootContextParamSets.register("selector", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
    public static final ContextKeySet VILLAGER_TRADE = LootContextParamSets.register("villager_trade", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED));
    public static final ContextKeySet FISHING = LootContextParamSets.register("fishing", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).optional(LootContextParams.THIS_ENTITY));
    public static final ContextKeySet ENTITY = LootContextParamSets.register("entity", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.DAMAGE_SOURCE).optional(LootContextParams.ATTACKING_ENTITY).optional(LootContextParams.DIRECT_ATTACKING_ENTITY).optional(LootContextParams.LAST_DAMAGE_PLAYER));
    public static final ContextKeySet EQUIPMENT = LootContextParamSets.register("equipment", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
    public static final ContextKeySet ARCHAEOLOGY = LootContextParamSets.register("archaeology", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.TOOL));
    public static final ContextKeySet GIFT = LootContextParamSets.register("gift", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY));
    public static final ContextKeySet PIGLIN_BARTER = LootContextParamSets.register("barter", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY));
    public static final ContextKeySet VAULT = LootContextParamSets.register("vault", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).optional(LootContextParams.THIS_ENTITY).optional(LootContextParams.TOOL));
    public static final ContextKeySet ADVANCEMENT_REWARD = LootContextParamSets.register("advancement_reward", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN));
    public static final ContextKeySet ADVANCEMENT_ENTITY = LootContextParamSets.register("advancement_entity", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN));
    public static final ContextKeySet ADVANCEMENT_LOCATION = LootContextParamSets.register("advancement_location", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).required(LootContextParams.BLOCK_STATE).optional(LootContextParams.BLOCK_ENTITY));
    public static final ContextKeySet BLOCK_USE = LootContextParamSets.register("block_use", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE));
    public static final ContextKeySet BLOCK = LootContextParamSets.register("block", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.BLOCK_STATE).required(LootContextParams.ORIGIN).required(LootContextParams.TOOL).optional(LootContextParams.THIS_ENTITY).optional(LootContextParams.BLOCK_ENTITY).optional(LootContextParams.EXPLOSION_RADIUS));
    public static final ContextKeySet SHEARING = LootContextParamSets.register("shearing", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY).required(LootContextParams.TOOL));
    public static final ContextKeySet ENTITY_INTERACT = LootContextParamSets.register("entity_interact", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.TARGET_ENTITY).optional(LootContextParams.INTERACTING_ENTITY).required(LootContextParams.TOOL));
    public static final ContextKeySet BLOCK_INTERACT = LootContextParamSets.register("block_interact", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.BLOCK_STATE).required(LootContextParams.ORIGIN).optional(LootContextParams.BLOCK_ENTITY).optional(LootContextParams.INTERACTING_ENTITY).optional(LootContextParams.TOOL));
    public static final ContextKeySet CONTAINER_PROCESS = LootContextParamSets.register("container_process", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.BLOCK_ENTITY).required(LootContextParams.BLOCK_STATE).required(LootContextParams.CONTAINER).required(LootContextParams.ORIGIN));
    public static final ContextKeySet ENCHANTED_DAMAGE = LootContextParamSets.register("enchanted_damage", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN).required(LootContextParams.DAMAGE_SOURCE).optional(LootContextParams.DIRECT_ATTACKING_ENTITY).optional(LootContextParams.ATTACKING_ENTITY));
    public static final ContextKeySet ENCHANTED_ITEM = LootContextParamSets.register("enchanted_item", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.TOOL).required(LootContextParams.ENCHANTMENT_LEVEL));
    public static final ContextKeySet ENCHANTED_LOCATION = LootContextParamSets.register("enchanted_location", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN).required(LootContextParams.ENCHANTMENT_ACTIVE));
    public static final ContextKeySet ENCHANTED_ENTITY = LootContextParamSets.register("enchanted_entity", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN));
    public static final ContextKeySet HIT_BLOCK = LootContextParamSets.register("hit_block", (ContextKeySet.Builder builder) -> builder.required(LootContextParams.THIS_ENTITY).required(LootContextParams.ENCHANTMENT_LEVEL).required(LootContextParams.ORIGIN).required(LootContextParams.BLOCK_STATE));

    private static ContextKeySet register(String name, Consumer<ContextKeySet.Builder> consumer) {
        ResourceKey<ContextKeySet> key = ResourceKey.create(Registries.CONTEXT_KEY_SET, Identifier.withDefaultNamespace(name));
        return LootContextParamSets.register(consumer, key);
    }

    private static ContextKeySet register(Consumer<ContextKeySet.Builder> consumer, ResourceKey<ContextKeySet> key) {
        ContextKeySet.Builder builder = new ContextKeySet.Builder();
        consumer.accept(builder);
        return Registry.register(BuiltInRegistries.CONTEXT_KEY_SET, key, builder.build());
    }

    public static ContextKeySet bootstrap(Registry<ContextKeySet> registry) {
        return ALL_PARAMS;
    }

    public static void validate() {
        HashSet allParams = new HashSet();
        BuiltInRegistries.CONTEXT_KEY_SET.forEach(paramSet -> {
            if (paramSet != ALL_PARAMS) {
                allParams.addAll(paramSet.allowed());
            }
        });
        Sets.SetView missingFromAllParams = Sets.difference(allParams, ALL_PARAMS.required());
        if (!missingFromAllParams.isEmpty()) {
            throw new IllegalStateException("Missing parameters from 'all_params': " + String.valueOf(missingFromAllParams));
        }
    }
}

