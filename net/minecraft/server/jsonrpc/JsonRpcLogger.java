/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 *  org.slf4j.spi.LoggingEventBuilder
 */
package net.minecraft.server.jsonrpc;

import com.mojang.logging.LogUtils;
import net.minecraft.server.jsonrpc.methods.ClientInfo;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;

public class JsonRpcLogger {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREFIX = "RPC Connection #{}: ";

    public void log(ClientInfo clientInfo, String message, Object ... args) {
        LoggingEventBuilder builder = LOGGER.atInfo().setMessage(PREFIX + message).addArgument((Object)clientInfo.connectionId());
        for (Object arg : args) {
            builder = builder.addArgument(arg);
        }
        builder.log();
    }
}

