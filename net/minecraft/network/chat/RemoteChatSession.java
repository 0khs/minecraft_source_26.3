/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.chat;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record RemoteChatSession(UUID sessionId, ProfilePublicKey profilePublicKey) {
    public SignedMessageValidator createMessageValidator(Duration gracePeriod) {
        return new SignedMessageValidator.KeyBased(this.profilePublicKey.createSignatureValidator(), () -> this.profilePublicKey.data().hasExpired(gracePeriod));
    }

    public SignedMessageChain.Decoder createMessageDecoder(UUID profileId) {
        return new SignedMessageChain(profileId, this.sessionId).decoder(this.profilePublicKey);
    }

    public Data asData() {
        return new Data(this.sessionId, this.profilePublicKey.data());
    }

    public boolean hasExpired() {
        return this.profilePublicKey.data().hasExpired();
    }

    public record Data(UUID sessionId, ProfilePublicKey.Data profilePublicKey) {
        public static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, Data::sessionId, ProfilePublicKey.Data.STREAM_CODEC, Data::profilePublicKey, Data::new);

        public RemoteChatSession validate(GameProfile profile, SignatureValidator serviceSignatureValidator) throws ProfilePublicKey.ValidationException {
            return new RemoteChatSession(this.sessionId, ProfilePublicKey.createValidated(serviceSignatureValidator, profile.id(), this.profilePublicKey));
        }
    }
}

