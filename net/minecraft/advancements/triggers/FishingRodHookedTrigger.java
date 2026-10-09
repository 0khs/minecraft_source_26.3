/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.triggers;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FishingRodHookedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack rod, FishingHook hook, Collection<ItemStack> items) {
        LootContext hookedInContext = EntityPredicate.createContext(player, hook.getHookedIn() != null ? hook.getHookedIn() : hook);
        this.trigger(player, t -> t.matches(rod, hookedInContext, items));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, Optional<ItemPredicate> rod, Optional<Holder<LootItemCondition>> entity, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group((App)LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ItemPredicate.CODEC.optionalFieldOf("rod").forGetter(TriggerInstance::rod), (App)LootItemCondition.CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity), (App)ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)).apply((Applicative)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> fishedItem(Optional<ItemPredicate> rod, Optional<EntityPredicate> entity, Optional<ItemPredicate> item) {
            return CriteriaTriggers.FISHING_ROD_HOOKED.createCriterion(new TriggerInstance(Optional.empty(), rod, EntityPredicate.wrap(entity), item));
        }

        public boolean matches(ItemStack rod, LootContext hookedIn, Collection<ItemStack> items) {
            if (this.rod.isPresent() && !this.rod.get().test(rod)) {
                return false;
            }
            if (this.entity.isPresent() && !this.entity.get().value().test(hookedIn)) {
                return false;
            }
            if (this.item.isPresent()) {
                boolean matched = false;
                Entity hookedInEntity = hookedIn.getOptional(LootContextParams.THIS_ENTITY);
                if (hookedInEntity instanceof ItemEntity) {
                    ItemEntity item = (ItemEntity)hookedInEntity;
                    if (this.item.get().test(item.getItem())) {
                        matched = true;
                    }
                }
                for (ItemStack item : items) {
                    if (!this.item.get().test(item)) continue;
                    matched = true;
                    break;
                }
                if (!matched) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            Validatable.validateHolder(validator.entityContext(), "entity", this.entity);
        }
    }
}

