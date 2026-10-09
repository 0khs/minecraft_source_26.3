/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.triggers;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class SimpleCriterionTrigger<T extends SimpleInstance>
implements CriterionTrigger<T> {
    protected void trigger(ServerPlayer player, Predicate<T> matcher) {
        PlayerAdvancements advancements = player.getAdvancements();
        Map listenersForType = advancements.getTriggerMapForType(this);
        if (listenersForType == null || listenersForType.isEmpty()) {
            return;
        }
        LootContext playerContext = EntityPredicate.createContext(player, player);
        ArrayList<PlayerAdvancements.TriggerInstanceKey> matchedConditions = null;
        for (Map.Entry entry : listenersForType.entrySet()) {
            Optional<Holder<LootItemCondition>> predicate;
            SimpleInstance value = (SimpleInstance)entry.getValue();
            if (!matcher.test(value) || (predicate = value.player()).isPresent() && !predicate.get().value().test(playerContext)) continue;
            if (matchedConditions == null) {
                matchedConditions = new ArrayList<PlayerAdvancements.TriggerInstanceKey>();
            }
            matchedConditions.add(entry.getKey());
        }
        if (matchedConditions != null) {
            for (PlayerAdvancements.TriggerInstanceKey criterion : matchedConditions) {
                advancements.award(criterion.advancement(), criterion.criterion());
            }
        }
    }

    public static interface SimpleInstance
    extends CriterionTriggerInstance {
        @Override
        default public void validate(ValidationContextSource validator) {
            Validatable.validateHolder(validator.entityContext(), "player", this.player());
        }

        public Optional<Holder<LootItemCondition>> player();
    }
}

