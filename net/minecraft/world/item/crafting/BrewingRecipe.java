/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class BrewingRecipe
implements Recipe<BrewingInput> {
    public static final MapCodec<BrewingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)PotionIngredient.MAP_CODEC.fieldOf("input").forGetter(o -> o.input), (App)PotionIngredient.MAP_CODEC.fieldOf("reagent").forGetter(o -> o.reagent), (App)ItemStackTemplate.CODEC.fieldOf("output").forGetter(o -> o.output)).apply((Applicative)i, BrewingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BrewingRecipe> STREAM_CODEC = StreamCodec.composite(PotionIngredient.STREAM_CODEC, o -> o.input, PotionIngredient.STREAM_CODEC, o -> o.reagent, ItemStackTemplate.STREAM_CODEC, o -> o.output, BrewingRecipe::new);
    public static final RecipeSerializer<BrewingRecipe> SERIALIZER = new RecipeSerializer<BrewingRecipe>(MAP_CODEC, STREAM_CODEC);
    private final PotionIngredient input;
    private final PotionIngredient reagent;
    private final ItemStackTemplate output;

    public BrewingRecipe(PotionIngredient input, PotionIngredient reagent, ItemStackTemplate output) {
        this.input = input;
        this.reagent = reagent;
        this.output = output;
    }

    public PotionIngredient getInput() {
        return this.input;
    }

    public PotionIngredient getReagent() {
        return this.reagent;
    }

    public ItemStackTemplate getOutput() {
        return this.output;
    }

    @Override
    public boolean matches(BrewingInput brewingInput, Level level) {
        return this.matches(brewingInput);
    }

    public boolean matches(BrewingInput brewingInput) {
        return this.input.test(brewingInput.input()) && this.reagent.test(brewingInput.reagent());
    }

    @Override
    public ItemStack assemble(BrewingInput input) {
        return this.output.create();
    }

    @Override
    public RecipeType<BrewingRecipe> getType() {
        return RecipeType.BREWING;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<BrewingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @FunctionalInterface
    public static interface Factory<T extends BrewingRecipe> {
        public T create(Recipe.CommonInfo var1, PotionIngredient var2, PotionIngredient var3, ItemStackTemplate var4);
    }
}

