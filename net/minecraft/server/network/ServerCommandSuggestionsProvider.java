/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerCommandSuggestionsProvider {
    private static final int MAX_COMMAND_SUGGESTIONS = 1000;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_INTERVAL_TICKS = 1;
    private final ServerPlayer player;
    private final AtomicReference<@Nullable Request> lastRequest = new AtomicReference();
    private volatile boolean hasBudget;

    public ServerCommandSuggestionsProvider(ServerPlayer player) {
        this.player = player;
    }

    public void tick() {
        if (this.player.tickCount % 1 == 0) {
            this.hasBudget = true;
            this.tryProcessRequest();
        }
    }

    private void tryProcessRequest() {
        if (!this.hasBudget) {
            return;
        }
        Request request = this.lastRequest.getAndSet(null);
        if (request == null) {
            return;
        }
        this.hasBudget = false;
        try {
            StringReader reader = new StringReader(request.command);
            if (reader.canRead() && reader.peek() == '/') {
                reader.skip();
            }
            MinecraftServer server = this.player.level().getServer();
            CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
            ParseResults parse = dispatcher.parse(reader, (Object)this.player.createCommandSourceStack());
            if (this.lastRequest.get() != null) {
                return;
            }
            dispatcher.getCompletionSuggestions(parse).thenAccept(suggestions -> request.future.complete(ServerCommandSuggestionsProvider.limitSuggestionCount(suggestions)));
        }
        catch (Exception e) {
            LOGGER.error("Failed to resolve command suggestions for {}", (Object)this.player.getGameProfile().name(), (Object)e);
        }
    }

    private static Suggestions limitSuggestionCount(Suggestions suggestions) {
        if (suggestions.getList().size() <= 1000) {
            return suggestions;
        }
        return new Suggestions(suggestions.getRange(), suggestions.getList().subList(0, 1000));
    }

    private void trySchedule() {
        if (this.hasBudget) {
            this.player.level().getServer().execute(this::tryProcessRequest);
        }
    }

    public CompletableFuture<Suggestions> request(String command) {
        Request request = new Request(command);
        Request oldRequest = this.lastRequest.getAndSet(request);
        if (oldRequest == null) {
            this.trySchedule();
        }
        return request.future;
    }

    private static class Request {
        private final String command;
        private final CompletableFuture<Suggestions> future = new CompletableFuture();

        private Request(String command) {
            this.command = command;
        }
    }
}

