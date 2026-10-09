/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number.floats;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;

public interface ContextFloatProvider
extends Validatable {
    public float getFloatUnsafe(LootContext var1) throws ArithmeticException;

    default public float getFloat(LootContext context) {
        try {
            float result = this.getFloatUnsafe(context);
            if (Float.isFinite(result)) {
                return result;
            }
        }
        catch (ArithmeticException arithmeticException) {
            // empty catch block
        }
        return 0.0f;
    }

    default public float getFloatOrThrow(LootContext context) throws ArithmeticException {
        float value = this.getFloatUnsafe(context);
        if (!Float.isFinite(value)) {
            throw new ArithmeticException("Invalid value: " + value);
        }
        return value;
    }

    public MapCodec<? extends ContextFloatProvider> codec();

    public static float intToFloatSafe(int value) {
        return value;
    }
}

