/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timeline;

public class TimeCommand {
    private static final DynamicCommandExceptionType ERROR_NO_DEFAULT_CLOCK = new DynamicCommandExceptionType(dimension -> Component.translatableEscape("commands.time.no_default_clock", dimension));
    private static final Dynamic2CommandExceptionType ERROR_NO_TIME_MARKER_FOUND = new Dynamic2CommandExceptionType((clock, timeMarker) -> Component.translatableEscape("commands.time.no_time_marker_found", timeMarker, clock));
    private static final Dynamic2CommandExceptionType ERROR_WRONG_TIMELINE_FOR_CLOCK = new Dynamic2CommandExceptionType((clock, timeline) -> Component.translatableEscape("commands.time.wrong_timeline_for_clock", timeline, clock));
    private static final Dynamic2CommandExceptionType ERROR_ALREADY_AT_TIME_MARKER = new Dynamic2CommandExceptionType((clock, marker) -> Component.translatableEscape("commands.time.set.already_at_time_marker", clock, marker));
    private static final Dynamic2CommandExceptionType ERROR_ALREADY_AT_TIME = new Dynamic2CommandExceptionType((clock, time) -> Component.translatableEscape("commands.time.set.already_at_time", clock, time));
    private static final DynamicCommandExceptionType ERROR_ALREADY_PAUSED = new DynamicCommandExceptionType(clock -> Component.translatableEscape("commands.time.pause.already_paused", clock));
    private static final DynamicCommandExceptionType ERROR_ALREADY_RUNNING = new DynamicCommandExceptionType(clock -> Component.translatableEscape("commands.time.pause.already_running", clock));
    private static final Dynamic2CommandExceptionType ERROR_ALREADY_SAME_RATE = new Dynamic2CommandExceptionType((clock, rate) -> Component.translatableEscape("commands.time.rate.already_same", clock, rate));
    private static final int MAX_CLOCK_RATE = 1000;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralArgumentBuilder baseCommand = (LiteralArgumentBuilder)Commands.literal("time").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        dispatcher.register(TimeCommand.addClockNodes(context, baseCommand, c -> TimeCommand.getDefaultClock((CommandSourceStack)c.getSource())));
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)baseCommand.then(Commands.literal("query").then(Commands.literal("gametime").executes(c -> TimeCommand.queryGameTime((CommandSourceStack)c.getSource()))))).then(Commands.literal("of").then(TimeCommand.addClockNodes(context, Commands.argument("clock", ResourceArgument.resource(context, Registries.WORLD_CLOCK)), c -> ResourceArgument.getClock((CommandContext<CommandSourceStack>)c, "clock")))));
    }

    private static <A extends ArgumentBuilder<CommandSourceStack, A>> A addClockNodes(CommandBuildContext context, A node, ClockGetter clockGetter) {
        return (A)node.then(((LiteralArgumentBuilder)Commands.literal("set").then(Commands.argument("time", TimeArgument.time()).executes(c -> TimeCommand.setTotalTicks((CommandSourceStack)c.getSource(), clockGetter.getClock((CommandContext<CommandSourceStack>)c), IntegerArgumentType.getInteger((CommandContext)c, (String)"time"))))).then(Commands.argument("timemarker", IdentifierArgument.id()).suggests((c, p) -> TimeCommand.suggestTimeMarkers((CommandSourceStack)c.getSource(), p, clockGetter.getClock((CommandContext<CommandSourceStack>)c))).executes(c -> TimeCommand.setTimeToTimeMarker((CommandSourceStack)c.getSource(), clockGetter.getClock((CommandContext<CommandSourceStack>)c), ResourceKey.create(ClockTimeMarkers.ROOT_ID, IdentifierArgument.getId((CommandContext<CommandSourceStack>)c, "timemarker")))))).then(Commands.literal("add").then(Commands.argument("time", TimeArgument.time(Integer.MIN_VALUE)).executes(c -> TimeCommand.addTime((CommandSourceStack)c.getSource(), clockGetter.getClock((CommandContext<CommandSourceStack>)c), IntegerArgumentType.getInteger((CommandContext)c, (String)"time"))))).then(Commands.literal("pause").executes(c -> TimeCommand.setPaused((CommandSourceStack)c.getSource(), clockGetter.getClock((CommandContext<CommandSourceStack>)c), true))).then(Commands.literal("resume").executes(c -> TimeCommand.setPaused((CommandSourceStack)c.getSource(), clockGetter.getClock((CommandContext<CommandSourceStack>)c), false))).then(Commands.literal("rate").then(Commands.argument("rate", FloatArgumentType.floatArg((float)1.0E-5f, (float)1000.0f)).executes(c -> TimeCommand.setRate((CommandSourceStack)c.getSource(), clockGetter.getClock((CommandContext<CommandSourceStack>)c), FloatArgumentType.getFloat((CommandContext)c, (String)"rate"))))).then(((LiteralArgumentBuilder)Commands.literal("query").then(Commands.literal("time").executes(c -> TimeCommand.queryTime((CommandSourceStack)c.getSource(), clockGetter.getClock((CommandContext<CommandSourceStack>)c))))).then(((RequiredArgumentBuilder)Commands.argument("timeline", ResourceArgument.resource(context, Registries.TIMELINE)).suggests((c, p) -> TimeCommand.suggestTimelines((CommandSourceStack)c.getSource(), p, clockGetter.getClock((CommandContext<CommandSourceStack>)c))).executes(c -> TimeCommand.queryTimelineTicks((CommandSourceStack)c.getSource(), clockGetter.getClock((CommandContext<CommandSourceStack>)c), ResourceArgument.getTimeline((CommandContext<CommandSourceStack>)c, "timeline")))).then(Commands.literal("repetition").executes(c -> TimeCommand.queryTimelineRepetitions((CommandSourceStack)c.getSource(), clockGetter.getClock((CommandContext<CommandSourceStack>)c), ResourceArgument.getTimeline((CommandContext<CommandSourceStack>)c, "timeline"))))));
    }

    private static CompletableFuture<Suggestions> suggestTimeMarkers(CommandSourceStack source, SuggestionsBuilder builder, Holder<WorldClock> clock) {
        return SharedSuggestionProvider.suggestResource(source.getServer().clockManager().commandTimeMarkersForClock(clock).map(ResourceKey::identifier), builder);
    }

    private static CompletableFuture<Suggestions> suggestTimelines(CommandSourceStack source, SuggestionsBuilder builder, Holder<WorldClock> clock) {
        Stream<ResourceKey> timelines = source.registryAccess().lookupOrThrow(Registries.TIMELINE).listElements().filter(timeline -> ((Timeline)timeline.value()).clock().equals(clock)).map(Holder.Reference::key);
        return SharedSuggestionProvider.suggestResource(timelines.map(ResourceKey::identifier), builder);
    }

    private static int queryGameTime(CommandSourceStack source) {
        long gameTime = source.getLevel().getGameTime();
        source.sendSuccess(() -> Component.translatable("commands.time.query.gametime", gameTime), false);
        return TimeCommand.wrapTime(gameTime);
    }

    private static int queryTime(CommandSourceStack source, Holder<WorldClock> clock) {
        ServerClockManager clockManager = source.getServer().clockManager();
        long totalTicks = ((ServerClockManager.ServerClockInstance)clockManager.getInstance((Holder)clock)).totalTicks();
        source.sendSuccess(() -> Component.translatable("commands.time.query.absolute", clock.getRegisteredName(), totalTicks), false);
        return TimeCommand.wrapTime(totalTicks);
    }

    private static int queryTimelineTicks(CommandSourceStack source, Holder<WorldClock> clock, Holder<Timeline> timeline) throws CommandSyntaxException {
        if (!clock.equals(timeline.value().clock())) {
            throw ERROR_WRONG_TIMELINE_FOR_CLOCK.create((Object)clock.getRegisteredName(), (Object)timeline.getRegisteredName());
        }
        ServerClockManager clockManager = source.getServer().clockManager();
        long currentTicks = timeline.value().getCurrentTicks(clockManager);
        source.sendSuccess(() -> Component.translatable("commands.time.query.timeline", timeline.getRegisteredName(), currentTicks), false);
        return TimeCommand.wrapTime(currentTicks);
    }

    private static int queryTimelineRepetitions(CommandSourceStack source, Holder<WorldClock> clock, Holder<Timeline> timeline) throws CommandSyntaxException {
        if (!clock.equals(timeline.value().clock())) {
            throw ERROR_WRONG_TIMELINE_FOR_CLOCK.create((Object)clock.getRegisteredName(), (Object)timeline.getRegisteredName());
        }
        ServerClockManager clockManager = source.getServer().clockManager();
        long repetitions = timeline.value().getPeriodCount(clockManager);
        source.sendSuccess(() -> Component.translatable("commands.time.query.timeline.repetitions", timeline.getRegisteredName(), repetitions), false);
        return TimeCommand.wrapTime(repetitions);
    }

    private static int setTotalTicks(CommandSourceStack source, Holder<WorldClock> clock, int totalTicks) throws CommandSyntaxException {
        ServerClockManager clockManager = source.getServer().clockManager();
        if (((ServerClockManager.ServerClockInstance)clockManager.getInstance((Holder)clock)).totalTicks() == (long)totalTicks) {
            throw ERROR_ALREADY_AT_TIME.create((Object)clock.getRegisteredName(), (Object)totalTicks);
        }
        clockManager.setTotalTicks(clock, totalTicks);
        source.sendSuccess(() -> Component.translatable("commands.time.set.absolute", clock.getRegisteredName(), totalTicks), true);
        return totalTicks;
    }

    private static int addTime(CommandSourceStack source, Holder<WorldClock> clock, int time) {
        ServerClockManager clockManager = source.getServer().clockManager();
        clockManager.addTicks(clock, time);
        long totalTicks = ((ServerClockManager.ServerClockInstance)clockManager.getInstance((Holder)clock)).totalTicks();
        source.sendSuccess(() -> Component.translatable("commands.time.set.absolute", clock.getRegisteredName(), totalTicks), true);
        return TimeCommand.wrapTime(totalTicks);
    }

    private static int setTimeToTimeMarker(CommandSourceStack source, Holder<WorldClock> clock, ResourceKey<ClockTimeMarker> timeMarkerId) throws CommandSyntaxException {
        ServerClockManager clockManager = source.getServer().clockManager();
        ServerClockManager.MoveResult moveResult = clockManager.moveToTimeMarker(clock, timeMarkerId);
        String clockName = clock.getRegisteredName();
        String timeMarkerName = timeMarkerId.identifier().toString();
        switch (moveResult) {
            case NO_TIME_MARKER_FOUND: {
                throw ERROR_NO_TIME_MARKER_FOUND.create((Object)clockName, (Object)timeMarkerName);
            }
            case NOT_MOVED: {
                throw ERROR_ALREADY_AT_TIME_MARKER.create((Object)clockName, (Object)timeMarkerName);
            }
            case MOVED: {
                source.sendSuccess(() -> Component.translatable("commands.time.set.time_marker", clockName, timeMarkerName), true);
            }
        }
        return TimeCommand.wrapTime(((ServerClockManager.ServerClockInstance)clockManager.getInstance((Holder)clock)).totalTicks());
    }

    private static int setPaused(CommandSourceStack source, Holder<WorldClock> clock, boolean paused) throws CommandSyntaxException {
        ServerClockManager clockManager = source.getServer().clockManager();
        String clockName = clock.getRegisteredName();
        if (((ServerClockManager.ServerClockInstance)clockManager.getInstance((Holder)clock)).isPaused() == paused) {
            if (paused) {
                throw ERROR_ALREADY_PAUSED.create((Object)clockName);
            }
            throw ERROR_ALREADY_RUNNING.create((Object)clockName);
        }
        clockManager.setPaused(clock, paused);
        source.sendSuccess(() -> Component.translatable(paused ? "commands.time.pause" : "commands.time.resume", clockName), true);
        return 1;
    }

    private static int setRate(CommandSourceStack source, Holder<WorldClock> clock, float rate) throws CommandSyntaxException {
        ServerClockManager clockManager = source.getServer().clockManager();
        if (((ServerClockManager.ServerClockInstance)clockManager.getInstance((Holder)clock)).rate() == rate) {
            throw ERROR_ALREADY_SAME_RATE.create((Object)clock.getRegisteredName(), (Object)Float.valueOf(rate));
        }
        clockManager.setRate(clock, rate);
        source.sendSuccess(() -> Component.translatable("commands.time.rate", clock.getRegisteredName(), Float.valueOf(rate)), true);
        return 1;
    }

    private static int wrapTime(long ticks) {
        return Math.toIntExact(ticks % Integer.MAX_VALUE);
    }

    private static Holder<WorldClock> getDefaultClock(CommandSourceStack source) throws CommandSyntaxException {
        Holder<DimensionType> dimensionType = source.getLevel().dimensionTypeRegistration();
        return dimensionType.value().defaultClock().orElseThrow(() -> ERROR_NO_DEFAULT_CLOCK.create((Object)dimensionType.getRegisteredName()));
    }

    private static interface ClockGetter {
        public Holder<WorldClock> getClock(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }
}

