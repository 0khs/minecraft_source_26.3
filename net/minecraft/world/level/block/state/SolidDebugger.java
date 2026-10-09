/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.state;

import com.mojang.logging.LogUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

public class SolidDebugger {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ScopedValue<SolidDebugger> STATUS = ScopedValue.newInstance();
    private final Map<Holder<Block>, Reason> reasonByBlock = new LinkedHashMap<Holder<Block>, Reason>();

    public static boolean logAndGet(Holder<Block> block, Reason reason) {
        Reason existingReason;
        if (STATUS.isBound() && (existingReason = SolidDebugger.STATUS.get().reasonByBlock.putIfAbsent(block, reason)) != null && existingReason.causesSolid != reason.causesSolid) {
            LOGGER.debug("Block {} has multiple values for solid. Existing: {} ({}) New: {} ({})", new Object[]{block, existingReason.causesSolid, existingReason.message, reason.causesSolid, reason.message});
        }
        return reason.causesSolid;
    }

    public void dump() {
        Map blocksByReason = this.reasonByBlock.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        blocksByReason.forEach((reason, blocks) -> {
            if (!blocks.isEmpty()) {
                String blockNames = blocks.stream().map(b -> "\t" + b.getRegisteredName()).collect(Collectors.joining("\n"));
                LOGGER.debug("The following blocks are {} because: {}:\n {}", new Object[]{reason.causesSolid ? "solid" : "not solid", reason.message, blockNames});
            }
        });
    }

    public static void runAndDump(Runnable task) {
        if (SharedConstants.DEBUG_CALCULATE_SOLID) {
            SolidDebugger solidDebugger = new SolidDebugger();
            ScopedValue.where(STATUS, solidDebugger).run(task);
            solidDebugger.dump();
        } else {
            task.run();
        }
    }

    public static enum Reason {
        FORCE_SOLID_ON(true, "forceSolidOn called on properties"),
        LARGE_ENOUGH_COLLISION_SHAPE(true, "collision shape bounds size is large enough"),
        HIGH_ENOUGH_COLLISION_SHAPE(true, "collision shape bounds are higher than 1"),
        FORCE_SOLID_OFF(false, "forceSolidOff called on properties"),
        NULL_CACHE(false, "cache is null"),
        EMPTY_COLLISION_SHAPE(false, "collision shape is empty"),
        FALLTHROUGH(false, "fallthrough");

        private final boolean causesSolid;
        private final String message;

        private Reason(boolean causesSolid, String message) {
            this.causesSolid = causesSolid;
            this.message = message;
        }
    }
}

