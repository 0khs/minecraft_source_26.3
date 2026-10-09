/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public class SetRandomDyesFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetRandomDyesFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> SetRandomDyesFunction.commonFields(i).and((App)ContextIntProviders.CODEC.fieldOf("number_of_dyes").forGetter(f -> f.numberOfDyes)).apply((Applicative)i, SetRandomDyesFunction::new));
    private final Holder<ContextIntProvider> numberOfDyes;

    private SetRandomDyesFunction(Optional<Holder<LootItemCondition>> condition, Holder<ContextIntProvider> numberOfDyes) {
        super(condition);
        this.numberOfDyes = numberOfDyes;
    }

    public MapCodec<SetRandomDyesFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext context) {
        RandomSource random = context.getRandom();
        int rolls = this.numberOfDyes.value().getInt(context);
        if (rolls <= 0) {
            return itemStack;
        }
        ArrayList<DyeColor> dyes = new ArrayList<DyeColor>(rolls);
        for (int i = 0; i < rolls; ++i) {
            dyes.add(Util.getRandom(DyeColor.VALUES, random));
        }
        return DyedItemColor.applyDyes(itemStack, dyes);
    }

    public static LootItemConditionalFunction.Builder<?> withCount(Holder<ContextIntProvider> numberOfDyes) {
        return SetRandomDyesFunction.simpleBuilder(conditions -> new SetRandomDyesFunction((Optional<Holder<LootItemCondition>>)conditions, numberOfDyes));
    }
}

