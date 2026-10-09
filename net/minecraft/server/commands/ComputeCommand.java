/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.LootContextSources;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

public class ComputeCommand {
    private static final Dynamic2CommandExceptionType INVALID_NAMED_VALUE = new Dynamic2CommandExceptionType((provider, value) -> Component.translatableEscape("command.compute.result.named.invalid", provider, value));
    private static final DynamicCommandExceptionType INVALID_UNNAMED_VALUE = new DynamicCommandExceptionType(value -> Component.translatableEscape("command.compute.result.unnamed.invalid", value));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(LootContextSources.addContextSources((LiteralArgumentBuilder)Commands.literal("compute").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)), (contextDecorator, output) -> {
            output.accept(Commands.literal("float").then(((RequiredArgumentBuilder)Commands.argument("provider", ResourceOrIdArgument.floatProvider(context)).executes(c -> ComputeCommand.computeAsFloat((CommandContext<CommandSourceStack>)c, contextDecorator, ResourceOrIdArgument.getFloatProvider((CommandContext<CommandSourceStack>)c, "provider"), 1.0f))).then(Commands.argument("scale", FloatArgumentType.floatArg()).executes(c -> ComputeCommand.computeAsFloat((CommandContext<CommandSourceStack>)c, contextDecorator, ResourceOrIdArgument.getFloatProvider((CommandContext<CommandSourceStack>)c, "provider"), FloatArgumentType.getFloat((CommandContext)c, (String)"scale"))))));
            output.accept(Commands.literal("integer").then(Commands.argument("provider", ResourceOrIdArgument.intProvider(context)).executes(c -> ComputeCommand.computeAsInt((CommandContext<CommandSourceStack>)c, contextDecorator, ResourceOrIdArgument.getIntProvider((CommandContext<CommandSourceStack>)c, "provider")))));
        }));
    }

    private static void printExactOutput(CommandSourceStack source, Holder<?> provider, int result) {
        source.sendSuccess(() -> provider.unwrapKey().map(key -> Component.translatable("command.compute.result.named.exact", Component.translationArg(key.identifier()), result)).orElseGet(() -> Component.translatable("command.compute.result.unnamed.exact", result)), false);
    }

    private static void printRoundedOutput(CommandSourceStack source, Holder<?> provider, int result, float original) {
        source.sendSuccess(() -> provider.unwrapKey().map(key -> Component.translatable("command.compute.result.named.rounded", Component.translationArg(key.identifier()), Float.valueOf(original), result)).orElseGet(() -> Component.translatable("command.compute.result.unnamed.rounded", Float.valueOf(original), result)), false);
    }

    private static int computeAsInt(CommandContext<CommandSourceStack> context, LootContextSources.ContextDecorator decorator, Holder<ContextIntProvider> provider) throws CommandSyntaxException {
        LootContext lootContext = decorator.createContext(context);
        try {
            int result = provider.value().getIntUnsafe(lootContext);
            CommandSourceStack source = (CommandSourceStack)context.getSource();
            ComputeCommand.printExactOutput(source, provider, result);
            return result;
        }
        catch (ArithmeticException e) {
            throw ComputeCommand.throwInvalidValue(provider, e.getMessage());
        }
    }

    private static int computeAsFloat(CommandContext<CommandSourceStack> context, LootContextSources.ContextDecorator decorator, Holder<ContextFloatProvider> provider, float scale) throws CommandSyntaxException {
        float original;
        LootContext lootContext = decorator.createContext(context);
        try {
            original = provider.value().getFloatUnsafe(lootContext);
        }
        catch (ArithmeticException e) {
            throw ComputeCommand.throwInvalidValue(provider, e.getMessage());
        }
        if (!Float.isFinite(original)) {
            String invalidValue = Float.toString(original);
            throw ComputeCommand.throwInvalidValue(provider, invalidValue);
        }
        int result = Mth.floor(original * scale);
        CommandSourceStack source = (CommandSourceStack)context.getSource();
        if ((float)result == original) {
            ComputeCommand.printExactOutput(source, provider, result);
        } else {
            ComputeCommand.printRoundedOutput(source, provider, result, original);
        }
        return result;
    }

    private static CommandSyntaxException throwInvalidValue(Holder<?> provider, String invalidValue) {
        return provider.unwrapKey().map(key -> INVALID_NAMED_VALUE.create((Object)Component.translationArg(key.identifier()), (Object)invalidValue)).orElseGet(() -> INVALID_UNNAMED_VALUE.create((Object)invalidValue));
    }
}

