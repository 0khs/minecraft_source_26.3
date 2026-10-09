/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.palette;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.resources.palette.Palette;
import net.minecraft.client.resources.palette.PaletteMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

public class PaletteMappingCache {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final LoadingCache<IdPair, PaletteMapping> cache;

    public PaletteMappingCache(final Map<Identifier, Palette> palettes) {
        this.cache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5L)).maximumSize(256L).build((CacheLoader)new CacheLoader<IdPair, PaletteMapping>(this){
            {
                Objects.requireNonNull(this$0);
            }

            public PaletteMapping load(IdPair pair) {
                Palette basePalette = (Palette)palettes.get(pair.base);
                Palette targetPalette = (Palette)palettes.get(pair.target);
                if (basePalette == null) {
                    LOGGER.warn("Couldn't find base palette {}", (Object)pair.base);
                    return PaletteMapping.NONE;
                }
                if (targetPalette == null) {
                    LOGGER.warn("Couldn't find target palette {}", (Object)pair.target);
                    return PaletteMapping.NONE;
                }
                if (basePalette.size() != targetPalette.size()) {
                    LOGGER.warn("Could not create palette mapping for {}, had different sizes", (Object)pair);
                    return PaletteMapping.NONE;
                }
                return PaletteMapping.create(basePalette, targetPalette);
            }
        });
    }

    public PaletteMapping get(Identifier baseId, Identifier targetId) {
        return (PaletteMapping)this.cache.getUnchecked((Object)new IdPair(baseId, targetId));
    }

    private record IdPair(Identifier base, Identifier target) {
    }
}

