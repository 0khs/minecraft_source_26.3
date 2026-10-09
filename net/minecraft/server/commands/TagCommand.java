/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.world.entity.Entity;

public class TagCommand {
    private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.tag.add.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.tag.remove.failed"));
    private static final CommandResponseTracker.Messages<Entity> RESPONSE_NO_TAGS = CommandResponseTracker.messages((entity, n) -> Component.translatable("commands.tag.list.single.empty", entity.getDisplayName()), (entityCount, n) -> Component.translatable("commands.tag.list.multiple.empty", entityCount));
    private static final CommandResponseTracker.MessagesWithArg<Entity, String> RESPONSE_REMOVE = CommandResponseTracker.messages(ERROR_REMOVE_FAILED, (entity, n, name) -> Component.translatable("commands.tag.remove.success.single", name, entity.getDisplayName()), (entityCount, n, name) -> Component.translatable("commands.tag.remove.success.multiple", name, entityCount));
    private static final CommandResponseTracker.MessagesWithArg<Entity, String> RESPONSE_ADD = CommandResponseTracker.messages(ERROR_ADD_FAILED, (entity, n, name) -> Component.translatable("commands.tag.add.success.single", name, entity.getDisplayName()), (entityCount, n, name) -> Component.translatable("commands.tag.add.success.multiple", name, entityCount));
    private static final CommandResponseTracker.MessagesWithArg<Entity, Set<String>> RESPONSE_LIST = CommandResponseTracker.messages((entity, n, tags) -> Component.translatable("commands.tag.list.single.success", entity.getDisplayName(), tags.size(), ComponentUtils.formatList(tags)), (entityCount, n, tags) -> Component.translatable("commands.tag.list.multiple.success", entityCount, tags.size(), ComponentUtils.formatList(tags)));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tag").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities()).then(Commands.literal("add").then(Commands.argument("name", StringArgumentType.word()).executes(c -> TagCommand.addTag((CommandSourceStack)c.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets"), StringArgumentType.getString((CommandContext)c, (String)"name")))))).then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.word()).suggests((c, p) -> SharedSuggestionProvider.suggest(TagCommand.getTags(EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets")), p)).executes(c -> TagCommand.removeTag((CommandSourceStack)c.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets"), StringArgumentType.getString((CommandContext)c, (String)"name")))))).then(Commands.literal("list").executes(c -> TagCommand.listTags((CommandSourceStack)c.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets"))))));
    }

    private static Collection<String> getTags(Collection<? extends Entity> entities) {
        HashSet<String> result = new HashSet<String>();
        for (Entity entity : entities) {
            result.addAll(entity.entityTags());
        }
        return result;
    }

    private static int addTag(CommandSourceStack source, Collection<? extends Entity> targets, String name) throws CommandSyntaxException {
        CommandResponseTracker response = CommandResponseTracker.create();
        for (Entity entity : targets) {
            response.track(entity, entity.addTag(name));
        }
        return response.sendFeedback(source, true, RESPONSE_ADD, name);
    }

    private static int removeTag(CommandSourceStack source, Collection<? extends Entity> targets, String name) throws CommandSyntaxException {
        CommandResponseTracker response = CommandResponseTracker.create();
        for (Entity entity : targets) {
            response.track(entity, entity.removeTag(name));
        }
        return response.sendFeedback(source, true, RESPONSE_REMOVE, name);
    }

    private static int listTags(CommandSourceStack source, Collection<? extends Entity> targets) throws CommandSyntaxException {
        CommandResponseTracker response = CommandResponseTracker.create();
        HashSet<String> tags = new HashSet<String>();
        for (Entity entity : targets) {
            Set<String> entityTags = entity.entityTags();
            tags.addAll(entityTags);
            response.track(entity, entityTags.size());
        }
        if (tags.isEmpty()) {
            response.sendFeedback(source, false, RESPONSE_NO_TAGS);
        } else {
            response.sendFeedback(source, false, RESPONSE_LIST, tags);
        }
        return tags.size();
    }
}

