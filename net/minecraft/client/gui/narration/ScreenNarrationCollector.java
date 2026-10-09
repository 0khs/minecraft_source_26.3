/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.gui.narration;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationThunk;
import net.minecraft.client.gui.narration.NarrationTrigger;

public class ScreenNarrationCollector {
    private int generation;
    private final Map<EntryKey, NarrationEntry> entries = Maps.newTreeMap(Comparator.comparing(e -> e.type).thenComparing(e -> e.depth));

    public void update(Consumer<NarrationElementOutput> updater, NarrationTrigger narrationTrigger) {
        ++this.generation;
        updater.accept(new Output(this, 0, narrationTrigger));
    }

    public String collectNarrationText(boolean force) {
        final StringBuilder result = new StringBuilder();
        Consumer<String> appender = new Consumer<String>(this){
            private boolean firstEntry;
            {
                Objects.requireNonNull(this$0);
                this.firstEntry = true;
            }

            @Override
            public void accept(String s) {
                if (!this.firstEntry) {
                    result.append(". ");
                }
                this.firstEntry = false;
                result.append(s);
            }
        };
        this.entries.forEach((k, v) -> {
            if (v.generation == this.generation && (force || !v.alreadyNarrated)) {
                v.contents.getText(appender);
                v.alreadyNarrated = true;
            }
        });
        return result.toString();
    }

    private class Output
    implements NarrationElementOutput {
        private final int depth;
        private final NarrationTrigger narrationTrigger;
        final /* synthetic */ ScreenNarrationCollector this$0;

        private Output(ScreenNarrationCollector screenNarrationCollector, int depth, NarrationTrigger narrationTrigger) {
            ScreenNarrationCollector screenNarrationCollector2 = screenNarrationCollector;
            Objects.requireNonNull(screenNarrationCollector2);
            this.this$0 = screenNarrationCollector2;
            this.depth = depth;
            this.narrationTrigger = narrationTrigger;
        }

        @Override
        public void add(NarratedElementType type, NarrationThunk<?> contents) {
            this.this$0.entries.computeIfAbsent(new EntryKey(type, this.depth), k -> new NarrationEntry()).update(this.this$0.generation, contents);
        }

        @Override
        public NarrationElementOutput nest() {
            return new Output(this.this$0, this.depth + 1, this.narrationTrigger);
        }

        @Override
        public NarrationTrigger narrationTrigger() {
            return this.narrationTrigger;
        }
    }

    private static class NarrationEntry {
        private NarrationThunk<?> contents = NarrationThunk.EMPTY;
        private int generation = -1;
        private boolean alreadyNarrated;

        private NarrationEntry() {
        }

        public NarrationEntry update(int generation, NarrationThunk<?> contents) {
            if (!this.contents.equals(contents)) {
                this.contents = contents;
                this.alreadyNarrated = false;
            } else if (this.generation + 1 != generation) {
                this.alreadyNarrated = false;
            }
            this.generation = generation;
            return this;
        }
    }

    private record EntryKey(NarratedElementType type, int depth) {
    }
}

