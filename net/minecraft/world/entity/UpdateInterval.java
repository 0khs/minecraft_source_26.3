/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

public interface UpdateInterval {
    public static final UpdateInterval NEVER = new UpdateInterval(){

        @Override
        public boolean test(int tick) {
            return false;
        }

        @Override
        public int nextInterval(int tick) {
            return 0;
        }
    };

    public boolean test(int var1);

    public int nextInterval(int var1);

    public static UpdateInterval periodic(final int period) {
        return new UpdateInterval(){

            @Override
            public boolean test(int tick) {
                return tick % period == 0;
            }

            @Override
            public int nextInterval(int tick) {
                return tick / period * period + period;
            }
        };
    }
}

