/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.InCommandFunction;

public interface ArgProvider<T> {
    public T access(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

    public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> var1, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> var2);

    public static <T> ArgProvider<T> create(final String key, final Supplier<ArgumentBuilder<CommandSourceStack, ?>> child, final InCommandFunction<CommandContext<CommandSourceStack>, T> access) {
        return new ArgProvider<T>(){

            @Override
            public T access(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
                return access.apply(context);
            }

            @Override
            public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> parent, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function) {
                return parent.then(Commands.literal(key).then(function.apply((ArgumentBuilder)child.get())));
            }
        };
    }

    public static <T> List<ArgProvider<T>> buildList(String argName, List<Factory<T>> factories) {
        ImmutableList.Builder result = ImmutableList.builderWithExpectedSize((int)factories.size());
        for (Factory<T> factory : factories) {
            result.add(factory.create(argName));
        }
        return result.build();
    }

    @FunctionalInterface
    public static interface Factory<T> {
        public ArgProvider<T> create(String var1);
    }
}

