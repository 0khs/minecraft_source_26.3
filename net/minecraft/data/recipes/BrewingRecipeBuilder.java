/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.data.recipes;

import java.util.Optional;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public class BrewingRecipeBuilder
implements RecipeBuilder {
    private final PotionIngredient input;
    private final PotionIngredient reagent;
    private final ItemStackTemplate output;

    private BrewingRecipeBuilder(PotionIngredient input, PotionIngredient reagent, ItemStackTemplate output) {
        this.input = input;
        this.reagent = reagent;
        this.output = output;
    }

    private static PotionIngredient potionIngredient(Item potionContainer, Holder<Potion> potion) {
        return PotionIngredient.of(potionContainer, PotionsPredicate.ofPotion(potion));
    }

    private static ItemStackTemplate potionOutput(Item potionContainer, Holder<Potion> potion) {
        return new ItemStackTemplate(potionContainer, DataComponentPatch.builder().set(DataComponents.POTION_CONTENTS, new PotionContents(potion)).build());
    }

    public static BrewingRecipeBuilder brewingMix(Item container, Holder<Potion> inputPotion, Item reagentItem, Holder<Potion> outputPotion) {
        PotionIngredient input = BrewingRecipeBuilder.potionIngredient(container, inputPotion);
        PotionIngredient reagent = PotionIngredient.of(reagentItem);
        ItemStackTemplate output = BrewingRecipeBuilder.potionOutput(container, outputPotion);
        return new BrewingRecipeBuilder(input, reagent, output);
    }

    public static BrewingRecipeBuilder brewingContainerTransform(Item inputContainer, Holder<Potion> inputPotion, Item reagentItem, Item outputContainer) {
        PotionIngredient input = BrewingRecipeBuilder.potionIngredient(inputContainer, inputPotion);
        PotionIngredient reagent = PotionIngredient.of(reagentItem);
        ItemStackTemplate output = BrewingRecipeBuilder.potionOutput(outputContainer, inputPotion);
        return new BrewingRecipeBuilder(input, reagent, output);
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        throw new IllegalStateException("Brewing recipes cannot be unlocked");
    }

    @Override
    public BrewingRecipeBuilder group(@Nullable String group) {
        throw new IllegalStateException("Brewing recipes do not have groups");
    }

    public static Optional<Holder<Potion>> getExactPotion(PotionsPredicate predicate) {
        if (predicate.potions().isEmpty()) {
            return Optional.empty();
        }
        if (predicate.effects().isPresent()) {
            return Optional.empty();
        }
        HolderSet<Potion> potionSet = predicate.potions().get();
        if (potionSet.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(potionSet.get(0));
    }

    private Optional<Holder<Potion>> getIngredientPotion(PotionIngredient ingredient) {
        return ingredient.potions().flatMap(BrewingRecipeBuilder::getExactPotion);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        ResourceKey inputItem = (ResourceKey)this.input.ingredient().getSingleItem().flatMap(Holder::unwrapKey).orElseThrow();
        ResourceKey potionId = (ResourceKey)this.getIngredientPotion(this.input).flatMap(Holder::unwrapKey).orElseThrow();
        ResourceKey reagentItem = (ResourceKey)this.reagent.ingredient().getSingleItem().flatMap(Holder::unwrapKey).orElseThrow();
        Identifier combined = inputItem.identifier().withPath(inputPath -> "brewing/" + inputPath + "_" + potionId.identifier().getPath() + "_" + reagentItem.identifier().getPath());
        return ResourceKey.create(Registries.RECIPE, combined);
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
        BrewingRecipe recipe = new BrewingRecipe(this.input, this.reagent, this.output);
        recipeOutput.accept(id, recipe, null);
    }
}

