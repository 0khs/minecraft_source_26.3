/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.server.level.ServerPlayer;

public class WardenSpawnTrackerCommand {
    private static final CommandResponseTracker.Messages<ServerPlayer> RESPONSE_SET_LEVEL = CommandResponseTracker.messages((player, n) -> Component.translatable("commands.warden_spawn_tracker.set.success.single", player.getDisplayName()), (playerCount, n) -> Component.translatable("commands.warden_spawn_tracker.set.success.multiple", playerCount));
    private static final CommandResponseTracker.Messages<ServerPlayer> RESPONSE_RESET = CommandResponseTracker.messages((player, n) -> Component.translatable("commands.warden_spawn_tracker.clear.success.single", player.getDisplayName()), (playerCount, n) -> Component.translatable("commands.warden_spawn_tracker.clear.success.multiple", playerCount));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("warden_spawn_tracker").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("clear").executes(c -> WardenSpawnTrackerCommand.resetTracker((CommandSourceStack)c.getSource(), (Collection<ServerPlayer>)ImmutableList.of((Object)((CommandSourceStack)c.getSource()).getPlayerOrException()))))).then(Commands.literal("set").then(Commands.argument("warning_level", IntegerArgumentType.integer((int)0, (int)4)).executes(c -> WardenSpawnTrackerCommand.setWarningLevel((CommandSourceStack)c.getSource(), (Collection<ServerPlayer>)ImmutableList.of((Object)((CommandSourceStack)c.getSource()).getPlayerOrException()), IntegerArgumentType.getInteger((CommandContext)c, (String)"warning_level"))))));
    }

    private static int setWarningLevel(CommandSourceStack source, Collection<ServerPlayer> players, int warningLevel) throws CommandSyntaxException {
        CommandResponseTracker tracker = CommandResponseTracker.create();
        for (ServerPlayer player : players) {
            player.getWardenSpawnTracker().setWarningLevel(warningLevel);
            tracker.track(player);
        }
        return tracker.sendFeedback(source, true, RESPONSE_SET_LEVEL);
    }

    private static int resetTracker(CommandSourceStack source, Collection<ServerPlayer> players) throws CommandSyntaxException {
        CommandResponseTracker tracker = CommandResponseTracker.create();
        for (ServerPlayer player : players) {
            player.getWardenSpawnTracker().reset();
            tracker.track(player);
        }
        return tracker.sendFeedback(source, true, RESPONSE_RESET);
    }
}

