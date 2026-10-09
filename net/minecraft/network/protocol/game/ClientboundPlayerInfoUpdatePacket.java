/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.PropertyMap
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.IntFunction;
import net.minecraft.Optionull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

public class ClientboundPlayerInfoUpdatePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayerInfoUpdatePacket> STREAM_CODEC = Packet.codec(ClientboundPlayerInfoUpdatePacket::write, ClientboundPlayerInfoUpdatePacket::new);
    private final EnumSet<Action> actions;
    private final List<Entry> entries;

    public ClientboundPlayerInfoUpdatePacket(EnumSet<Action> actions, Collection<ServerPlayer> players) {
        this.actions = actions;
        this.entries = players.stream().map(Entry::new).toList();
    }

    public ClientboundPlayerInfoUpdatePacket(Action action, ServerPlayer player) {
        this.actions = EnumSet.of(action);
        this.entries = List.of(new Entry(player));
    }

    public static ClientboundPlayerInfoUpdatePacket createPlayerInitializing(Collection<ServerPlayer> players) {
        EnumSet<Action[]> actions = EnumSet.of(Action.ADD_PLAYER, new Action[]{Action.INITIALIZE_CHAT, Action.UPDATE_GAME_MODE, Action.UPDATE_LISTED, Action.UPDATE_LATENCY, Action.UPDATE_DISPLAY_NAME, Action.UPDATE_HAT, Action.UPDATE_LIST_ORDER});
        return new ClientboundPlayerInfoUpdatePacket(actions, players);
    }

    private static StreamCodec<RegistryFriendlyByteBuf, Entry> entryCodec(EnumSet<Action> actions) {
        return StreamCodec.of((output, entry) -> {
            output.writeUUID(entry.profileId());
            for (Action action : actions) {
                action.writer.write((RegistryFriendlyByteBuf)((Object)output), (Entry)entry);
            }
        }, input -> {
            EntryBuilder builder = new EntryBuilder(input.readUUID());
            for (Action action : actions) {
                action.reader.read(builder, (RegistryFriendlyByteBuf)((Object)input));
            }
            return builder.build();
        });
    }

    private ClientboundPlayerInfoUpdatePacket(RegistryFriendlyByteBuf input) {
        this.actions = input.readEnumSet(Action.class);
        this.entries = (List)ClientboundPlayerInfoUpdatePacket.entryCodec(this.actions).apply(ByteBufCodecs.list()).decode(input);
    }

    private void write(RegistryFriendlyByteBuf output) {
        output.writeEnumSet(this.actions, Action.class);
        ClientboundPlayerInfoUpdatePacket.entryCodec(this.actions).apply(ByteBufCodecs.list()).encode(output, this.entries);
    }

    @Override
    public PacketType<ClientboundPlayerInfoUpdatePacket> type() {
        return GamePacketTypes.CLIENTBOUND_PLAYER_INFO_UPDATE;
    }

    @Override
    public void handle(ClientGamePacketListener listener) {
        listener.handlePlayerInfoUpdate(this);
    }

    public EnumSet<Action> actions() {
        return this.actions;
    }

    public List<Entry> entries() {
        return this.entries;
    }

    public List<Entry> newEntries() {
        return this.actions.contains((Object)Action.ADD_PLAYER) ? this.entries : List.of();
    }

    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("actions", this.actions).add("entries", this.entries).toString();
    }

    public record Entry(UUID profileId, @Nullable GameProfile profile, boolean listed, int latency, GameType gameMode, @Nullable Component displayName, boolean showHat, int listOrder, @Nullable RemoteChatSession.Data chatSession) {
        private Entry(ServerPlayer player) {
            this(player.getUUID(), player.getGameProfile(), true, player.connection.latency(), player.gameMode(), player.getTabListDisplayName(), player.isModelPartShown(PlayerModelPart.HAT), player.getTabListOrder(), Optionull.map(player.getChatSession(), RemoteChatSession::asData));
        }
    }

    public static enum Action {
        ADD_PLAYER(0, (entry, input) -> {
            String name = (String)ByteBufCodecs.PLAYER_NAME.decode(input);
            PropertyMap properties = (PropertyMap)ByteBufCodecs.GAME_PROFILE_PROPERTIES.decode(input);
            entry.profile = new GameProfile(entry.profileId, name, properties);
        }, (output, entry) -> {
            GameProfile profile = Objects.requireNonNull(entry.profile());
            ByteBufCodecs.PLAYER_NAME.encode(output, profile.name());
            ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode(output, profile.properties());
        }),
        INITIALIZE_CHAT(1, (entry, input) -> {
            entry.chatSession = input.readNullable(RemoteChatSession.Data.STREAM_CODEC);
        }, (output, entry) -> output.writeNullable(entry.chatSession, RemoteChatSession.Data.STREAM_CODEC)),
        UPDATE_GAME_MODE(2, (entry, input) -> {
            entry.gameMode = (GameType)GameType.STREAM_CODEC.decode(input);
        }, (output, entry) -> GameType.STREAM_CODEC.encode(output, entry.gameMode)),
        UPDATE_LISTED(3, (entry, input) -> {
            entry.listed = input.readBoolean();
        }, (output, entry) -> output.writeBoolean(entry.listed())),
        UPDATE_LATENCY(4, (entry, input) -> {
            entry.latency = input.readVarInt();
        }, (output, entry) -> output.writeVarInt(entry.latency())),
        UPDATE_DISPLAY_NAME(5, (entry, input) -> {
            entry.displayName = FriendlyByteBuf.readNullable(input, ComponentSerialization.TRUSTED_STREAM_CODEC);
        }, (output, entry) -> FriendlyByteBuf.writeNullable(output, entry.displayName(), ComponentSerialization.TRUSTED_STREAM_CODEC)),
        UPDATE_LIST_ORDER(6, (entry, input) -> {
            entry.listOrder = input.readVarInt();
        }, (output, entry) -> output.writeVarInt(entry.listOrder)),
        UPDATE_HAT(7, (entry, input) -> {
            entry.showHat = input.readBoolean();
        }, (output, entry) -> output.writeBoolean(entry.showHat));

        private final int id;
        private final Reader reader;
        private final Writer writer;
        private static final IntFunction<Action> BY_ID;
        public static final StreamCodec<ByteBuf, Action> STREAM_CODEC;

        private Action(int id, Reader reader, Writer writer) {
            this.id = id;
            this.reader = reader;
            this.writer = writer;
        }

        static {
            BY_ID = ByIdMap.continuous(a -> a.id, Action.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, a -> a.id);
        }

        public static interface Reader {
            public void read(EntryBuilder var1, RegistryFriendlyByteBuf var2);
        }

        public static interface Writer {
            public void write(RegistryFriendlyByteBuf var1, Entry var2);
        }
    }

    private static class EntryBuilder {
        private final UUID profileId;
        private @Nullable GameProfile profile;
        private boolean listed;
        private int latency;
        private GameType gameMode = GameType.DEFAULT_MODE;
        private @Nullable Component displayName;
        private boolean showHat;
        private int listOrder;
        private @Nullable RemoteChatSession.Data chatSession;

        private EntryBuilder(UUID profileId) {
            this.profileId = profileId;
        }

        private Entry build() {
            return new Entry(this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.showHat, this.listOrder, this.chatSession);
        }
    }
}

