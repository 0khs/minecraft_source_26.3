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
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.jspecify.annotations.Nullable;

public record RuleBasedStateProvider(@Nullable Holder<BlockStateProvider> fallback, List<Rule> rules) implements BlockStateProvider
{
    public static final MapCodec<RuleBasedStateProvider> CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)BlockStateProvider.CODEC.optionalFieldOf("fallback").forGetter(provider -> Optional.ofNullable(provider.fallback)), (App)Rule.CODEC.listOf().fieldOf("rules").forGetter(p -> p.rules)).apply((Applicative)i, RuleBasedStateProvider::new));

    private RuleBasedStateProvider(Optional<Holder<BlockStateProvider>> fallback, List<Rule> rules) {
        this((Holder<BlockStateProvider>)fallback.orElse(null), rules);
    }

    public static RuleBasedStateProvider ifTrueThenProvide(BlockPredicate ifTrue, Block thenProvide) {
        return RuleBasedStateProvider.ifTrueThenProvide(ifTrue, BlockStateProvider.of(thenProvide));
    }

    public static RuleBasedStateProvider ifTrueThenProvide(BlockPredicate ifTrue, BlockStateProvider thenProvide) {
        return new RuleBasedStateProvider((Holder<BlockStateProvider>)null, List.of(new Rule(ifTrue, Holder.direct(thenProvide))));
    }

    public MapCodec<RuleBasedStateProvider> codec() {
        return CODEC;
    }

    @Override
    public BlockState getState(LevelAccessor level, RandomSource random, BlockPos pos) {
        BlockState result = this.getOptionalState(level, random, pos);
        return result != null ? result : level.getBlockState(pos);
    }

    @Override
    public @Nullable BlockState getOptionalState(LevelAccessor level, RandomSource random, BlockPos pos) {
        for (Rule rule : this.rules) {
            BlockState optionalState;
            if (!rule.ifTrue().test(level, pos) || (optionalState = rule.then().value().getOptionalState(level, random, pos)) == null) continue;
            return optionalState;
        }
        return this.fallback == null ? null : this.fallback.value().getOptionalState(level, random, pos);
    }

    public static Builder builder() {
        return new Builder(null);
    }

    public static Builder builder(@Nullable BlockStateProvider fallback) {
        return new Builder(fallback);
    }

    public record Rule(BlockPredicate ifTrue, Holder<BlockStateProvider> then) {
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(i -> i.group((App)BlockPredicate.CODEC.fieldOf("if_true").forGetter(Rule::ifTrue), (App)BlockStateProvider.CODEC.fieldOf("then").forGetter(Rule::then)).apply((Applicative)i, Rule::new));
    }

    public static class Builder {
        private final @Nullable BlockStateProvider fallback;
        private final List<Rule> rules = new ArrayList<Rule>();

        public Builder(@Nullable BlockStateProvider fallback) {
            this.fallback = fallback;
        }

        public Builder ifTrueThenProvide(BlockPredicate ifTrue, BlockStateProvider thenProvide) {
            this.rules.add(new Rule(ifTrue, Holder.direct(thenProvide)));
            return this;
        }

        public Builder ifTrueThenProvide(BlockPredicate ifTrue, Block thenProvide) {
            this.rules.add(new Rule(ifTrue, BlockStateProvider.holderOf(thenProvide)));
            return this;
        }

        public Builder ifTrueThenProvide(BlockPredicate ifTrue, BlockState thenProvide) {
            this.rules.add(new Rule(ifTrue, BlockStateProvider.holderOf(thenProvide)));
            return this;
        }

        public RuleBasedStateProvider build() {
            return new RuleBasedStateProvider(this.fallback == null ? null : Holder.direct(this.fallback), this.rules);
        }
    }
}

