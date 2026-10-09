/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.clock.ClockInstance;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.clock.ClockNetworkState;
import net.minecraft.world.clock.WorldClock;

public class ClientClockManager
implements ClockManager {
    private final Map<Holder<WorldClock>, ClientClockInstance> clocks = new HashMap<Holder<WorldClock>, ClientClockInstance>();
    private long lastTickGameTime;

    @Override
    public ClientClockInstance getInstance(Holder<WorldClock> definition) {
        return this.clocks.computeIfAbsent(definition, holder -> new ClientClockInstance());
    }

    public void tick(long gameTime) {
        long gameTimeDelta = gameTime - this.lastTickGameTime;
        this.lastTickGameTime = gameTime;
        for (ClientClockInstance instance : this.clocks.values()) {
            double newPartialTicks = (double)instance.partialTick + (double)gameTimeDelta * (double)instance.rate;
            long fullTicks = Mth.floor(newPartialTicks);
            instance.partialTick = (float)(newPartialTicks - (double)fullTicks);
            instance.totalTicks += fullTicks;
        }
    }

    public void handleUpdates(long gameTime, Map<Holder<WorldClock>, ClockNetworkState> updates) {
        this.tick(gameTime);
        updates.forEach((definition, state) -> {
            ClockInstance clock = this.getInstance((Holder)definition);
            ((ClientClockInstance)clock).totalTicks = state.totalTicks();
            ((ClientClockInstance)clock).partialTick = state.partialTick();
            ((ClientClockInstance)clock).rate = state.rate();
        });
    }

    public static class ClientClockInstance
    implements ClockInstance {
        private long totalTicks;
        private float partialTick;
        private float rate = 1.0f;

        @Override
        public long totalTicks() {
            return this.totalTicks;
        }

        @Override
        public float partialTick() {
            return this.partialTick;
        }

        @Override
        public float rate() {
            return this.rate;
        }

        @Override
        public boolean isPaused() {
            return this.rate == 0.0f;
        }
    }
}

