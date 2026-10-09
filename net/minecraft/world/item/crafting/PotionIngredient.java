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
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.level.ItemLike;

public record PotionIngredient(Ingredient ingredient, Optional<PotionsPredicate> potions) implements Predicate<ItemStack>
{
    public static final MapCodec<PotionIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Ingredient.CODEC.fieldOf("item").forGetter(o -> o.ingredient), (App)PotionsPredicate.CODEC.optionalFieldOf("potion_contents").forGetter(o -> o.potions)).apply((Applicative)i, PotionIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionIngredient> STREAM_CODEC = StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC, o -> o.ingredient, ByteBufCodecs.optional(PotionsPredicate.STREAM_CODEC), o -> o.potions, PotionIngredient::new);

    @Override
    public boolean test(ItemStack input) {
        if (!this.ingredient.test(input)) {
            return false;
        }
        return this.potions.isEmpty() || this.potions.get().matches(input);
    }

    public static PotionIngredient of(Item item, PotionsPredicate potions) {
        return new PotionIngredient(Ingredient.of((ItemLike)item), Optional.of(potions));
    }

    public static PotionIngredient of(Item item) {
        return new PotionIngredient(Ingredient.of((ItemLike)item), Optional.empty());
    }

    public static boolean isPotionInput(ItemStack itemStack, RecipeAccess recipeAccess) {
        RecipePropertySet brewingInputs = recipeAccess.propertySet(RecipePropertySet.BREWING_INPUTS);
        return brewingInputs.test(itemStack) || itemStack.is(ItemTags.BREWING_POTION_INPUTS);
    }
}

