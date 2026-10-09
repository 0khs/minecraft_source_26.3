/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;

public record DisplayInfo(ItemStackTemplate icon, Component title, Component description, Optional<ClientAsset.ResourceTexture> background, AdvancementType type, boolean showToast, boolean announceToChat, boolean hidden) {
    public static final Codec<DisplayInfo> CODEC = RecordCodecBuilder.create(i -> i.group((App)ItemStackTemplate.CODEC.fieldOf("icon").forGetter(DisplayInfo::icon), (App)ComponentSerialization.CODEC.fieldOf("title").forGetter(DisplayInfo::title), (App)ComponentSerialization.CODEC.fieldOf("description").forGetter(DisplayInfo::description), (App)ClientAsset.ResourceTexture.CODEC.optionalFieldOf("background").forGetter(DisplayInfo::background), (App)AdvancementType.CODEC.optionalFieldOf("frame", (Object)AdvancementType.TASK).forGetter(DisplayInfo::type), (App)Codec.BOOL.optionalFieldOf("show_toast", (Object)true).forGetter(DisplayInfo::showToast), (App)Codec.BOOL.optionalFieldOf("announce_to_chat", (Object)true).forGetter(DisplayInfo::announceToChat), (App)Codec.BOOL.optionalFieldOf("hidden", (Object)false).forGetter(DisplayInfo::hidden)).apply((Applicative)i, DisplayInfo::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DisplayInfo> STREAM_CODEC = StreamCodec.ofMember(DisplayInfo::serializeToNetwork, DisplayInfo::fromNetwork);

    private void serializeToNetwork(RegistryFriendlyByteBuf output) {
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(output, this.title);
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(output, this.description);
        ItemStackTemplate.STREAM_CODEC.encode(output, this.icon);
        AdvancementType.STREAM_CODEC.encode(output, this.type);
        int flags = 0;
        if (this.background.isPresent()) {
            flags |= 1;
        }
        if (this.showToast) {
            flags |= 2;
        }
        if (this.hidden) {
            flags |= 4;
        }
        output.writeInt(flags);
        this.background.map(ClientAsset::id).ifPresent(output::writeIdentifier);
    }

    private static DisplayInfo fromNetwork(RegistryFriendlyByteBuf input) {
        Component title = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode(input);
        Component description = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode(input);
        ItemStackTemplate icon = (ItemStackTemplate)ItemStackTemplate.STREAM_CODEC.decode(input);
        AdvancementType frame = (AdvancementType)AdvancementType.STREAM_CODEC.decode(input);
        int flags = input.readInt();
        Optional<ClientAsset.ResourceTexture> background = (flags & 1) != 0 ? Optional.of(new ClientAsset.ResourceTexture(input.readIdentifier())) : Optional.empty();
        boolean showToast = (flags & 2) != 0;
        boolean hidden = (flags & 4) != 0;
        return new DisplayInfo(icon, title, description, background, frame, showToast, false, hidden);
    }
}

