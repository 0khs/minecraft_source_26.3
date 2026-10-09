/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.SwingAnimationType;

public class SwingAnimationArgument
implements ArgumentType<SwingAnimationType> {
    private static final Collection<String> EXAMPLES = Stream.of(SwingAnimationType.WHACK, SwingAnimationType.STAB).map(SwingAnimationType::getSerializedName).collect(Collectors.toList());
    private static final SwingAnimationType[] VALUES = SwingAnimationType.values();
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(value -> Component.translatableEscape("argument.swing_animation.invalid", value));

    public SwingAnimationType parse(StringReader reader) throws CommandSyntaxException {
        String swingAnimationString = reader.readUnquotedString();
        SwingAnimationType swingAnimation = SwingAnimationType.byName(swingAnimationString, null);
        if (swingAnimation == null) {
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)reader, (Object)swingAnimationString);
        }
        return swingAnimation;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (context.getSource() instanceof SharedSuggestionProvider) {
            return SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(SwingAnimationType::getSerializedName), builder);
        }
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static SwingAnimationArgument swingAnimationType() {
        return new SwingAnimationArgument();
    }

    public static SwingAnimationType getSwingAnimationType(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return (SwingAnimationType)context.getArgument(name, SwingAnimationType.class);
    }
}

