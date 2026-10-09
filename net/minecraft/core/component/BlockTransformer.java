/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public record BlockTransformer(List<BlockTransformData> transforms) {
    public static final Codec<BlockTransformer> DIRECT_CODEC = BlockTransformData.CODEC.listOf(1, 200).xmap(BlockTransformer::new, BlockTransformer::transforms);
    public static final Codec<Holder<BlockTransformer>> CODEC = RegistryCodecs.holder(Registries.BLOCK_TRANSFORMER);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<BlockTransformer>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.BLOCK_TRANSFORMER);

    public InteractionResult transformBlock(UseOnContext context) {
        if (BlockTransformer.playerHasBlockingItemUseIntent(context)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        for (BlockTransformData transformData : this.transforms) {
            BlockState newBlockState;
            Direction clickedFace = context.getClickedFace();
            if (transformData.disallowedFaces().contains(clickedFace) || (newBlockState = transformData.blockStateProvider.value().getOptionalState(level, level.getRandom(), pos)) == null) continue;
            BlockState updatedShape = transformData.updateFromNeighbors ? Block.updateFromNeighbourShapes(newBlockState, level, pos) : newBlockState;
            Player player = context.getPlayer();
            ItemStack itemInHand = context.getItemInHand();
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemInHand);
            }
            BlockState oldBlockState = level.getBlockState(pos);
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                transformData.loot.ifPresent(lt -> Block.dropFromBlockInteractLootTable(serverLevel, lt, pos, oldBlockState, level.getBlockEntity(pos), itemInHand, player, (sl, stack) -> transformData.dropStrategy.resourcePopper.pop((Level)sl, pos, clickedFace, (ItemStack)stack)));
            }
            if (itemInHand.isStackable()) {
                itemInHand.consume(transformData.consumeOnUse ? 1 : 0, player);
            } else if (player != null) {
                itemInHand.hurtAndBreak(transformData.itemDamagePerUse, (LivingEntity)player, context.getHand().asEquipmentSlot());
            }
            level.setBlock(pos, updatedShape, 11);
            level.playSound((Entity)player, pos, transformData.sound.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            transformData.particle.send(level, player, pos);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, updatedShape));
            if (transformData.transformType == TransformType.COPPER_CHEST && oldBlockState.getBlock() instanceof CopperChestBlock && oldBlockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos neighborPos = ChestBlock.getConnectedBlockPos(pos, oldBlockState);
                level.gameEvent(GameEvent.BLOCK_CHANGE, neighborPos, GameEvent.Context.of(player, level.getBlockState(neighborPos)));
                transformData.particle.send(level, player, neighborPos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static boolean playerHasBlockingItemUseIntent(UseOnContext context) {
        Player player = context.getPlayer();
        return context.getHand().equals((Object)InteractionHand.MAIN_HAND) && player.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS) && !player.isSecondaryUseActive();
    }

    public record BlockTransformData(Holder<BlockStateProvider> blockStateProvider, Holder<SoundEvent> sound, TransformParticle particle, List<Direction> disallowedFaces, Optional<ResourceKey<LootTable>> loot, DropStrategy dropStrategy, boolean updateFromNeighbors, TransformType transformType, boolean consumeOnUse, int itemDamagePerUse) {
        public static final Codec<BlockTransformData> CODEC = RecordCodecBuilder.create(i -> i.group((App)BlockStateProvider.CODEC.fieldOf("block_state_provider").forGetter(BlockTransformData::blockStateProvider), (App)SoundEvent.CODEC.optionalFieldOf("sound", BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY)).forGetter(BlockTransformData::sound), (App)TransformParticle.CODEC.optionalFieldOf("particle", (Object)TransformParticle.NONE).forGetter(BlockTransformData::particle), (App)Direction.CODEC.listOf().optionalFieldOf("disallowed_faces", List.of()).forGetter(BlockTransformData::disallowedFaces), (App)LootTable.KEY_CODEC.optionalFieldOf("loot").forGetter(BlockTransformData::loot), (App)DropStrategy.CODEC.optionalFieldOf("drop_strategy", (Object)DropStrategy.FROM_MIDDLE).forGetter(BlockTransformData::dropStrategy), (App)Codec.BOOL.optionalFieldOf("update_from_neighbors", (Object)true).forGetter(BlockTransformData::updateFromNeighbors), (App)TransformType.CODEC.optionalFieldOf("transform_type", (Object)TransformType.SINGLE_BLOCK).forGetter(BlockTransformData::transformType), (App)Codec.BOOL.optionalFieldOf("consume_on_use", (Object)true).forGetter(BlockTransformData::consumeOnUse), (App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("item_damage_per_use", (Object)0).forGetter(BlockTransformData::itemDamagePerUse)).apply((Applicative)i, BlockTransformData::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, BlockTransformData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.fromCodecWithRegistries(BlockStateProvider.CODEC), BlockTransformData::blockStateProvider, SoundEvent.STREAM_CODEC, BlockTransformData::sound, TransformParticle.STREAM_CODEC, BlockTransformData::particle, Direction.STREAM_CODEC.apply(ByteBufCodecs.list()), BlockTransformData::disallowedFaces, ResourceKey.streamCodec(Registries.LOOT_TABLE).apply(ByteBufCodecs::optional), BlockTransformData::loot, DropStrategy.STREAM_CODEC, BlockTransformData::dropStrategy, ByteBufCodecs.BOOL, BlockTransformData::updateFromNeighbors, TransformType.STREAM_CODEC, BlockTransformData::transformType, ByteBufCodecs.BOOL, BlockTransformData::consumeOnUse, ByteBufCodecs.VAR_INT, BlockTransformData::itemDamagePerUse, BlockTransformData::new);

        public static Builder builder(Holder<BlockStateProvider> targetStateProvider) {
            return new Builder(targetStateProvider);
        }

        public static Builder builder(BlockStateProvider targetStateProvider) {
            return BlockTransformData.builder(Holder.direct(targetStateProvider));
        }

        public static Builder builder(BlockPredicate predicate, Block block) {
            return BlockTransformData.builder(RuleBasedStateProvider.builder().ifTrueThenProvide(predicate, block).build());
        }

        public static class Builder {
            private final Holder<BlockStateProvider> targetStateProvider;
            private Holder<SoundEvent> sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.EMPTY);
            private TransformParticle particle = TransformParticle.NONE;
            private List<Direction> disallowedFaces = List.of();
            private Optional<ResourceKey<LootTable>> loot = Optional.empty();
            private DropStrategy dropStrategy = DropStrategy.FROM_MIDDLE;
            private boolean updateFromNeighbors = true;
            private TransformType transformType = TransformType.SINGLE_BLOCK;
            private boolean consumeOnUse = true;
            private int itemDamagePerUse = 1;

            private Builder(Holder<BlockStateProvider> targetStateProvider) {
                this.targetStateProvider = targetStateProvider;
            }

            public Builder sound(Holder<SoundEvent> sound) {
                this.sound = sound;
                return this;
            }

            public Builder particle(TransformParticle particle) {
                this.particle = particle;
                return this;
            }

            public Builder disallowedFaces(List<Direction> disallowedFaces) {
                this.disallowedFaces = disallowedFaces;
                return this;
            }

            public Builder loot(ResourceKey<LootTable> loot) {
                this.loot = Optional.of(loot);
                return this;
            }

            public Builder dropStrategy(DropStrategy dropStrategy) {
                this.dropStrategy = dropStrategy;
                return this;
            }

            public Builder updateFromNeighbors(boolean updateFromNeighbors) {
                this.updateFromNeighbors = updateFromNeighbors;
                return this;
            }

            public Builder transformType(TransformType transformType) {
                this.transformType = transformType;
                return this;
            }

            public Builder consumeOnUse(boolean consumeOnUse) {
                this.consumeOnUse = consumeOnUse;
                return this;
            }

            public Builder itemDamagePerUse(int itemDamagePerUse) {
                this.itemDamagePerUse = itemDamagePerUse;
                return this;
            }

            public BlockTransformData build() {
                return new BlockTransformData(this.targetStateProvider, this.sound, this.particle, this.disallowedFaces, this.loot, this.dropStrategy, this.updateFromNeighbors, this.transformType, this.consumeOnUse, this.itemDamagePerUse);
            }
        }
    }

    public static enum TransformParticle implements StringRepresentable
    {
        NONE(0, "none", 0),
        SCRAPE(1, "scrape", 3005),
        WAX_ON(2, "wax_on", 3003),
        WAX_OFF(3, "wax_off", 3004);

        private final int id;
        private final String name;
        private final @LevelEvent.Value int levelEvent;
        public static final Codec<TransformParticle> CODEC;
        private static final IntFunction<TransformParticle> BY_ID;
        public static final StreamCodec<ByteBuf, TransformParticle> STREAM_CODEC;

        private TransformParticle(@LevelEvent.Value int id, String name, int levelEvent) {
            this.id = id;
            this.name = name;
            this.levelEvent = levelEvent;
        }

        public void send(Level level, @Nullable Entity player, BlockPos pos) {
            if (!this.equals(NONE)) {
                level.levelEvent(player, this.getLevelEvent(), pos, 0);
            }
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public @LevelEvent.Value int getLevelEvent() {
            return this.levelEvent;
        }

        static {
            CODEC = StringRepresentable.fromValues(TransformParticle::values);
            BY_ID = ByIdMap.continuous(TransformParticle::getId, TransformParticle.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TransformParticle::getId);
        }
    }

    public static enum TransformType implements StringRepresentable
    {
        SINGLE_BLOCK(0, "single_block"),
        COPPER_CHEST(1, "copper_chest");

        private final int id;
        private final String name;
        public static final Codec<TransformType> CODEC;
        private static final IntFunction<TransformType> BY_ID;
        public static final StreamCodec<ByteBuf, TransformType> STREAM_CODEC;

        private TransformType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        static {
            CODEC = StringRepresentable.fromValues(TransformType::values);
            BY_ID = ByIdMap.continuous(TransformType::getId, TransformType.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TransformType::getId);
        }
    }

    public static enum DropStrategy implements StringRepresentable
    {
        CLICKED_FACE(0, "clicked_face", ResourcePopper.FROM_FACE),
        FROM_MIDDLE(1, "from_middle", ResourcePopper.FROM_MIDDLE);

        private final int id;
        private final String name;
        private final ResourcePopper resourcePopper;
        public static final Codec<DropStrategy> CODEC;
        private static final IntFunction<DropStrategy> BY_ID;
        public static final StreamCodec<ByteBuf, DropStrategy> STREAM_CODEC;

        private DropStrategy(int id, String name, ResourcePopper resourcePopper) {
            this.id = id;
            this.name = name;
            this.resourcePopper = resourcePopper;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public void pop(Level level, BlockPos pos, Direction direction, ItemStack stack) {
            this.resourcePopper.pop(level, pos, direction, stack);
        }

        static {
            CODEC = StringRepresentable.fromValues(DropStrategy::values);
            BY_ID = ByIdMap.continuous(DropStrategy::getId, DropStrategy.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DropStrategy::getId);
        }

        @FunctionalInterface
        public static interface ResourcePopper {
            public static final ResourcePopper FROM_FACE = Block::popResourceFromFace;
            public static final ResourcePopper FROM_MIDDLE = (level, pos, direction, stack) -> Block.popResource(level, pos, stack);

            public void pop(Level var1, BlockPos var2, Direction var3, ItemStack var4);
        }
    }
}

