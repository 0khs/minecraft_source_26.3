/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.Identifier;

public record ClientboundUpdateAdvancementsPacket(boolean shouldReset, List<PositionedAdvancement> added, Set<Identifier> removed, Map<Identifier, AdvancementProgress> progress, boolean showAdvancements) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateAdvancementsPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, ClientboundUpdateAdvancementsPacket::shouldReset, PositionedAdvancement.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundUpdateAdvancementsPacket::added, Identifier.STREAM_CODEC.apply(ByteBufCodecs.collection(Sets::newLinkedHashSetWithExpectedSize)), ClientboundUpdateAdvancementsPacket::removed, ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, AdvancementProgress.STREAM_CODEC), ClientboundUpdateAdvancementsPacket::progress, ByteBufCodecs.BOOL, ClientboundUpdateAdvancementsPacket::showAdvancements, ClientboundUpdateAdvancementsPacket::new);

    @Override
    public PacketType<ClientboundUpdateAdvancementsPacket> type() {
        return GamePacketTypes.CLIENTBOUND_UPDATE_ADVANCEMENTS;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handleUpdateAdvancementsPacket(this);
    }

    public record PositionedAdvancement(AdvancementHolder advancement, float x, float y) {
        public static final StreamCodec<RegistryFriendlyByteBuf, PositionedAdvancement> STREAM_CODEC = StreamCodec.composite(AdvancementHolder.STREAM_CODEC, PositionedAdvancement::advancement, ByteBufCodecs.FLOAT, PositionedAdvancement::x, ByteBufCodecs.FLOAT, PositionedAdvancement::y, PositionedAdvancement::new);

        public static PositionedAdvancement fromNode(AdvancementNode node) {
            return new PositionedAdvancement(node.holder(), node.x(), node.y());
        }
    }
}

