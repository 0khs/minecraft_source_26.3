/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jspecify.annotations.Nullable;

public record BlockPredicate(Optional<HolderSet<Block>> blocks, Optional<StatePropertiesPredicate> properties, Optional<NbtPredicate> nbt, DataComponentMatchers components) {
    public static final MapCodec<BlockPredicate> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)RegistryCodecs.holderSet(Registries.BLOCK).optionalFieldOf("blocks").forGetter(BlockPredicate::blocks), (App)StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(BlockPredicate::properties), (App)NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(BlockPredicate::nbt), (App)DataComponentMatchers.CODEC.forGetter(BlockPredicate::components)).apply((Applicative)i, BlockPredicate::new));
    public static final Codec<BlockPredicate> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPredicate> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.optional(ByteBufCodecs.holderSet(Registries.BLOCK)), BlockPredicate::blocks, ByteBufCodecs.optional(StatePropertiesPredicate.STREAM_CODEC), BlockPredicate::properties, ByteBufCodecs.optional(NbtPredicate.STREAM_CODEC), BlockPredicate::nbt, DataComponentMatchers.STREAM_CODEC, BlockPredicate::components, BlockPredicate::new);

    public boolean matches(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity;
        if (!level.isLoaded(pos)) {
            return false;
        }
        if (!this.matchesState(level.getBlockState(pos))) {
            return false;
        }
        return !this.willMatchBlockEntity() || this.matchesBlockEntity(level, blockEntity = level.getBlockEntity(pos));
    }

    public boolean willMatchBlockEntity() {
        return this.nbt.isPresent() || !this.components.isEmpty();
    }

    public boolean matches(BlockInWorld blockInWorld) {
        if (!this.matchesState(blockInWorld.getState())) {
            return false;
        }
        return !this.nbt.isPresent() || BlockPredicate.matchesBlockEntityData(blockInWorld.getLevel(), blockInWorld.getEntity(), this.nbt.get());
    }

    public boolean matchesState(BlockState state) {
        if (this.blocks.isPresent() && !state.is(this.blocks.get())) {
            return false;
        }
        return !this.properties.isPresent() || this.properties.get().matches(state);
    }

    public boolean matchesBlockEntity(LevelReader level, @Nullable BlockEntity blockEntity) {
        if (this.nbt.isPresent() && !BlockPredicate.matchesBlockEntityData(level, blockEntity, this.nbt.get())) {
            return false;
        }
        return this.components.isEmpty() || BlockPredicate.matchesComponents(blockEntity, this.components);
    }

    private static boolean matchesBlockEntityData(LevelReader level, @Nullable BlockEntity entity, NbtPredicate nbt) {
        return entity != null && nbt.matches(entity.saveWithFullMetadata(level.registryAccess()));
    }

    private static boolean matchesComponents(@Nullable BlockEntity entity, DataComponentMatchers components) {
        return entity != null && components.test(entity.collectComponents());
    }

    public boolean requiresNbt() {
        return this.nbt.isPresent();
    }

    public static class Builder {
        private Optional<HolderSet<Block>> blocks = Optional.empty();
        private Optional<StatePropertiesPredicate> properties = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();
        private DataComponentMatchers components = DataComponentMatchers.ANY;

        private Builder() {
        }

        public static Builder block() {
            return new Builder();
        }

        public Builder of(HolderGetter<Block> lookup, Block ... blocks) {
            return this.of(lookup, Arrays.asList(blocks));
        }

        public Builder of(HolderGetter<Block> lookup, Collection<Block> blocks) {
            this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, blocks));
            return this;
        }

        public Builder of(HolderGetter<Block> lookup, TagKey<Block> tag) {
            this.blocks = Optional.of(lookup.getOrThrow(tag));
            return this;
        }

        public Builder hasNbt(CompoundTag nbt) {
            this.nbt = Optional.of(new NbtPredicate(nbt));
            return this;
        }

        public Builder setProperties(StatePropertiesPredicate.Builder properties) {
            this.properties = properties.build();
            return this;
        }

        public Builder components(DataComponentMatchers components) {
            this.components = components;
            return this;
        }

        public BlockPredicate build() {
            return new BlockPredicate(this.blocks, this.properties, this.nbt, this.components);
        }
    }
}

