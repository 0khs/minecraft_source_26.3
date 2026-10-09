/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.world.entity.Entity;

public class KillCommand {
    private static final CommandResponseTracker.Messages<Entity> RESPONSE_KILL = CommandResponseTracker.messages((entity, n) -> Component.translatable("commands.kill.success.single", entity.getDisplayName()), (entityCount, n) -> Component.translatable("commands.kill.success.multiple", entityCount));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("kill").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes(c -> KillCommand.kill((CommandSourceStack)c.getSource(), (Collection<? extends Entity>)ImmutableList.of((Object)((CommandSourceStack)c.getSource()).getEntityOrException())))).then(Commands.argument("targets", EntityArgument.entities()).executes(c -> KillCommand.kill((CommandSourceStack)c.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets")))));
    }

    private static int kill(CommandSourceStack source, Collection<? extends Entity> victims) throws CommandSyntaxException {
        CommandResponseTracker tracker = CommandResponseTracker.create();
        for (Entity entity : victims) {
            entity.kill(source.getLevel());
            tracker.track(entity);
        }
        return tracker.sendFeedback(source, true, RESPONSE_KILL);
    }
}

