/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.Nullable;

public class SignText {
    public static final int LINES = 4;
    public static final Codec<SignText> CODEC = RecordCodecBuilder.create(i -> i.group((App)ComponentSerialization.CODEC.listOf(4, 4).fieldOf("messages").forGetter(o -> o.messages), (App)ComponentSerialization.CODEC.listOf(4, 4).lenientOptionalFieldOf("filtered_messages").forGetter(SignText::filteredMessagesForSerialization), (App)ExtraCodecs.optionalAlwaysPresentFieldOf(DyeColor.CODEC, "color", DyeColor.BLACK).forGetter(o -> o.color), (App)ExtraCodecs.optionalAlwaysPresentFieldOf(Codec.BOOL, "has_glowing_text", false).forGetter(o -> o.hasGlowingText)).apply((Applicative)i, SignText::load)).validate(SignText::validateLineCounts);
    public static final StreamCodec<RegistryFriendlyByteBuf, SignText> STREAM_CODEC = StreamCodec.composite(ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.fixedSizeList(4)), o -> o.messages, ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.fixedSizeList(4)).apply(ByteBufCodecs::optional), SignText::filteredMessagesForSerialization, DyeColor.STREAM_CODEC, o -> o.color, ByteBufCodecs.BOOL, o -> o.hasGlowingText, SignText::load);
    public static final TooltipProvider.Getter<SignText> FRONT_TEXT = SignText.createTooltip(Component.translatable("sign.front_text").withStyle(ChatFormatting.GRAY));
    public static final TooltipProvider.Getter<SignText> BACK_TEXT = SignText.createTooltip(Component.translatable("sign.back_text").withStyle(ChatFormatting.GRAY));
    private static final List<Component> EMPTY_MESSAGES = Collections.nCopies(4, CommonComponents.EMPTY);
    public static final SignText EMPTY = new SignText(EMPTY_MESSAGES, EMPTY_MESSAGES, DyeColor.BLACK, false);
    private final List<Component> messages;
    private final List<Component> filteredMessages;
    private final DyeColor color;
    private final boolean hasGlowingText;
    private FormattedCharSequence @Nullable [] renderMessages;
    private boolean renderMessagedFiltered;

    public SignText(List<Component> messages, List<Component> filteredMessages, DyeColor color, boolean hasGlowingText) {
        this.messages = messages;
        this.filteredMessages = filteredMessages;
        this.color = color;
        this.hasGlowingText = hasGlowingText;
    }

    private static SignText load(List<Component> messages, Optional<List<Component>> filteredMessages, DyeColor color, boolean hasGlowingText) {
        return new SignText(messages, filteredMessages.orElse(messages), color, hasGlowingText);
    }

    private static DataResult<SignText> validateLineCounts(SignText signText) {
        if (signText.filteredMessages.size() != signText.messages.size()) {
            return DataResult.error(() -> "Filtered and raw line counts are not equal");
        }
        return DataResult.success((Object)signText);
    }

    public boolean hasGlowingText() {
        return this.hasGlowingText;
    }

    public SignText withGlowingText(boolean hasGlowingText) {
        if (hasGlowingText == this.hasGlowingText) {
            return this;
        }
        return new SignText(this.messages, this.filteredMessages, this.color, hasGlowingText);
    }

    public DyeColor getColor() {
        return this.color;
    }

    public SignText withColor(DyeColor color) {
        if (color == this.getColor()) {
            return this;
        }
        return new SignText(this.messages, this.filteredMessages, color, this.hasGlowingText);
    }

    public boolean hasMessage(boolean shouldFilter) {
        return this.getMessages(shouldFilter).stream().anyMatch(component -> !component.getString().isEmpty());
    }

    public List<Component> getMessages(boolean shouldFilter) {
        return shouldFilter ? this.filteredMessages : this.messages;
    }

    public FormattedCharSequence[] getRenderMessages(boolean shouldFilter, Function<Component, FormattedCharSequence> prepare) {
        if (this.renderMessages == null || this.renderMessagedFiltered != shouldFilter) {
            List<Component> messages = this.getMessages(shouldFilter);
            this.renderMessagedFiltered = shouldFilter;
            this.renderMessages = new FormattedCharSequence[messages.size()];
            for (int i = 0; i < messages.size(); ++i) {
                this.renderMessages[i] = prepare.apply(messages.get(i));
            }
        }
        return this.renderMessages;
    }

    private Optional<List<Component>> filteredMessagesForSerialization() {
        return this.filteredMessages.equals(this.messages) ? Optional.empty() : Optional.of(this.filteredMessages);
    }

    public boolean hasAnyClickCommands(boolean shouldFilter) {
        for (Component message : this.getMessages(shouldFilter)) {
            Style style = message.getStyle();
            ClickEvent event = style.getClickEvent();
            if (event == null || event.action() != ClickEvent.Action.RUN_COMMAND) continue;
            return true;
        }
        return false;
    }

    public boolean hasEditableText(boolean shouldFilter) {
        return this.getMessages(shouldFilter).stream().allMatch(message -> message.equals(CommonComponents.EMPTY) || message.getContents() instanceof PlainTextContents);
    }

    public boolean equals(Object object) {
        if (object instanceof SignText) {
            SignText signText = (SignText)object;
            return this.hasGlowingText == signText.hasGlowingText && this.messages.equals(signText.messages) && this.filteredMessages.equals(signText.filteredMessages) && this.color == signText.color;
        }
        return false;
    }

    public int hashCode() {
        int result = 1;
        result = 31 * result + this.messages.hashCode();
        result = 31 * result + this.filteredMessages.hashCode();
        result = 31 * result + this.color.hashCode();
        result = 31 * result + Boolean.hashCode(this.hasGlowingText);
        return result;
    }

    public Mutable asMutable() {
        return new Mutable(this);
    }

    public static TooltipProvider.Getter<SignText> createTooltip(Component title) {
        return component -> component.equals(EMPTY) ? (tooltipContext, consumer, tooltipFlag, dataComponentGetter) -> {} : (tooltipContext, consumer, tooltipFlag, dataComponentGetter) -> {
            consumer.accept(title);
            for (Component message : component.messages) {
                consumer.accept(Component.literal("  ").append(message));
            }
        };
    }

    public static class Mutable {
        private final Component[] messages;
        private final Component[] filteredMessages;
        private DyeColor color;
        private boolean isTextGlowing;

        private Mutable(SignText original) {
            this.messages = (Component[])original.messages.toArray(Component[]::new);
            this.filteredMessages = (Component[])original.messages.toArray(Component[]::new);
            this.color = original.color;
            this.isTextGlowing = original.hasGlowingText;
        }

        public SignText asImmutable() {
            return new SignText(List.of(this.messages), List.of(this.filteredMessages), this.color, this.isTextGlowing);
        }

        public Mutable setLine(int index, Component raw, Component filtered) {
            this.messages[index] = raw;
            this.filteredMessages[index] = filtered;
            return this;
        }

        public Mutable setLine(int index, Component text) {
            return this.setLine(index, text, text);
        }

        public Mutable modifyLine(int index, UnaryOperator<Component> modifier) {
            this.messages[index] = (Component)modifier.apply(this.messages[index]);
            this.filteredMessages[index] = (Component)modifier.apply(this.filteredMessages[index]);
            return this;
        }

        public Mutable modifyLines(UnaryOperator<Component> modifier) {
            for (int i = 0; i < this.messages.length; ++i) {
                this.modifyLine(i, modifier);
            }
            return this;
        }

        public Mutable setColor(DyeColor color) {
            this.color = color;
            return this;
        }

        public Mutable setTextGlowing(boolean flag) {
            this.isTextGlowing = flag;
            return this;
        }
    }
}

