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
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ModifyContainerContents
extends LootItemConditionalFunction {
    public static final MapCodec<ModifyContainerContents> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> ModifyContainerContents.commonFields(i).and(i.group((App)ContainerComponentManipulators.CODEC.fieldOf("component").forGetter(f -> f.component), (App)LootItemFunctions.CODEC.fieldOf("modifier").forGetter(f -> f.modifier))).apply((Applicative)i, ModifyContainerContents::new));
    private final ContainerComponentManipulator<?> component;
    private final Holder<LootItemFunction> modifier;

    private ModifyContainerContents(Optional<Holder<LootItemCondition>> condition, ContainerComponentManipulator<?> component, Holder<LootItemFunction> modifier) {
        super(condition);
        this.component = component;
        this.modifier = modifier;
    }

    public MapCodec<ModifyContainerContents> codec() {
        return MAP_CODEC;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext context) {
        if (itemStack.isEmpty()) {
            return itemStack;
        }
        this.component.modifyItems(itemStack, c -> (ItemStack)this.modifier.value().apply(c, context));
        return itemStack;
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        Validatable.validateHolder(context, "modifier", this.modifier);
    }
}

