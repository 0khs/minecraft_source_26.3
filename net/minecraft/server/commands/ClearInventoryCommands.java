/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ClearInventoryCommands {
    private static final DynamicCommandExceptionType ERROR_SINGLE = new DynamicCommandExceptionType(name -> Component.translatableEscape("clear.failed.single", name));
    private static final DynamicCommandExceptionType ERROR_MULTIPLE = new DynamicCommandExceptionType(count -> Component.translatableEscape("clear.failed.multiple", count));
    private static final CommandResponseTracker.Messages<ServerPlayer> RESPONSE_TEST = CommandResponseTracker.messages((player, totalValue) -> Component.translatable("commands.clear.test.single", totalValue, player.getDisplayName()), (playerCount, totalValue) -> Component.translatable("commands.clear.test.multiple", totalValue, playerCount));
    private static final CommandResponseTracker.Messages<ServerPlayer> RESPONSE_CLEAR = CommandResponseTracker.messages((player, totalValue) -> Component.translatable("commands.clear.success.single", totalValue, player.getDisplayName()), (playerCount, totalValue) -> Component.translatable("commands.clear.success.multiple", totalValue, playerCount));
    private static final CommandResponseTracker.Dispatch<CommandSyntaxException, ServerPlayer> ERROR_DISPATCH = new CommandResponseTracker.Dispatch<CommandSyntaxException, ServerPlayer>((player, n) -> ERROR_SINGLE.create((Object)player.getDisplayName()), (playerCount, n) -> ERROR_MULTIPLE.create((Object)playerCount));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clear").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes(c -> ClearInventoryCommands.clearUnlimited((CommandSourceStack)c.getSource(), Collections.singleton(((CommandSourceStack)c.getSource()).getPlayerOrException()), itemStack -> true))).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes(c -> ClearInventoryCommands.clearUnlimited((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), itemStack -> true))).then(((RequiredArgumentBuilder)Commands.argument("item", ItemPredicateArgument.itemPredicate(context)).executes(c -> ClearInventoryCommands.clearUnlimited((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)c, "item")))).then(Commands.argument("maxCount", IntegerArgumentType.integer((int)0)).executes(c -> ClearInventoryCommands.clearInventory((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), ItemPredicateArgument.getItemPredicate((CommandContext<CommandSourceStack>)c, "item"), IntegerArgumentType.getInteger((CommandContext)c, (String)"maxCount")))))));
    }

    private static int clearUnlimited(CommandSourceStack source, Collection<ServerPlayer> players, Predicate<ItemStack> predicate) throws CommandSyntaxException {
        return ClearInventoryCommands.clearInventory(source, players, predicate, -1);
    }

    private static int clearInventory(CommandSourceStack source, Collection<ServerPlayer> players, Predicate<ItemStack> predicate, int maxCount) throws CommandSyntaxException {
        CommandResponseTracker tracker = CommandResponseTracker.create();
        boolean countingOnly = maxCount == 0;
        for (ServerPlayer player : players) {
            tracker.track(player, player.getInventory().clearOrCountMatchingItems(predicate, countingOnly, maxCount, player.inventoryMenu.getCraftSlots()));
            if (countingOnly) continue;
            player.containerMenu.broadcastChanges();
            player.inventoryMenu.slotsChanged(player.getInventory());
        }
        if (tracker.totalValue() == 0) {
            throw tracker.dispatch(CommandResponseTracker.ElementType.ANY, ERROR_DISPATCH);
        }
        return tracker.sendFeedback(source, true, CommandResponseTracker.ElementType.NON_ZERO, maxCount == 0 ? RESPONSE_TEST : RESPONSE_CLEAR);
    }
}

