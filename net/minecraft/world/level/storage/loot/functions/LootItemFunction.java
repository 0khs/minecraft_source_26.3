/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface LootItemFunction
extends LootContextUser,
BiFunction<ItemStack, LootContext, ItemStack> {
    public MapCodec<? extends LootItemFunction> codec();

    public static Consumer<ItemStack> decorate(Optional<Holder<LootItemFunction>> maybeFunction, Consumer<ItemStack> output, LootContext context) {
        if (maybeFunction.isPresent()) {
            Holder<LootItemFunction> function = maybeFunction.get();
            return drop -> output.accept((ItemStack)((LootItemFunction)function.value()).apply(drop, context));
        }
        return output;
    }

    public static interface Builder {
        public LootItemFunction build();
    }
}

