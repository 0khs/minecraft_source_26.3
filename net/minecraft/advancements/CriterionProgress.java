/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements;

import io.netty.buffer.ByteBuf;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

public class CriterionProgress {
    public static final StreamCodec<ByteBuf, CriterionProgress> STREAM_CODEC = ByteBufCodecs.INSTANT.apply(ByteBufCodecs::optional).map(obtained -> new CriterionProgress(obtained.orElse(null)), progress -> Optional.ofNullable(progress.obtained));
    private @Nullable Instant obtained;

    public CriterionProgress() {
    }

    public CriterionProgress(@Nullable Instant obtained) {
        this.obtained = obtained;
    }

    public boolean isDone() {
        return this.obtained != null;
    }

    public void grant() {
        this.obtained = Instant.now();
    }

    public void revoke() {
        this.obtained = null;
    }

    public @Nullable Instant getObtained() {
        return this.obtained;
    }

    public String toString() {
        return "CriterionProgress{obtained=" + String.valueOf(this.obtained == null ? "false" : this.obtained) + "}";
    }
}

