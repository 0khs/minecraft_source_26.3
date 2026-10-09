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
import java.util.Optional;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.phys.Vec3;

public class ItemUsedOnLocationTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos pos, ItemInstance tool) {
        ServerLevel level = player.level();
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.THIS_ENTITY, player).withParameter(LootContextParams.BLOCK_STATE, state).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withParameter(LootContextParams.TOOL, tool).create(LootContextParamSets.ADVANCEMENT_LOCATION);
        LootContext context = new LootContext.Builder(params).create(Optional.empty());
        this.trigger(player, t -> t.matches(context));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, Optional<Holder<LootItemCondition>> location) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group((App)LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)LootItemCondition.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location)).apply((Applicative)i, TriggerInstance::new));

        public static Criterion<TriggerInstance> placedBlock(HolderGetter<Block> blocks, Block block) {
            Holder<LootItemCondition> location = Holder.direct(MatchBlock.blockMatches(blocks, block).build());
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(location)));
        }

        public static Criterion<TriggerInstance> placedBlock(LootItemCondition.Builder locationCondition) {
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(Holder.direct(locationCondition.build()))));
        }

        public static <T extends Comparable<T>> Criterion<TriggerInstance> placedBlockWithProperties(HolderGetter<Block> blocks, Block block, Property<T> property, String propertyValue) {
            StatePropertiesPredicate.Builder predicateBuilder = StatePropertiesPredicate.Builder.properties().hasProperty(property, propertyValue);
            Holder<LootItemCondition> location = Holder.direct(MatchBlock.blockMatches(blocks, block, predicateBuilder).build());
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(location)));
        }

        public static Criterion<TriggerInstance> placedBlockWithProperties(HolderGetter<Block> blocks, Block block, Property<Boolean> property, boolean propertyValue) {
            return TriggerInstance.placedBlockWithProperties(blocks, block, property, String.valueOf(propertyValue));
        }

        public static Criterion<TriggerInstance> placedBlockWithProperties(HolderGetter<Block> blocks, Block block, Property<Integer> property, int propertyValue) {
            return TriggerInstance.placedBlockWithProperties(blocks, block, property, String.valueOf(propertyValue));
        }

        public static <T extends Comparable<T> & StringRepresentable> Criterion<TriggerInstance> placedBlockWithProperties(HolderGetter<Block> blocks, Block block, Property<T> properties, T propertyValue) {
            return TriggerInstance.placedBlockWithProperties(blocks, block, properties, ((StringRepresentable)propertyValue).getSerializedName());
        }

        private static TriggerInstance itemUsedOnLocation(LocationPredicate.Builder location, ItemPredicate.Builder item) {
            Holder<LootItemCondition> predicate = Holder.direct(AllOfCondition.allOf(LocationCheck.checkLocation(location), MatchTool.toolMatches(item)).build());
            return new TriggerInstance(Optional.empty(), Optional.of(predicate));
        }

        public static Criterion<TriggerInstance> itemUsedOnBlock(LocationPredicate.Builder location, ItemPredicate.Builder item) {
            return CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(TriggerInstance.itemUsedOnLocation(location, item));
        }

        public static Criterion<TriggerInstance> allayDropItemOnBlock(LocationPredicate.Builder location, ItemPredicate.Builder item) {
            return CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.createCriterion(TriggerInstance.itemUsedOnLocation(location, item));
        }

        public boolean matches(LootContext locationContext) {
            return this.location.isEmpty() || this.location.get().value().test(locationContext);
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(validator);
            Validatable.validateHolder(validator.context(LootContextParamSets.ADVANCEMENT_LOCATION), "location", this.location);
        }
    }
}

