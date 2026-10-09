/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.context;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public final class ContextMap {
    public static final ContextMap EMPTY = new ContextMap(Map.of());
    private final Map<ContextKey<?>, Object> params;

    private ContextMap(Map<ContextKey<?>, Object> params) {
        this.params = params;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean has(ContextKey<?> key) {
        return this.params.containsKey(key);
    }

    public <T> T getOrThrow(ContextKey<T> key) {
        T value = this.get(key);
        if (value == null) {
            throw new NoSuchElementException(key.name().toString());
        }
        return value;
    }

    public <T> @Nullable T get(ContextKey<T> key) {
        return (T)this.params.get(key);
    }

    @Contract(value="_,!null->!null; _,_->_")
    public <T> @Nullable T getOrDefault(ContextKey<T> param, @Nullable T _default) {
        return (T)this.params.getOrDefault(param, _default);
    }

    public static class Builder {
        private final Map<ContextKey<?>, Object> params = new Reference2ObjectOpenHashMap();

        private Builder() {
        }

        public <T> Builder set(ContextKey<T> param, @Nullable T value) {
            if (value == null) {
                this.params.remove(param);
            } else {
                this.params.put(param, value);
            }
            return this;
        }

        public <T> @Nullable T get(ContextKey<T> param) {
            return (T)this.params.get(param);
        }

        public ContextMap build() {
            if (this.params.isEmpty()) {
                return EMPTY;
            }
            return new ContextMap((Map<ContextKey<?>, Object>)new Reference2ObjectOpenHashMap(this.params));
        }

        public ContextMap buildAndValidate(ContextKeySet paramSet) {
            Sets.SetView notAllowed = Sets.difference(this.params.keySet(), paramSet.allowed());
            if (!notAllowed.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + String.valueOf(notAllowed));
            }
            Sets.SetView missingRequired = Sets.difference(paramSet.required(), this.params.keySet());
            if (!missingRequired.isEmpty()) {
                throw new IllegalArgumentException("Missing required parameters: " + String.valueOf(missingRequired));
            }
            return this.build();
        }
    }
}

