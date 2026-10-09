/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.triggers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RecipeUnlockedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    @Override
    public void trigger(ServerPlayer player, RecipeHolder<?> recipe) {
        ((SimpleCriterionTrigger)this).trigger(player, (T t) -> t.matches(recipe));
    }

    public static Criterion<TriggerInstance> unlocked(Holder<Recipe<?>> recipe) {
        return RecipeUnlockedTrigger.unlocked(HolderSet.direct(recipe));
    }

    public static Criterion<TriggerInstance> unlocked(HolderSet<Recipe<?>> recipe) {
        return CriteriaTriggers.RECIPE_UNLOCKED.createCriterion(new TriggerInstance(Optional.empty(), recipe));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, HolderSet<Recipe<?>> recipes) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group((App)LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)Recipe.LIST_CODEC.fieldOf("recipes").forGetter(TriggerInstance::recipes)).apply((Applicative)i, TriggerInstance::new));

        public boolean matches(RecipeHolder<?> recipe) {
            return this.recipes.stream().anyMatch(holder -> holder.is(recipe.id()));
        }
    }
}

