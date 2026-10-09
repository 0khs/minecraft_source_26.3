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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LightningStrikeTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, LightningBolt lightning, List<Entity> entitiesAround) {
        List entitiesAroundContexts = entitiesAround.stream().map(v -> EntityPredicate.createContext(player, v)).collect(Collectors.toList());
        LootContext lightningContext = EntityPredicate.createContext(player, lightning);
        this.trigger(player, t -> t.matches(lightningContext, entitiesAroundContexts));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, Optional<Holder<LootItemCondition>> lightning, Optional<Holder<LootItemCondition>> bystander) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group((App)LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)LootItemCondition.CODEC.optionalFieldOf("lightning").forGetter(TriggerInstance::lightning), (App)LootItemCondition.CODEC.optionalFieldOf("bystander").forGetter(TriggerInstance::bystander)).apply((Applicative)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> lightningStrike(Optional<EntityPredicate> lightning, Optional<EntityPredicate> bystander) {
            return CriteriaTriggers.LIGHTNING_STRIKE.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap(lightning), EntityPredicate.wrap(bystander)));
        }

        public boolean matches(LootContext bolt, List<LootContext> entitiesAround) {
            if (this.lightning.isPresent() && !this.lightning.get().value().test(bolt)) {
                return false;
            }
            return !this.bystander.isPresent() || !entitiesAround.stream().noneMatch(this.bystander.get().value());
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            Validatable.validateHolder(validator.entityContext(), "lightning", this.lightning);
            Validatable.validateHolder(validator.entityContext(), "bystander", this.bystander);
        }
    }
}

