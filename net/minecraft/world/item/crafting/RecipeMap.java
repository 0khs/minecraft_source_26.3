/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class RecipeMap {
    public static final RecipeMap EMPTY = new RecipeMap((Multimap<RecipeType<?>, RecipeHolder<?>>)ImmutableMultimap.of(), Map.of());
    private final Multimap<RecipeType<?>, RecipeHolder<?>> byType;
    private final Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey;

    private RecipeMap(Multimap<RecipeType<?>, RecipeHolder<?>> byType, Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey) {
        this.byType = byType;
        this.byKey = byKey;
    }

    public static RecipeMap create(HolderLookup<Recipe<?>> recipes) {
        ImmutableMultimap.Builder byType = ImmutableMultimap.builder();
        ImmutableMap.Builder byKey = ImmutableMap.builder();
        recipes.listElements().forEach(recipe -> {
            RecipeHolder<Recipe> legacyRecipeHolder = new RecipeHolder<Recipe>(recipe.key(), (Recipe)recipe.value());
            byType.put(((Recipe)recipe.value()).getType(), legacyRecipeHolder);
            byKey.put(recipe.key(), legacyRecipeHolder);
        });
        return new RecipeMap((Multimap<RecipeType<?>, RecipeHolder<?>>)byType.build(), (Map<ResourceKey<Recipe<?>>, RecipeHolder<?>>)byKey.build());
    }

    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type) {
        return this.byType.get(type);
    }

    public Collection<RecipeHolder<?>> values() {
        return this.byKey.values();
    }

    public @Nullable RecipeHolder<?> byKey(ResourceKey<Recipe<?>> recipeId) {
        return this.byKey.get(recipeId);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getRecipesFor(RecipeType<T> type, I container, Level level) {
        if (container.isEmpty()) {
            return Stream.empty();
        }
        return this.byType(type).stream().filter(r -> r.value().matches((RecipeInput)container, level));
    }
}

