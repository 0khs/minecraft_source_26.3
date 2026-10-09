/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.SwingAnimationArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.SwingAnimation;

public class SwingCommand {
    private static final SimpleCommandExceptionType ERROR_NO_LIVING_ENTITY = new SimpleCommandExceptionType((Message)Component.translatable("commands.swing.failed.notliving"));
    private static final CommandResponseTracker.Messages<LivingEntity> RESPONSE_SWING = CommandResponseTracker.messages(ERROR_NO_LIVING_ENTITY, (entity, n) -> Component.translatable("commands.swing.success.single", entity.getDisplayName()), (entityCount, n) -> Component.translatable("commands.swing.success.multiple", entityCount));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("swing").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).executes(c -> SwingCommand.swing((CommandSourceStack)c.getSource(), List.of(((CommandSourceStack)c.getSource()).getEntityOrException()), InteractionHand.MAIN_HAND))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities()).executes(c -> SwingCommand.swing((CommandSourceStack)c.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets"), InteractionHand.MAIN_HAND))).then(SwingCommand.handSwing("mainhand", InteractionHand.MAIN_HAND))).then(SwingCommand.handSwing("offhand", InteractionHand.OFF_HAND))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> handSwing(String name, InteractionHand hand) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(name).executes(c -> SwingCommand.swing((CommandSourceStack)c.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets"), hand))).then(((RequiredArgumentBuilder)Commands.argument("animation", SwingAnimationArgument.swingAnimationType()).executes(c -> SwingCommand.swing((CommandSourceStack)c.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets"), hand, SwingAnimationArgument.getSwingAnimationType((CommandContext<CommandSourceStack>)c, "animation"), SwingAnimation.DEFAULT.duration()))).then(Commands.argument("duration", TimeArgument.time(1)).executes(c -> SwingCommand.swing((CommandSourceStack)c.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets"), hand, SwingAnimationArgument.getSwingAnimationType((CommandContext<CommandSourceStack>)c, "animation"), IntegerArgumentType.getInteger((CommandContext)c, (String)"duration")))));
    }

    private static int swing(CommandSourceStack source, Collection<? extends Entity> targets, InteractionHand hand) throws CommandSyntaxException {
        return SwingCommand.swing(source, targets, hand, SwingAnimation.DEFAULT.type(), SwingAnimation.DEFAULT.duration());
    }

    private static int swing(CommandSourceStack source, Collection<? extends Entity> targets, InteractionHand hand, SwingAnimationType animationType, int duration) throws CommandSyntaxException {
        CommandResponseTracker tracker = CommandResponseTracker.create();
        SwingAnimation animation = new SwingAnimation(animationType, duration);
        for (Entity entity : targets) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity livingEntity = (LivingEntity)entity;
            livingEntity.swing(hand, animation, true);
            tracker.track(livingEntity);
        }
        return tracker.sendFeedback(source, true, RESPONSE_SWING);
    }
}

