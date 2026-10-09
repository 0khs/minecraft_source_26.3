/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.server.level.ServerPlayer;

public class PostEffectCommand {
    private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.posteffect.add.failed"));
    private static final SimpleCommandExceptionType ERROR_CLEAR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.posteffect.clear.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.posteffect.remove.failed"));
    private static final CommandResponseTracker.MessagesWithArg<ServerPlayer, Identifier> RESPONSE_ADD = CommandResponseTracker.messages(ERROR_ADD_FAILED, (entity, n, posteffect) -> Component.translatable("commands.posteffect.add.success.single", Component.translationArg(posteffect), entity.getDisplayName()), (entityCount, n, posteffect) -> Component.translatable("commands.posteffect.add.success.multiple", Component.translationArg(posteffect), entityCount));
    private static final CommandResponseTracker.Messages<ServerPlayer> RESPONSE_CLEAR = CommandResponseTracker.messages(ERROR_CLEAR_FAILED, (entity, n) -> Component.translatable("commands.posteffect.clear.success.single", entity.getDisplayName()), (entityCount, n) -> Component.translatable("commands.posteffect.clear.success.multiple", entityCount));
    private static final CommandResponseTracker.MessagesWithArg<ServerPlayer, Identifier> RESPONSE_REMOVE = CommandResponseTracker.messages(ERROR_REMOVE_FAILED, (entity, n, posteffect) -> Component.translatable("commands.posteffect.remove.success.single", Component.translationArg(posteffect), entity.getDisplayName()), (entityCount, n, posteffect) -> Component.translatable("commands.posteffect.remove.success.multiple", Component.translationArg(posteffect), entityCount));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("posteffect").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("add").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("posteffect", IdentifierArgument.id()).suggests(SuggestionProviders.cast(SuggestionProviders.POST_EFFECTS)).executes(c -> PostEffectCommand.addPostEffect((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)c, "posteffect"))))))).then(Commands.literal("clear").then(Commands.argument("targets", EntityArgument.players()).executes(c -> PostEffectCommand.clearPostEffect((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets")))))).then(Commands.literal("list").then(Commands.argument("target", EntityArgument.player()).executes(c -> PostEffectCommand.listPostEffects((CommandSourceStack)c.getSource(), EntityArgument.getPlayer((CommandContext<CommandSourceStack>)c, "target")))))).then(Commands.literal("remove").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("posteffect", IdentifierArgument.id()).suggests(SuggestionProviders.cast(SuggestionProviders.POST_EFFECTS)).executes(c -> PostEffectCommand.removePostEffect((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), IdentifierArgument.getId((CommandContext<CommandSourceStack>)c, "posteffect")))))));
    }

    private static int addPostEffect(CommandSourceStack source, Collection<ServerPlayer> players, Identifier posteffect) throws CommandSyntaxException {
        CommandResponseTracker tracker = CommandResponseTracker.create();
        for (ServerPlayer player : players) {
            tracker.track(player, player.addPostEffect(posteffect));
        }
        return tracker.sendFeedback(source, true, RESPONSE_ADD, posteffect);
    }

    private static int clearPostEffect(CommandSourceStack source, Collection<ServerPlayer> players) throws CommandSyntaxException {
        CommandResponseTracker tracker = CommandResponseTracker.create();
        for (ServerPlayer player : players) {
            tracker.track(player, player.clearPostEffects());
        }
        return tracker.sendFeedback(source, true, RESPONSE_CLEAR);
    }

    private static int listPostEffects(CommandSourceStack source, ServerPlayer player) {
        List<Identifier> postEffects = player.getPostEffects();
        if (postEffects.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.posteffect.list.empty", player.getDisplayName()), false);
        } else {
            String names = postEffects.stream().map(Identifier::toString).collect(Collectors.joining(", "));
            source.sendSuccess(() -> Component.translatable("commands.posteffect.list.success", player.getDisplayName(), postEffects.size(), names), false);
        }
        return postEffects.size();
    }

    private static int removePostEffect(CommandSourceStack source, Collection<ServerPlayer> players, Identifier posteffect) throws CommandSyntaxException {
        CommandResponseTracker tracker = CommandResponseTracker.create();
        for (ServerPlayer player : players) {
            tracker.track(player, player.removePostEffect(posteffect));
        }
        return tracker.sendFeedback(source, true, RESPONSE_REMOVE, posteffect);
    }
}

