/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum AdvancementType implements StringRepresentable
{
    TASK(0, "task", ChatFormatting.GREEN),
    CHALLENGE(1, "challenge", ChatFormatting.DARK_PURPLE),
    GOAL(2, "goal", ChatFormatting.GREEN);

    public static final Codec<AdvancementType> CODEC;
    private static final IntFunction<AdvancementType> BY_ID;
    public static final StreamCodec<ByteBuf, AdvancementType> STREAM_CODEC;
    private final int id;
    private final String name;
    private final ChatFormatting chatColor;
    private final Component displayName;

    private AdvancementType(int id, String name, ChatFormatting chatColor) {
        this.id = id;
        this.name = name;
        this.chatColor = chatColor;
        this.displayName = Component.translatable("advancements.toast." + name);
    }

    public ChatFormatting getChatColor() {
        return this.chatColor;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public MutableComponent createAnnouncement(AdvancementHolder holder, ServerPlayer player) {
        return Component.translatable("chat.type.advancement." + this.name, player.getDisplayName(), Advancement.name(holder));
    }

    static {
        CODEC = StringRepresentable.fromEnum(AdvancementType::values);
        BY_ID = ByIdMap.continuous(h -> h.id, AdvancementType.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, h -> h.id);
    }
}

