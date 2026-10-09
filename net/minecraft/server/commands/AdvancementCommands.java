/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 */
package net.minecraft.server.commands;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCommands {
    private static final Dynamic2CommandExceptionType ERROR_CRITERION_NOT_FOUND = new Dynamic2CommandExceptionType((name, criterion) -> Component.translatableEscape("commands.advancement.criterionNotFound", name, criterion));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("advancement").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("grant").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.literal("only").then(((RequiredArgumentBuilder)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.GRANT, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)c, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement"), Mode.ONLY)))).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests((c, p) -> SharedSuggestionProvider.suggest(ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement").value().criteria().keySet(), p)).executes(c -> AdvancementCommands.performCriterion((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.GRANT, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement"), StringArgumentType.getString((CommandContext)c, (String)"criterion"))))))).then(Commands.literal("from").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.GRANT, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)c, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement"), Mode.FROM)))))).then(Commands.literal("until").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.GRANT, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)c, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement"), Mode.UNTIL)))))).then(Commands.literal("through").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.GRANT, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)c, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement"), Mode.THROUGH)))))).then(Commands.literal("everything").executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.GRANT, ((CommandSourceStack)c.getSource()).getServer().getAdvancements().getAllAdvancements(), false)))))).then(Commands.literal("revoke").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.literal("only").then(((RequiredArgumentBuilder)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)c, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement"), Mode.ONLY)))).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests((c, p) -> SharedSuggestionProvider.suggest(ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement").value().criteria().keySet(), p)).executes(c -> AdvancementCommands.performCriterion((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.REVOKE, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement"), StringArgumentType.getString((CommandContext)c, (String)"criterion"))))))).then(Commands.literal("from").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)c, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement"), Mode.FROM)))))).then(Commands.literal("until").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)c, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement"), Mode.UNTIL)))))).then(Commands.literal("through").then(Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements((CommandContext<CommandSourceStack>)c, ResourceKeyArgument.getAdvancement((CommandContext<CommandSourceStack>)c, "advancement"), Mode.THROUGH)))))).then(Commands.literal("everything").executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers((CommandContext<CommandSourceStack>)c, "targets"), Action.REVOKE, ((CommandSourceStack)c.getSource()).getServer().getAdvancements().getAllAdvancements()))))));
    }

    private static int perform(CommandSourceStack source, Collection<ServerPlayer> players, Action action, Collection<AdvancementHolder> advancements) throws CommandSyntaxException {
        return AdvancementCommands.perform(source, players, action, advancements, true);
    }

    private static int perform(CommandSourceStack source, Collection<ServerPlayer> players, Action action, Collection<AdvancementHolder> advancements, boolean showAdvancements) throws CommandSyntaxException {
        CommandResponseTracker tracker = CommandResponseTracker.create();
        for (ServerPlayer player : players) {
            tracker.track(player, action.perform(player, advancements, showAdvancements));
        }
        int advancementCount = advancements.size();
        if (advancementCount == 1) {
            AdvancementHolder advancementName = (AdvancementHolder)Iterables.getOnlyElement(advancements);
            if (tracker.totalValue() == 0) {
                throw tracker.dispatch(CommandResponseTracker.ElementType.ANY, action.singleAdvancementsError, advancementName);
            }
            return tracker.sendFeedback(source, true, CommandResponseTracker.ElementType.NON_ZERO, action.singleAdvancementSuccessResponse, advancementName);
        }
        if (tracker.totalValue() == 0) {
            throw tracker.dispatch(CommandResponseTracker.ElementType.ANY, action.multipleAdvancementsError, advancementCount);
        }
        return tracker.sendFeedback(source, true, CommandResponseTracker.ElementType.NON_ZERO, action.multipleAdvancementsSuccessResponse, advancementCount);
    }

    private static int performCriterion(CommandSourceStack source, Collection<ServerPlayer> players, Action action, AdvancementHolder holder, String criterion) throws CommandSyntaxException {
        Advancement advancement = holder.value();
        if (!advancement.criteria().containsKey(criterion)) {
            throw ERROR_CRITERION_NOT_FOUND.create((Object)Advancement.name(holder), (Object)criterion);
        }
        CommandResponseTracker tracker = CommandResponseTracker.create();
        for (ServerPlayer player : players) {
            tracker.track(player, action.performCriterion(player, holder, criterion));
        }
        if (tracker.totalValue() == 0) {
            throw tracker.dispatch(CommandResponseTracker.ElementType.ANY, action.criterionError, holder, criterion);
        }
        return tracker.sendFeedback(source, true, CommandResponseTracker.ElementType.NON_ZERO, action.criterionSuccessResponse, holder, criterion);
    }

    private static List<AdvancementHolder> getAdvancements(CommandContext<CommandSourceStack> context, AdvancementHolder target, Mode mode) {
        AdvancementTree advancementTree = ((CommandSourceStack)context.getSource()).getServer().getAdvancements().tree();
        AdvancementNode targetNode = advancementTree.get(target);
        if (targetNode == null) {
            return List.of(target);
        }
        ArrayList<AdvancementHolder> advancements = new ArrayList<AdvancementHolder>();
        if (mode.parents) {
            for (AdvancementNode parent = targetNode.parent(); parent != null; parent = parent.parent()) {
                advancements.add(parent.holder());
            }
        }
        advancements.add(target);
        if (mode.children) {
            AdvancementCommands.addChildren(targetNode, advancements);
        }
        return advancements;
    }

    private static void addChildren(AdvancementNode parent, List<AdvancementHolder> output) {
        for (AdvancementNode child : parent.children()) {
            output.add(child.holder());
            AdvancementCommands.addChildren(child, output);
        }
    }

    private static enum Action {
        GRANT("grant"){

            @Override
            protected boolean perform(ServerPlayer player, AdvancementHolder advancement) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (progress.isDone()) {
                    return false;
                }
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
                return true;
            }

            @Override
            protected boolean performCriterion(ServerPlayer player, AdvancementHolder advancement, String criterion) {
                return player.getAdvancements().award(advancement, criterion);
            }
        }
        ,
        REVOKE("revoke"){

            @Override
            protected boolean perform(ServerPlayer player, AdvancementHolder advancement) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (!progress.hasProgress()) {
                    return false;
                }
                for (String criterion : progress.getCompletedCriteria()) {
                    player.getAdvancements().revoke(advancement, criterion);
                }
                return true;
            }

            @Override
            protected boolean performCriterion(ServerPlayer player, AdvancementHolder advancement, String criterion) {
                return player.getAdvancements().revoke(advancement, criterion);
            }
        };

        public final CommandResponseTracker.MessagesWithArg<ServerPlayer, AdvancementHolder> singleAdvancementSuccessResponse = CommandResponseTracker.messages((player, n, advancement) -> Component.translatable("commands.advancement." + key + ".one.to.one.success", Advancement.name(advancement), player.getDisplayName()), (playerCount, n, advancement) -> Component.translatable("commands.advancement." + key + ".one.to.many.success", Advancement.name(advancement), playerCount));
        public final CommandResponseTracker.DispatchWithArg<CommandSyntaxException, ServerPlayer, AdvancementHolder> singleAdvancementsError;
        public final CommandResponseTracker.MessagesWithArg<ServerPlayer, Integer> multipleAdvancementsSuccessResponse;
        public final CommandResponseTracker.DispatchWithArg<CommandSyntaxException, ServerPlayer, Integer> multipleAdvancementsError;
        public final CommandResponseTracker.MessagesWithArgs<ServerPlayer, AdvancementHolder, String> criterionSuccessResponse;
        public final CommandResponseTracker.DispatchWithArgs<CommandSyntaxException, ServerPlayer, AdvancementHolder, String> criterionError;

        private Action(String key) {
            Dynamic2CommandExceptionType singleAdvancementSinglePlayerError = new Dynamic2CommandExceptionType((advancement, player) -> Component.translatableEscape("commands.advancement." + key + ".one.to.one.failure", advancement, player));
            Dynamic2CommandExceptionType singleAdvancementMultiplePlayersError = new Dynamic2CommandExceptionType((advancement, playerCount) -> Component.translatableEscape("commands.advancement." + key + ".one.to.many.failure", advancement, playerCount));
            this.singleAdvancementsError = new CommandResponseTracker.DispatchWithArg<CommandSyntaxException, ServerPlayer, AdvancementHolder>((player, n, advancement) -> singleAdvancementSinglePlayerError.create((Object)Advancement.name(advancement), (Object)player.getDisplayName()), (playerCount, n, advancement) -> singleAdvancementMultiplePlayersError.create((Object)Advancement.name(advancement), (Object)playerCount));
            this.multipleAdvancementsSuccessResponse = CommandResponseTracker.messages((player, n, advancementCount) -> Component.translatable("commands.advancement." + key + ".many.to.one.success", advancementCount, player.getDisplayName()), (playerCount, n, advancementCount) -> Component.translatable("commands.advancement." + key + ".many.to.many.success", advancementCount, playerCount));
            Dynamic2CommandExceptionType multipleAdvancementSinglePlayerError = new Dynamic2CommandExceptionType((advancementCount, player) -> Component.translatableEscape("commands.advancement." + key + ".many.to.one.failure", advancementCount, player));
            Dynamic2CommandExceptionType multipleAdvancementMultiplePlayersError = new Dynamic2CommandExceptionType((advancementCount, playerCount) -> Component.translatableEscape("commands.advancement." + key + ".many.to.many.failure", advancementCount, playerCount));
            this.multipleAdvancementsError = new CommandResponseTracker.DispatchWithArg<CommandSyntaxException, ServerPlayer, Integer>((player, n, advancementCount) -> multipleAdvancementSinglePlayerError.create(advancementCount, (Object)player.getDisplayName()), (playerCount, n, advancementCount) -> multipleAdvancementMultiplePlayersError.create(advancementCount, (Object)playerCount));
            this.criterionSuccessResponse = CommandResponseTracker.messages((player, n, advancement, criterion) -> Component.translatable("commands.advancement." + key + ".criterion.to.one.success", criterion, Advancement.name(advancement), player.getDisplayName()), (playerCount, n, advancement, criterion) -> Component.translatable("commands.advancement." + key + ".criterion.to.many.success", criterion, Advancement.name(advancement), playerCount));
            Dynamic3CommandExceptionType criterionSinglePlayerError = new Dynamic3CommandExceptionType((criterion, advancement, player) -> Component.translatableEscape("commands.advancement." + key + ".criterion.to.one.failure", criterion, advancement, player));
            Dynamic3CommandExceptionType criterionMultiplePlayersError = new Dynamic3CommandExceptionType((criterion, advancement, playerCount) -> Component.translatableEscape("commands.advancement." + key + ".criterion.to.many.failure", criterion, advancement, playerCount));
            this.criterionError = new CommandResponseTracker.DispatchWithArgs<CommandSyntaxException, ServerPlayer, AdvancementHolder, String>((player, n, advancement, criterion) -> criterionSinglePlayerError.create(criterion, (Object)Advancement.name(advancement), (Object)player.getDisplayName()), (playerCount, n, advancement, criterion) -> criterionMultiplePlayersError.create(criterion, (Object)Advancement.name(advancement), (Object)playerCount));
        }

        public int perform(ServerPlayer player, Iterable<AdvancementHolder> advancements, boolean showAdvancements) {
            int count = 0;
            if (!showAdvancements) {
                player.getAdvancements().flushDirty(player, true);
            }
            for (AdvancementHolder advancement : advancements) {
                if (!this.perform(player, advancement)) continue;
                ++count;
            }
            if (!showAdvancements) {
                player.getAdvancements().flushDirty(player, false);
            }
            return count;
        }

        protected abstract boolean perform(ServerPlayer var1, AdvancementHolder var2);

        protected abstract boolean performCriterion(ServerPlayer var1, AdvancementHolder var2, String var3);
    }

    private static enum Mode {
        ONLY(false, false),
        THROUGH(true, true),
        FROM(false, true),
        UNTIL(true, false),
        EVERYTHING(true, true);

        private final boolean parents;
        private final boolean children;

        private Mode(boolean parents, boolean children) {
            this.parents = parents;
            this.children = children;
        }
    }
}

