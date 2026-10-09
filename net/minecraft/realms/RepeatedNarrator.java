/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.util.concurrent.RateLimiter
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.GameNarrator;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class RepeatedNarrator {
    private final float permitsPerSecond;
    private final AtomicReference<@Nullable Params> params = new AtomicReference();

    public RepeatedNarrator(Duration repeatDelay) {
        this.permitsPerSecond = 1000.0f / (float)repeatDelay.toMillis();
    }

    public void narrate(GameNarrator narrator, Component narration) {
        Params params = this.params.updateAndGet(existing -> {
            if (existing == null || !narration.equals(existing.narration)) {
                return new Params(narration, RateLimiter.create((double)this.permitsPerSecond));
            }
            return existing;
        });
        if (params.rateLimiter.tryAcquire(1)) {
            narrator.saySystemNow(narration);
        }
    }

    private record Params(Component narration, RateLimiter rateLimiter) {
    }
}

