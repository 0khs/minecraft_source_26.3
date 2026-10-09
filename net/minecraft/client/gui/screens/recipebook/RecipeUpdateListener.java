/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.recipebook;

import net.minecraft.world.item.crafting.display.RecipeDisplay;

public interface RecipeUpdateListener {
    public void recipesUpdated();

    public void fillGhostRecipe(RecipeDisplay var1);
}

