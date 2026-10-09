/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.SlotsArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.item.slot.RangeSlotSource;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.slot.SlotSources;

public class SlotSourceArgument
implements ArgumentType<Result> {
    private static final Collection<String> EXAMPLES = Util.join(ResourceOrIdArgument.EXAMPLES, SlotsArgument.EXAMPLES);
    private final ArgumentType<Holder<SlotSource>> holderArgument;

    private SlotSourceArgument(CommandBuildContext context) {
        this.holderArgument = new ResourceOrIdArgument<SlotSource>(context, Registries.SLOT_SOURCE, SlotSources.DIRECT_CODEC);
    }

    public static SlotSourceArgument slotSource(CommandBuildContext context) {
        return new SlotSourceArgument(context);
    }

    public static Result getSlotSource(CommandContext<CommandSourceStack> context, String name) {
        return (Result)context.getArgument(name, Result.class);
    }

    public Result parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        SlotRange slotRange = SlotRanges.tryRead(reader);
        if (slotRange != null) {
            return new LiteralResult(slotRange);
        }
        reader.setCursor(start);
        return new HolderResult((Holder)this.holderArgument.parse(reader));
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> contextBuilder, SuggestionsBuilder builder) {
        SuggestionsBuilder sub = builder.restart();
        SharedSuggestionProvider.suggest(SlotRanges.allNames(), sub);
        builder.add(sub);
        return this.holderArgument.listSuggestions(contextBuilder, builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static sealed interface Result
    permits HolderResult, LiteralResult {
        public SlotSource value();

        public Optional<String> name();
    }

    public record LiteralResult(SlotRange slotRange) implements Result
    {
        @Override
        public SlotSource value() {
            return RangeSlotSource.slotRange(this.slotRange);
        }

        @Override
        public Optional<String> name() {
            return Optional.of(this.slotRange.getSerializedName());
        }
    }

    public record HolderResult(Holder<SlotSource> holder) implements Result
    {
        @Override
        public SlotSource value() {
            return this.holder.value();
        }

        @Override
        public Optional<String> name() {
            return this.holder.getRegisteredNameIfPresent();
        }
    }
}

