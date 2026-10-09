/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.TelemetryEvent
 *  com.mojang.authlib.minecraft.TelemetrySession
 *  com.mojang.serialization.Codec
 */
package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.serialization.Codec;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryPropertyMap;

public record TelemetryEventInstance(TelemetryEventType type, TelemetryPropertyMap properties) {
    public static final Codec<TelemetryEventInstance> CODEC = TelemetryEventType.CODEC.dispatchStable(TelemetryEventInstance::type, TelemetryEventType::codec);

    public TelemetryEventInstance {
        properties.propertySet().forEach(property -> {
            if (!type.contains(property)) {
                throw new IllegalArgumentException("Property '" + property.id() + "' not expected for event: '" + type.id() + "'");
            }
        });
    }

    public TelemetryEvent export(TelemetrySession session) {
        return this.type.export(session, this.properties);
    }
}

