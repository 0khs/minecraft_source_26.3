/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import org.slf4j.Logger;

public class SetItemDamageFunction
extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SetItemDamageFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> SetItemDamageFunction.commonFields(i).and(i.group((App)ContextFloatProviders.CODEC.fieldOf("damage").forGetter(f -> f.damage), (App)Codec.BOOL.optionalFieldOf("add", (Object)false).forGetter(f -> f.add))).apply((Applicative)i, SetItemDamageFunction::new));
    private final Holder<ContextFloatProvider> damage;
    private final boolean add;

    private SetItemDamageFunction(Optional<Holder<LootItemCondition>> condition, Holder<ContextFloatProvider> damage, boolean add) {
        super(condition);
        this.damage = damage;
        this.add = add;
    }

    public MapCodec<SetItemDamageFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        Validatable.validateHolder(context, "damage", this.damage);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext context) {
        if (itemStack.isDamageableItem()) {
            int maxDamage = itemStack.getMaxDamage();
            float base = this.add ? 1.0f - (float)itemStack.getDamageValue() / (float)maxDamage : 0.0f;
            float pct = 1.0f - Mth.clamp(this.damage.value().getFloat(context) + base, 0.0f, 1.0f);
            itemStack.setDamageValue(Mth.floor(pct * (float)maxDamage));
        } else {
            LOGGER.warn("Couldn't set damage of loot item {}", (Object)itemStack);
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(Holder<ContextFloatProvider> value) {
        return SetItemDamageFunction.simpleBuilder(conditions -> new SetItemDamageFunction((Optional<Holder<LootItemCondition>>)conditions, value, false));
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(Holder<ContextFloatProvider> value, boolean add) {
        return SetItemDamageFunction.simpleBuilder(conditions -> new SetItemDamageFunction((Optional<Holder<LootItemCondition>>)conditions, value, add));
    }
}

