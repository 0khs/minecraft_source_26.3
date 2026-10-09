/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.ints;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;

public interface ContextIntProvider
extends Validatable {
    public int getIntUnsafe(LootContext var1) throws ArithmeticException;

    default public int getInt(LootContext context) {
        try {
            return this.getIntUnsafe(context);
        }
        catch (ArithmeticException arithmeticException) {
            return 0;
        }
    }

    public MapCodec<? extends ContextIntProvider> codec();

    public static int floatToIntSafe(float value) {
        if (!Float.isFinite(value)) {
            throw new ArithmeticException("Value " + value + " can't be safely converted to int");
        }
        return ContextIntProvider.longToIntSafe((long)value);
    }

    public static int longToIntSafe(long value) {
        int result = (int)value;
        if ((long)result != value) {
            throw new ArithmeticException("Value " + value + " can't be safely converted to int");
        }
        return result;
    }
}

