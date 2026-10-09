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
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class GiveCommand {
    private static final Dynamic2CommandExceptionType TOO_MANY_ITEMS = new Dynamic2CommandExceptionType((maxCount, item) -> Component.translatableEscape("commands.give.failed.toomanyitems", maxCount, item));
    private static final CommandResponseTracker.MessagesWithArgs<ServerPlayer, Integer, ItemStack> RESPONSE_GIVE = CommandResponseTracker.messages((player, n, count, prototypeItemStack) -> Component.translatable("commands.give.success.single", count, prototypeItemStack.getDisplayName(), player.getDisplayName()), (playerCount, n, count, prototypeItemStack) -> Component.translatable("commands.give.success.multiple", count, prototypeItemStack.getDisplayName(), playerCount));
    public static final int MAX_ALLOWED_ITEMSTACKS = 100;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("give").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.argument("targets", EntityArgument.players()).then(((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item(context)).executes(c -> GiveCommand.giveItem((CommandSourceStack)c.getSource(), ItemArgument.getItem(c, "item"), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), 1))).then(Commands.argument("count", IntegerArgumentType.integer((int)1)).executes(c -> GiveCommand.giveItem((CommandSourceStack)c.getSource(), ItemArgument.getItem(c, "item"), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), IntegerArgumentType.getInteger((CommandContext)c, (String)"count")))))));
    }

    private static int giveItem(CommandSourceStack source, ItemInput input, Collection<ServerPlayer> players, int count) throws CommandSyntaxException {
        CommandResponseTracker tracker = CommandResponseTracker.create();
        ItemStack prototypeItemStack = input.createItemStack(1);
        int maxStackSize = prototypeItemStack.getMaxStackSize();
        int maxAllowedCount = maxStackSize * 100;
        if (count > maxAllowedCount) {
            throw TOO_MANY_ITEMS.create((Object)maxAllowedCount, (Object)prototypeItemStack.getDisplayName());
        }
        for (ServerPlayer player : players) {
            int remaining = count;
            while (remaining > 0) {
                ItemEntity drop;
                int size = Math.min(maxStackSize, remaining);
                remaining -= size;
                ItemStack copyToDrop = prototypeItemStack.copyWithCount(size);
                boolean added = player.getInventory().add(copyToDrop);
                if (!added || !copyToDrop.isEmpty()) {
                    drop = player.createItemStackToDrop(copyToDrop, false, false);
                    if (drop == null) continue;
                    drop.setNoPickUpDelay();
                    drop.setTarget(player.getUUID());
                    drop.level().addFreshEntity(drop);
                    continue;
                }
                drop = player.createItemStackToDrop(prototypeItemStack.copy(), false, false);
                if (drop != null) {
                    drop.makeFakeItem();
                    drop.level().addFreshEntity(drop);
                }
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                player.containerMenu.broadcastChanges();
            }
            tracker.track(player);
        }
        return tracker.sendFeedback(source, true, RESPONSE_GIVE, count, prototypeItemStack);
    }
}

