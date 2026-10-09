/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.BrewingRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

public abstract class BrewingProvider {
    private final List<Item> containers = new ArrayList<Item>();
    private final List<ContainerTransformation> containerTransformations = new ArrayList<ContainerTransformation>();
    private final Set<Holder<Potion>> potions = new HashSet<Holder<Potion>>();
    private final RecipeOutput output;

    protected BrewingProvider(RecipeOutput output) {
        this.output = output;
    }

    protected void addContainerTransformation(Item container, Item reagent, Item output) {
        if (!this.containers.contains(container)) {
            throw new IllegalStateException("Adding a transformation for an unknown container: " + String.valueOf(container));
        }
        this.containerTransformations.add(new ContainerTransformation(container, reagent, output));
    }

    protected void addContainer(Item container) {
        this.containers.add(container);
    }

    protected void buildMix(Holder<Potion> input, Item reagent, Holder<Potion> output) {
        for (Item container : this.containers) {
            this.save(BrewingRecipeBuilder.brewingMix(container, input, reagent, output));
        }
        this.potions.add(input);
        this.potions.add(output);
    }

    protected void buildStartMix(Item reagent, Holder<Potion> output) {
        this.buildMix(Potions.WATER, reagent, Potions.MUNDANE);
        this.buildMix(Potions.AWKWARD, reagent, output);
    }

    protected void buildTransformations() {
        for (ContainerTransformation transformation : this.containerTransformations) {
            for (Holder<Potion> potion : this.potions) {
                this.save(BrewingRecipeBuilder.brewingContainerTransform(transformation.container(), potion, transformation.reagent(), transformation.output()));
            }
        }
    }

    protected void save(BrewingRecipeBuilder builder) {
        builder.save(this.output);
    }

    public final void buildRecipes() {
        this.addContainers();
        this.addContainerTransformations();
        this.buildMixes();
        this.buildTransformations();
    }

    protected abstract void addContainers();

    protected abstract void addContainerTransformations();

    protected abstract void buildMixes();

    private record ContainerTransformation(Item container, Item reagent, Item output) {
    }
}

