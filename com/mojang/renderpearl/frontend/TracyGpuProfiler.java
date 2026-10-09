/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.GpuApi
 *  com.mojang.jtracy.GpuContext
 *  com.mojang.jtracy.TracyClient
 */
package com.mojang.renderpearl.frontend;

import com.mojang.jtracy.GpuApi;
import com.mojang.jtracy.GpuContext;
import com.mojang.jtracy.TracyClient;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
import java.util.OptionalLong;

public class TracyGpuProfiler {
    private static final int MAX_QUERIES = 1024;
    private static final long TIME_BETWEEN_CALIBRATION = 5000000000L;
    private final GpuQueryPool queries;
    private final GpuContext context;
    private int head = 0;
    private int tail = 0;
    private final double period;
    private final FrontendGpuDevice device;
    private long calibrationOffset;
    private long lastCalibrationTime;

    public TracyGpuProfiler(FrontendGpuDevice device) {
        this.queries = device.createTimestampQueryPool(1024);
        this.context = TracyClient.createGpuContext((GpuApi)GpuApi.OPENGL, (long)System.nanoTime(), (float)1.0f);
        this.period = device.getDeviceInfo().timestampPeriod();
        this.device = device;
        this.lastCalibrationTime = System.nanoTime();
        this.calibrationOffset = device.getTimestampCalibrationOffset();
    }

    public void close() {
        this.queries.close();
    }

    public void pushZone(CommandEncoder encoder, String name) {
        int queryId = this.nextQueryId();
        encoder.writeTimestamp(this.queries, queryId);
        this.context.beginZone(queryId, name, "", "", 0);
    }

    public void popZone(CommandEncoder encoder) {
        int queryId = this.nextQueryId();
        encoder.writeTimestamp(this.queries, queryId);
        this.context.endZone(queryId);
    }

    public void endFrame() {
        OptionalLong timestamp;
        int i;
        OptionalLong[] timestamps;
        if (System.nanoTime() - this.lastCalibrationTime > 5000000000L) {
            this.calibrationOffset = this.device.getTimestampCalibrationOffset();
            this.lastCalibrationTime = System.nanoTime();
        }
        if (this.head < this.tail) {
            timestamps = this.queries.getValues(this.tail, 1024 - this.tail);
            for (i = 0; i < timestamps.length; ++i) {
                timestamp = timestamps[i];
                if (!timestamp.isPresent()) {
                    return;
                }
                this.context.submitQueryTimestamp(this.tail, (long)((double)timestamp.getAsLong() * this.period) + this.calibrationOffset);
                this.tail = (this.tail + 1) % 1024;
            }
        }
        if (this.tail < this.head) {
            timestamps = this.queries.getValues(this.tail, this.head - this.tail);
            for (i = 0; i < timestamps.length; ++i) {
                timestamp = timestamps[i];
                if (!timestamp.isPresent()) {
                    return;
                }
                this.context.submitQueryTimestamp(this.tail, (long)((double)timestamp.getAsLong() * this.period) + this.calibrationOffset);
                this.tail = (this.tail + 1) % 1024;
            }
        }
    }

    private int nextQueryId() {
        int id = this.head;
        this.head = (this.head + 1) % 1024;
        return id;
    }
}

