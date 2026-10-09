/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.SlotSourceArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ArgProvider;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.server.commands.item.BlockItemAccessor;
import net.minecraft.server.commands.item.EntityItemAccessor;
import net.minecraft.server.commands.item.ItemAccessor;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.item.ItemProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.slot.CountingModifier;
import net.minecraft.world.item.slot.SlotCollection;
import net.minecraft.world.item.slot.SlotSelector;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;

public class ItemCommands {
    public static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((x, y, z) -> Component.translatableEscape("commands.item.target.not_a_container", x, y, z));
    public static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType((x, y, z) -> Component.translatableEscape("commands.item.source.not_a_container", x, y, z));
    public static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(slot -> Component.translatableEscape("commands.item.target.no_such_slot", slot));
    private static final SimpleCommandExceptionType ERROR_TARGET_INAPPLICABLE_UNNAMED_SLOT = new SimpleCommandExceptionType((Message)Component.translatable("commands.item.target.no_such_slot.unnamed"));
    private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(slot -> Component.translatableEscape("commands.item.source.no_such_slot", slot));
    private static final SimpleCommandExceptionType ERROR_SOURCE_INAPPLICABLE_UNNAMED_SLOT = new SimpleCommandExceptionType((Message)Component.translatable("commands.item.source.no_such_slot.unnamed"));
    public static final SimpleCommandExceptionType ERROR_TARGET_NO_CHANGES = new SimpleCommandExceptionType((Message)Component.translatable("commands.item.target.failed"));
    public static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new DynamicCommandExceptionType(item -> Component.translatableEscape("commands.item.target.failed.known_item", item));
    public static final List<ArgProvider.Factory<ItemAccessor<?>>> ALL_PROVIDERS = List.of(EntityItemAccessor.PROVIDER, BlockItemAccessor.PROVIDER);
    public static final List<ArgProvider<ItemAccessor<?>>> TARGET_PROVIDERS = ArgProvider.buildList("target", ALL_PROVIDERS);
    public static final List<ArgProvider<ItemAccessor<?>>> SOURCE_PROVIDERS = ArgProvider.buildList("source", ALL_PROVIDERS);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder root = (LiteralArgumentBuilder)Commands.literal("item").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (ArgProvider<ItemAccessor<?>> targetProvider : TARGET_PROVIDERS) {
            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)root.then(ItemCommands.wrapSetItems(context, Commands.literal("replace"), targetProvider, ItemCommands::replaceItemProvider))).then(ItemCommands.wrapSetItems(context, Commands.literal("fill"), targetProvider, ItemCommands::fillItemProvider))).then(ItemCommands.wrapSetItems(context, Commands.literal("override"), targetProvider, ItemCommands::overrideItemProvider))).then(targetProvider.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("modify"), p -> p.then(Commands.argument("slots", SlotSourceArgument.slotSource(context)).then(Commands.argument("modifier", ResourceOrIdArgument.lootModifier(context)).executes(c -> ItemCommands.modifyItems((CommandSourceStack)c.getSource(), (ItemAccessor)targetProvider.access((CommandContext<CommandSourceStack>)c), SlotSourceArgument.getSlotSource((CommandContext<CommandSourceStack>)c, "slots"), ResourceOrIdArgument.getLootModifier((CommandContext<CommandSourceStack>)c, "modifier")))))));
        }
        dispatcher.register(root);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapSetItems(CommandBuildContext context, ArgumentBuilder<CommandSourceStack, ?> builder, ArgProvider<ItemAccessor<?>> targetProvider, ItemDistributor distributor) {
        return targetProvider.wrap(builder, t -> {
            RequiredArgumentBuilder<CommandSourceStack, SlotSourceArgument.Result> slotsNode = Commands.argument("slots", SlotSourceArgument.slotSource(context));
            for (ArgProvider<ItemAccessor<?>> sourceProvider : SOURCE_PROVIDERS) {
                slotsNode.then(sourceProvider.wrap((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("from"), p -> p.then(((RequiredArgumentBuilder)Commands.argument("sourceSlots", SlotSourceArgument.slotSource(context)).executes(c -> ItemCommands.setItems((CommandSourceStack)c.getSource(), (ItemAccessor)targetProvider.access((CommandContext<CommandSourceStack>)c), SlotSourceArgument.getSlotSource((CommandContext<CommandSourceStack>)c, "slots"), distributor, ItemCommands.getItems((CommandSourceStack)c.getSource(), (ItemAccessor)sourceProvider.access((CommandContext<CommandSourceStack>)c), SlotSourceArgument.getSlotSource((CommandContext<CommandSourceStack>)c, "sourceSlots"), null)))).then(Commands.argument("modifier", ResourceOrIdArgument.lootModifier(context)).executes(c -> ItemCommands.setItems((CommandSourceStack)c.getSource(), (ItemAccessor)targetProvider.access((CommandContext<CommandSourceStack>)c), SlotSourceArgument.getSlotSource((CommandContext<CommandSourceStack>)c, "slots"), distributor, ItemCommands.getItems((CommandSourceStack)c.getSource(), (ItemAccessor)sourceProvider.access((CommandContext<CommandSourceStack>)c), SlotSourceArgument.getSlotSource((CommandContext<CommandSourceStack>)c, "sourceSlots"), ResourceOrIdArgument.getLootModifier((CommandContext<CommandSourceStack>)c, "modifier"))))))));
            }
            slotsNode.then(Commands.literal("with").then(((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item(context)).executes(c -> ItemCommands.setItem((CommandSourceStack)c.getSource(), (ItemAccessor)targetProvider.access((CommandContext<CommandSourceStack>)c), SlotSourceArgument.getSlotSource((CommandContext<CommandSourceStack>)c, "slots"), distributor, ItemArgument.getItem(c, "item").createItemStack(1)))).then(Commands.argument("count", IntegerArgumentType.integer((int)1, (int)99)).executes(c -> ItemCommands.setItem((CommandSourceStack)c.getSource(), (ItemAccessor)targetProvider.access((CommandContext<CommandSourceStack>)c), SlotSourceArgument.getSlotSource((CommandContext<CommandSourceStack>)c, "slots"), distributor, ItemArgument.getItem(c, "item").createItemStack(IntegerArgumentType.getInteger((CommandContext)c, (String)"count")))))));
            return t.then(slotsNode);
        });
    }

    public static ItemProvider replaceItemProvider(Collection<ItemStack> items) {
        return ItemProvider.of(items);
    }

    public static ItemProvider fillItemProvider(Collection<ItemStack> items) {
        return ItemProvider.cycle(items);
    }

    public static ItemProvider overrideItemProvider(Collection<ItemStack> items) {
        return ItemProvider.of(items).orElseProvide(ItemStack.EMPTY);
    }

    private static <Target> int setItems(CommandSourceStack source, ItemAccessor<Target> accessor, SlotSourceArgument.Result slotSource, ItemProvider items, @Nullable ItemStack knownItem) throws CommandSyntaxException {
        MutableInt selectedCount = new MutableInt();
        SlotSelector slotSelector = SlotSelector.tracking(selectedCount);
        CommandResponseTracker tracker = CommandResponseTracker.create();
        accessor.setItems(source, slotSource.value(), (target, slots) -> {
            items.restart();
            int count = slots.replaceSlotItems(items, slotSelector);
            tracker.track(target, count);
            return count;
        });
        if (selectedCount.intValue() == 0) {
            throw slotSource.name().map(arg_0 -> ((DynamicCommandExceptionType)ERROR_TARGET_INAPPLICABLE_SLOT).create(arg_0)).orElseGet(() -> ((SimpleCommandExceptionType)ERROR_TARGET_INAPPLICABLE_UNNAMED_SLOT).create());
        }
        return accessor.getReplaceSuccess(source, tracker, knownItem);
    }

    private static int setItems(CommandSourceStack source, ItemAccessor<?> accessor, SlotSourceArgument.Result slotSource, ItemDistributor distributor, Collection<ItemStack> items) throws CommandSyntaxException {
        return ItemCommands.setItems(source, accessor, slotSource, distributor.apply(items), null);
    }

    private static int setItem(CommandSourceStack source, ItemAccessor<?> accessor, SlotSourceArgument.Result slotSource, ItemDistributor distributor, ItemStack item) throws CommandSyntaxException {
        return ItemCommands.setItems(source, accessor, slotSource, distributor.apply(List.of(item)), item);
    }

    private static <Target> int modifyItems(CommandSourceStack source, ItemAccessor<Target> accessor, SlotSourceArgument.Result slotSource, Holder<LootItemFunction> modifier) throws CommandSyntaxException {
        MutableInt selectedCount = new MutableInt();
        SlotSelector slotSelector = SlotSelector.tracking(SlotSelector.NON_EMPTY_SLOTS, selectedCount);
        CountingModifier countingModifier = new CountingModifier(ItemCommands.applyModifier(source, modifier.value()));
        CommandResponseTracker tracker = CommandResponseTracker.create();
        accessor.setItems(source, slotSource.value(), (target, slots) -> {
            int lastUpdated = countingModifier.updatedCount();
            slots.modifySlots(countingModifier, slotSelector);
            int count = countingModifier.updatedCount() - lastUpdated;
            tracker.track(target, count);
            return count;
        });
        if (selectedCount.intValue() == 0) {
            throw slotSource.name().map(arg_0 -> ((DynamicCommandExceptionType)ERROR_TARGET_INAPPLICABLE_SLOT).create(arg_0)).orElseGet(() -> ((SimpleCommandExceptionType)ERROR_TARGET_INAPPLICABLE_UNNAMED_SLOT).create());
        }
        return accessor.getModifySuccess(source, tracker);
    }

    private static Collection<ItemStack> getItems(CommandSourceStack source, ItemAccessor<?> accessor, SlotSourceArgument.Result slotSource, @Nullable Holder<LootItemFunction> modifier) throws CommandSyntaxException {
        List<ItemStack> items;
        SlotCollection slots = accessor.getSlots(source, slotSource.value());
        List<ItemStack> list = items = modifier != null ? slots.itemCopies().map(ItemCommands.applyModifier(source, modifier.value())).toList() : slots.itemCopies().toList();
        if (items.isEmpty()) {
            throw slotSource.name().map(arg_0 -> ((DynamicCommandExceptionType)ERROR_SOURCE_INAPPLICABLE_SLOT).create(arg_0)).orElseGet(() -> ((SimpleCommandExceptionType)ERROR_SOURCE_INAPPLICABLE_UNNAMED_SLOT).create());
        }
        return items;
    }

    private static UnaryOperator<ItemStack> applyModifier(CommandSourceStack source, LootItemFunction modifier) {
        LootParams lootParams = new LootParams.Builder(source.getLevel()).withParameter(LootContextParams.ORIGIN, source.getPosition()).withOptionalParameter(LootContextParams.THIS_ENTITY, source.getEntity()).create(LootContextParamSets.COMMAND);
        LootContext context = new LootContext.Builder(lootParams).create(Optional.empty());
        context.pushVisitedElement(LootContext.createVisitedEntry(modifier));
        return item -> {
            ItemStack newItem = (ItemStack)modifier.apply(item, context);
            newItem.limitSize(newItem.getMaxStackSize());
            return newItem;
        };
    }

    static SlotCollection getSlotsFromProvider(CommandSourceStack source, SlotProvider container, SlotSource slotSource) {
        LootParams lootParams = new LootParams.Builder(source.getLevel()).withParameter(LootContextParams.ORIGIN, source.getPosition()).withParameter(LootContextParams.CONTAINER, container).withOptionalParameter(LootContextParams.THIS_ENTITY, source.getEntity()).create(LootContextParamSets.COMMAND_SLOT_SOURCE);
        LootContext context = new LootContext.Builder(lootParams).create(Optional.empty());
        context.pushVisitedElement(LootContext.createVisitedEntry(slotSource));
        return slotSource.provide(context);
    }

    @FunctionalInterface
    private static interface ItemDistributor {
        public ItemProvider apply(Collection<ItemStack> var1);
    }
}

