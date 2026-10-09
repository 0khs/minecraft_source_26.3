/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands.item;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ArgProvider;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.server.commands.item.ItemAccessor;
import net.minecraft.server.commands.item.ItemCommands;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.slot.SlotCollection;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public record BlockItemAccessor(BlockPos pos) implements ItemAccessor<BlockPos>
{
    public static final ArgProvider.Factory<ItemAccessor<?>> PROVIDER = arg -> ArgProvider.create("block", () -> Commands.argument(arg, BlockPosArgument.blockPos()), c -> new BlockItemAccessor(BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)c, arg)));
    private static final CommandResponseTracker.Messages<BlockPos> RESPONSE_SET = CommandResponseTracker.messages(ItemCommands.ERROR_TARGET_NO_CHANGES, (pos, slotCount) -> Component.translatable("commands.item.block.replace.success", slotCount, pos.getX(), pos.getY(), pos.getZ()), (n, n2) -> Component.empty());
    private static final CommandResponseTracker.MessagesWithArg<BlockPos, ItemStack> RESPONSE_SET_KNOWN_ITEM = CommandResponseTracker.messages(arg_0 -> ((DynamicCommandExceptionType)ItemCommands.ERROR_TARGET_NO_CHANGES_KNOWN_ITEM).create(arg_0), (pos, slotCount, itemStack) -> Component.translatable("commands.item.block.replace.success.known_item", slotCount, pos.getX(), pos.getY(), pos.getZ(), itemStack.getDisplayName()), (n, n2, itemStack) -> Component.empty());
    private static final CommandResponseTracker.Messages<BlockPos> RESPONSE_MODIFY = CommandResponseTracker.messages(ItemCommands.ERROR_TARGET_NO_CHANGES, (pos, slotCount) -> Component.translatable("commands.item.block.modify.success", slotCount, pos.getX(), pos.getY(), pos.getZ()), (n, n2) -> Component.empty());

    @Override
    public SlotCollection getSlots(CommandSourceStack source, SlotSource slotSource) throws CommandSyntaxException {
        return this.getBlockSlots(source, slotSource, ItemCommands.ERROR_SOURCE_NOT_A_CONTAINER);
    }

    private SlotCollection getBlockSlots(CommandSourceStack source, SlotSource slotSource, Dynamic3CommandExceptionType exceptionType) throws CommandSyntaxException {
        Container container = BlockItemAccessor.getContainer(source, this.pos, exceptionType);
        return ItemCommands.getSlotsFromProvider(source, container, slotSource);
    }

    @Override
    public void setItems(CommandSourceStack source, SlotSource slotSource, ItemAccessor.SetterFunction<BlockPos> function) throws CommandSyntaxException {
        SlotCollection targetSlots = this.getBlockSlots(source, slotSource, ItemCommands.ERROR_TARGET_NOT_A_CONTAINER);
        function.apply(this.pos, targetSlots);
    }

    @Override
    public int getReplaceSuccess(CommandSourceStack source, CommandResponseTracker<BlockPos> tracker, @Nullable ItemStack knownItem) throws CommandSyntaxException {
        return knownItem != null ? tracker.sendFeedback(source, true, RESPONSE_SET_KNOWN_ITEM, knownItem) : tracker.sendFeedback(source, true, RESPONSE_SET);
    }

    @Override
    public int getModifySuccess(CommandSourceStack source, CommandResponseTracker<BlockPos> tracker) throws CommandSyntaxException {
        return tracker.sendFeedback(source, true, RESPONSE_MODIFY);
    }

    public static Container getContainer(CommandSourceStack source, BlockPos pos, Dynamic3CommandExceptionType exceptionType) throws CommandSyntaxException {
        BlockEntity entity = source.getLevel().getBlockEntity(pos);
        if (entity instanceof Container) {
            Container container = (Container)((Object)entity);
            return container;
        }
        throw exceptionType.create((Object)pos.getX(), (Object)pos.getY(), (Object)pos.getZ());
    }
}

