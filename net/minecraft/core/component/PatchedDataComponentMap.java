/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.Removed;
import net.minecraft.core.component.TypedDataComponent;
import org.jspecify.annotations.Nullable;

public final class PatchedDataComponentMap
implements DataComponentMap {
    private final DataComponentMap prototype;
    private Reference2ObjectMap<DataComponentType<?>, Object> patch;
    private boolean copyOnWrite;

    public PatchedDataComponentMap(DataComponentMap prototype) {
        this(prototype, Reference2ObjectMaps.emptyMap(), true);
    }

    private PatchedDataComponentMap(DataComponentMap prototype, Reference2ObjectMap<DataComponentType<?>, Object> patch, boolean copyOnWrite) {
        this.prototype = prototype;
        this.patch = patch;
        this.copyOnWrite = copyOnWrite;
    }

    public static PatchedDataComponentMap fromPatch(DataComponentMap prototype, DataComponentPatch patch) {
        if (PatchedDataComponentMap.isPatchSanitized(prototype, patch.map)) {
            return new PatchedDataComponentMap(prototype, patch.map, true);
        }
        PatchedDataComponentMap map = new PatchedDataComponentMap(prototype);
        map.applyPatch(patch);
        return map;
    }

    private static boolean isPatchSanitized(DataComponentMap prototype, Reference2ObjectMap<DataComponentType<?>, Object> patch) {
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(patch)) {
            Object defaultValue = prototype.get((DataComponentType)entry.getKey());
            Object value = entry.getValue();
            if (value.equals(defaultValue)) {
                return false;
            }
            if (!Removed.isRemoved(value) || defaultValue != null) continue;
            return false;
        }
        return true;
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> type) {
        return DataComponentPatch.getFromPatchAndPrototype(this.patch, this.prototype, type);
    }

    public boolean hasNonDefault(DataComponentType<?> type) {
        return this.patch.containsKey(type);
    }

    public <T> @Nullable T set(DataComponentType<T> type, @Nullable T value) {
        this.ensureMapOwnership();
        T defaultValue = this.prototype.get(type);
        Object lastValue = Objects.equals(value, defaultValue) ? this.patch.remove(type) : this.patch.put(type, Removed.nullToRemoved(value));
        if (lastValue != null) {
            return (T)Objects.requireNonNullElse(Removed.removedToNull(lastValue), defaultValue);
        }
        return defaultValue;
    }

    public <T> @Nullable T set(TypedDataComponent<T> value) {
        return this.set(value.type(), value.value());
    }

    public <T> @Nullable T remove(DataComponentType<? extends T> type) {
        this.ensureMapOwnership();
        T defaultValue = this.prototype.get(type);
        Object lastValue = defaultValue != null ? this.patch.put(type, (Object)Removed.INSTANCE) : this.patch.remove(type);
        if (lastValue != null) {
            return (T)Removed.removedToNull(lastValue);
        }
        return defaultValue;
    }

    public void applyPatch(DataComponentPatch patch) {
        this.ensureMapOwnership();
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(patch.map)) {
            this.applyPatch((DataComponentType)entry.getKey(), entry.getValue());
        }
    }

    private void applyPatch(DataComponentType<?> type, Object value) {
        Object defaultValue = this.prototype.get(type);
        if (Removed.isNotRemoved(value)) {
            if (value.equals(defaultValue)) {
                this.patch.remove(type);
            } else {
                this.patch.put(type, value);
            }
        } else if (defaultValue != null) {
            this.patch.put(type, (Object)Removed.INSTANCE);
        } else {
            this.patch.remove(type);
        }
    }

    public void restorePatch(DataComponentPatch patch) {
        this.ensureMapOwnership();
        this.patch.clear();
        this.patch.putAll(patch.map);
    }

    public void clearPatch() {
        this.ensureMapOwnership();
        this.patch.clear();
    }

    public void setAll(DataComponentMap components) {
        for (TypedDataComponent<?> entry : components) {
            entry.applyTo(this);
        }
    }

    private void ensureMapOwnership() {
        if (this.copyOnWrite) {
            this.patch = new Reference2ObjectArrayMap(this.patch);
            this.copyOnWrite = false;
        }
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        if (this.patch.isEmpty()) {
            return this.prototype.keySet();
        }
        ReferenceArraySet components = new ReferenceArraySet(this.prototype.keySet());
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.patch)) {
            Object value = entry.getValue();
            if (Removed.isNotRemoved(value)) {
                components.add((DataComponentType)entry.getKey());
                continue;
            }
            components.remove(entry.getKey());
        }
        return components;
    }

    @Override
    public Iterator<TypedDataComponent<?>> iterator() {
        if (this.patch.isEmpty()) {
            return this.prototype.iterator();
        }
        ArrayList<TypedDataComponent> components = new ArrayList<TypedDataComponent>(this.patch.size() + this.prototype.size());
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.patch)) {
            Object value = entry.getValue();
            if (!Removed.isNotRemoved(value)) continue;
            components.add(TypedDataComponent.createUnchecked((DataComponentType)entry.getKey(), value));
        }
        for (TypedDataComponent component : this.prototype) {
            if (this.patch.containsKey(component.type())) continue;
            components.add(component);
        }
        return components.iterator();
    }

    @Override
    public int size() {
        int size = this.prototype.size();
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.patch)) {
            boolean inPrototype;
            boolean inPatch = Removed.isNotRemoved(entry.getValue());
            if (inPatch == (inPrototype = this.prototype.has((DataComponentType)entry.getKey()))) continue;
            size += inPatch ? 1 : -1;
        }
        return size;
    }

    public DataComponentPatch asPatch() {
        if (this.patch.isEmpty()) {
            return DataComponentPatch.EMPTY;
        }
        this.copyOnWrite = true;
        return new DataComponentPatch(this.patch);
    }

    public PatchedDataComponentMap copy() {
        this.copyOnWrite = true;
        return new PatchedDataComponentMap(this.prototype, this.patch, true);
    }

    public DataComponentMap toImmutableMap() {
        if (this.patch.isEmpty()) {
            return this.prototype;
        }
        return this.copy();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PatchedDataComponentMap)) return false;
        PatchedDataComponentMap otherMap = (PatchedDataComponentMap)obj;
        if (!this.prototype.equals(otherMap.prototype)) return false;
        if (!this.patch.equals(otherMap.patch)) return false;
        return true;
    }

    public int hashCode() {
        return this.prototype.hashCode() + this.patch.hashCode() * 31;
    }

    public String toString() {
        return "{" + this.stream().map(TypedDataComponent::toString).collect(Collectors.joining(", ")) + "}";
    }
}

