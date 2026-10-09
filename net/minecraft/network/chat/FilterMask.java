/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  io.netty.buffer.ByteBuf
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.BitSet;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class FilterMask {
    public static final Codec<FilterMask> CODEC = StringRepresentable.fromEnum(Type::values).dispatch(FilterMask::type, Type::codec);
    public static final StreamCodec<ByteBuf, FilterMask> STREAM_CODEC = Type.STREAM_CODEC.dispatch(FilterMask::type, Type::streamCodec);
    public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), Type.FULLY_FILTERED);
    public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), Type.PASS_THROUGH);
    public static final Style FILTERED_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.filtered")));
    private static final char HASH = '#';
    private final BitSet mask;
    private final Type type;

    private FilterMask(BitSet mask, Type type) {
        this.mask = mask;
        this.type = type;
    }

    private FilterMask(BitSet mask) {
        this.mask = mask;
        this.type = Type.PARTIALLY_FILTERED;
    }

    public FilterMask(int length) {
        this(new BitSet(length), Type.PARTIALLY_FILTERED);
    }

    private Type type() {
        return this.type;
    }

    private BitSet mask() {
        return this.mask;
    }

    public void setFiltered(int index) {
        this.mask.set(index);
    }

    public @Nullable String apply(String text) {
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> null;
            case 0 -> text;
            case 2 -> {
                char[] chars = text.toCharArray();
                for (int i = 0; i < chars.length && i < this.mask.length(); ++i) {
                    if (!this.mask.get(i)) continue;
                    chars[i] = 35;
                }
                yield new String(chars);
            }
        };
    }

    public @Nullable Component applyWithFormatting(String text) {
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> null;
            case 0 -> Component.literal(text);
            case 2 -> {
                MutableComponent result = Component.empty();
                int previousIndex = 0;
                boolean filtered = this.mask.get(0);
                while (true) {
                    int nextIndex = filtered ? this.mask.nextClearBit(previousIndex) : this.mask.nextSetBit(previousIndex);
                    int v1 = nextIndex = nextIndex < 0 ? text.length() : nextIndex;
                    if (nextIndex == previousIndex) break;
                    if (filtered) {
                        result.append(Component.literal(StringUtils.repeat((char)'#', (int)(nextIndex - previousIndex))).withStyle(FILTERED_STYLE));
                    } else {
                        result.append(text.substring(previousIndex, nextIndex));
                    }
                    filtered = !filtered;
                    previousIndex = nextIndex;
                }
                yield result;
            }
        };
    }

    public boolean isEmpty() {
        return this.type == Type.PASS_THROUGH;
    }

    public boolean isFullyFiltered() {
        return this.type == Type.FULLY_FILTERED;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        FilterMask that = (FilterMask)o;
        return this.mask.equals(that.mask) && this.type == that.type;
    }

    public int hashCode() {
        int result = this.mask.hashCode();
        result = 31 * result + this.type.hashCode();
        return result;
    }

    private static enum Type implements StringRepresentable
    {
        PASS_THROUGH(0, "pass_through", () -> MapCodec.unit((Object)PASS_THROUGH), () -> StreamCodec.unit(PASS_THROUGH)),
        FULLY_FILTERED(1, "fully_filtered", () -> MapCodec.unit((Object)FULLY_FILTERED), () -> StreamCodec.unit(FULLY_FILTERED)),
        PARTIALLY_FILTERED(2, "partially_filtered", () -> ExtraCodecs.BIT_SET.xmap(FilterMask::new, FilterMask::mask).fieldOf("value"), () -> ByteBufCodecs.BIT_SET.map(FilterMask::new, FilterMask::mask));

        private static final IntFunction<Type> ID_MAP;
        public static final StreamCodec<ByteBuf, Type> STREAM_CODEC;
        private final int id;
        private final String serializedName;
        private final Supplier<MapCodec<FilterMask>> codec;
        private final Supplier<StreamCodec<ByteBuf, FilterMask>> streamCodec;

        private Type(int id, String serializedName, Supplier<MapCodec<FilterMask>> codec, Supplier<StreamCodec<ByteBuf, FilterMask>> streamCodec) {
            this.id = id;
            this.serializedName = serializedName;
            this.codec = Suppliers.memoize(codec::get);
            this.streamCodec = Suppliers.memoize(streamCodec::get);
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }

        private MapCodec<FilterMask> codec() {
            return this.codec.get();
        }

        private StreamCodec<ByteBuf, FilterMask> streamCodec() {
            return this.streamCodec.get();
        }

        static {
            ID_MAP = ByIdMap.continuous(t -> t.id, Type.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(ID_MAP, t -> t.id);
        }
    }
}

