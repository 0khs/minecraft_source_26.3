/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record LootItemRandomChanceWithEnchantedBonusCondition(float unenchantedChance, LevelBasedValue enchantedChance, Holder<Enchantment> enchantment) implements LootItemCondition
{
    public static final MapCodec<LootItemRandomChanceWithEnchantedBonusCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("unenchanted_chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::unenchantedChance), (App)LevelBasedValue.CODEC.fieldOf("enchanted_chance").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::enchantedChance), (App)Enchantment.CODEC.fieldOf("enchantment").forGetter(LootItemRandomChanceWithEnchantedBonusCondition::enchantment)).apply((Applicative)i, LootItemRandomChanceWithEnchantedBonusCondition::new));

    public MapCodec<LootItemRandomChanceWithEnchantedBonusCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ATTACKING_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        int n;
        Entity killerEntity = context.getOptional(LootContextParams.ATTACKING_ENTITY);
        if (killerEntity instanceof LivingEntity) {
            LivingEntity livingKiller = (LivingEntity)killerEntity;
            n = EnchantmentHelper.getEnchantmentLevel(this.enchantment, livingKiller);
        } else {
            n = 0;
        }
        int enchantmentLevel = n;
        float chance = enchantmentLevel > 0 ? this.enchantedChance.calculate(enchantmentLevel) : this.unenchantedChance;
        return context.getRandom().nextFloat() < chance;
    }

    public static LootItemCondition.Builder randomChanceAndLootingBoost(HolderGetter<Enchantment> enchantments, float chance, float perEnchantmentLevel) {
        return () -> new LootItemRandomChanceWithEnchantedBonusCondition(chance, new LevelBasedValue.Linear(chance + perEnchantmentLevel, perEnchantmentLevel), enchantments.getOrThrow(Enchantments.LOOTING));
    }
}

