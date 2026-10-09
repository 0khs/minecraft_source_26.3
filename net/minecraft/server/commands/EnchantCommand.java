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
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
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
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(target -> Component.translatableEscape("commands.enchant.failed.entity", target));
    private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType(target -> Component.translatableEscape("commands.enchant.failed.itemless", target));
    private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType(item -> Component.translatableEscape("commands.enchant.failed.incompatible", item));
    private static final Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType((level, max) -> Component.translatableEscape("commands.enchant.failed.level", level, max));
    private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType((Message)Component.translatable("commands.enchant.failed"));
    private static final CommandResponseTracker.MessagesWithArgs<Entity, Holder<Enchantment>, Integer> RESPONSE_ENCHANT = CommandResponseTracker.messages(ERROR_NOTHING_HAPPENED, (entity, n, enchantment, level) -> Component.translatable("commands.enchant.success.single", Enchantment.getFullname(enchantment, level), entity.getDisplayName()), (entityCount, n, enchantment, level) -> Component.translatable("commands.enchant.success.multiple", Enchantment.getFullname(enchantment, level), entityCount));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("enchant").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.argument("targets", EntityArgument.entities()).then(((RequiredArgumentBuilder)Commands.argument("enchantment", ResourceArgument.resource(context, Registries.ENCHANTMENT)).executes(c -> EnchantCommand.enchant((CommandSourceStack)c.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets"), ResourceArgument.getEnchantment((CommandContext<CommandSourceStack>)c, "enchantment"), 1))).then(Commands.argument("level", IntegerArgumentType.integer((int)0)).executes(c -> EnchantCommand.enchant((CommandSourceStack)c.getSource(), EntityArgument.getEntities((CommandContext<CommandSourceStack>)c, "targets"), ResourceArgument.getEnchantment((CommandContext<CommandSourceStack>)c, "enchantment"), IntegerArgumentType.getInteger((CommandContext)c, (String)"level")))))));
    }

    private static int enchant(CommandSourceStack source, Collection<? extends Entity> targets, Holder<Enchantment> enchantmentHolder, int level) throws CommandSyntaxException {
        Enchantment enchantment = enchantmentHolder.value();
        if (level > enchantment.getMaxLevel()) {
            throw ERROR_LEVEL_TOO_HIGH.create((Object)level, (Object)enchantment.getMaxLevel());
        }
        CommandResponseTracker tracker = CommandResponseTracker.create();
        for (Entity entity : targets) {
            if (entity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity)entity;
                ItemStack item = target.getMainHandItem();
                if (!item.isEmpty()) {
                    if (enchantment.canEnchant(item) && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantmentsForCrafting(item).keySet(), enchantmentHolder)) {
                        item.enchant(enchantmentHolder, level);
                        tracker.track(entity);
                        continue;
                    }
                    if (targets.size() != 1) continue;
                    throw ERROR_INCOMPATIBLE.create((Object)item.getHoverName().getString());
                }
                if (targets.size() != 1) continue;
                throw ERROR_NO_ITEM.create((Object)target.getDisplayName().getString());
            }
            if (targets.size() != 1) continue;
            throw ERROR_NOT_LIVING_ENTITY.create((Object)entity.getDisplayName().getString());
        }
        return tracker.sendFeedback(source, true, RESPONSE_ENCHANT, enchantmentHolder, level);
    }
}

