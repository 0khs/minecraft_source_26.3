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
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import org.jspecify.annotations.Nullable;

public record ScoreboardValue(ScoreboardNameProvider target, String score, Holder<ContextIntProvider> fallback) implements ContextIntProvider
{
    public static final MapCodec<ScoreboardValue> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)ScoreboardNameProviders.CODEC.fieldOf("target").forGetter(ScoreboardValue::target), (App)Codec.STRING.fieldOf("score").forGetter(ScoreboardValue::score), (App)ContextIntProviders.CODEC.optionalFieldOf("fallback", ContextIntProviders.exactly(0)).forGetter(ScoreboardValue::fallback)).apply((Applicative)i, ScoreboardValue::new));

    public MapCodec<ScoreboardValue> codec() {
        return MAP_CODEC;
    }

    @Override
    public void validate(ValidationContext context) {
        Validatable.validate(context, "target", this.target);
        Validatable.validateHolder(context, "fallback", this.fallback);
    }

    private @Nullable ReadOnlyScoreInfo getScoreInfo(LootContext context) {
        ScoreHolder scoreHolder = this.target.getScoreHolder(context);
        if (scoreHolder == null) {
            return null;
        }
        ServerScoreboard scoreboard = context.getLevel().getScoreboard();
        Objective objective = scoreboard.getObjective(this.score);
        if (objective == null) {
            return null;
        }
        return scoreboard.getPlayerScoreInfo(scoreHolder, objective);
    }

    @Override
    public int getIntUnsafe(LootContext context) {
        ReadOnlyScoreInfo scoreInfo = this.getScoreInfo(context);
        return scoreInfo != null ? scoreInfo.value() : this.fallback.value().getIntUnsafe(context);
    }
}

