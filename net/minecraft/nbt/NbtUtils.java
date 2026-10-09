/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Splitter
 *  com.google.common.collect.Comparators
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class NbtUtils {
    private static final Comparator<ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.comparingInt(list -> list.getIntOr(1, 0)).thenComparingInt(list -> list.getIntOr(0, 0)).thenComparingInt(list -> list.getIntOr(2, 0));
    private static final Comparator<ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.comparingDouble(list -> list.getDoubleOr(1, 0.0)).thenComparingDouble(list -> list.getDoubleOr(0, 0.0)).thenComparingDouble(list -> list.getDoubleOr(2, 0.0));
    private static final Codec<ResourceKey<Block>> BLOCK_NAME_CODEC = ResourceKey.codec(Registries.BLOCK);
    public static final String SNBT_DATA_TAG = "data";
    private static final char PROPERTIES_START = '{';
    private static final char PROPERTIES_END = '}';
    private static final String ELEMENT_SEPARATOR = ",";
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final Splitter COMMA_SPLITTER = Splitter.on((String)",");
    private static final Splitter COLON_SPLITTER = Splitter.on((char)':').limit(2);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NOT_FOUND = -1;
    private static final int BLOCK_STATE_ID_PROPERTIES_RENAME_VERSION = 5006;
    public static final String LEGACY_BLOCK_STATE_ID_TAG = "Name";
    public static final String LEGACY_BLOCKSTATE_PROPERTY_TAG = "Properties";

    private NbtUtils() {
    }

    @VisibleForTesting
    public static boolean compareNbt(@Nullable Tag expected, @Nullable Tag actual, boolean partialListMatches) {
        if (expected == actual) {
            return true;
        }
        if (expected == null) {
            return true;
        }
        if (actual == null) {
            return false;
        }
        if (!expected.getClass().equals(actual.getClass())) {
            return false;
        }
        if (expected instanceof CompoundTag) {
            CompoundTag expectedCompound = (CompoundTag)expected;
            CompoundTag actualCompound = (CompoundTag)actual;
            if (actualCompound.size() < expectedCompound.size()) {
                return false;
            }
            for (Map.Entry<String, Tag> entry : expectedCompound.entrySet()) {
                Tag tag = entry.getValue();
                if (NbtUtils.compareNbt(tag, actualCompound.get(entry.getKey()), partialListMatches)) continue;
                return false;
            }
            return true;
        }
        if (expected instanceof ListTag) {
            ListTag expectedList = (ListTag)expected;
            if (partialListMatches) {
                ListTag actualList = (ListTag)actual;
                if (expectedList.isEmpty()) {
                    return actualList.isEmpty();
                }
                if (actualList.size() < expectedList.size()) {
                    return false;
                }
                for (Tag tag : expectedList) {
                    boolean found = false;
                    for (Tag value : actualList) {
                        if (!NbtUtils.compareNbt(tag, value, partialListMatches)) continue;
                        found = true;
                        break;
                    }
                    if (found) continue;
                    return false;
                }
                return true;
            }
        }
        return expected.equals(actual);
    }

    public static BlockState readBlockState(HolderGetter<Block> blocks, CompoundTag tag) {
        Optional blockHolder = tag.read("id", BLOCK_NAME_CODEC).flatMap(blocks::get);
        if (blockHolder.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = (Block)((Holder)blockHolder.get()).value();
        BlockState result = block.defaultBlockState();
        Optional<CompoundTag> properties = tag.getCompound("properties");
        if (properties.isPresent()) {
            StateDefinition<Block, BlockState> definition = block.getStateDefinition();
            for (String key : properties.get().keySet()) {
                Property<?> property = definition.getProperty(key);
                if (property == null) continue;
                result = NbtUtils.setValueHelper(result, property, key, properties.get(), tag);
            }
        }
        return result;
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S result, Property<T> property, String key, CompoundTag properties, CompoundTag tag) {
        Optional value = properties.getString(key).flatMap(property::getValue);
        if (value.isPresent()) {
            return (S)((StateHolder)result.setValue(property, (Comparable)((Comparable)value.get())));
        }
        LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", new Object[]{key, properties.get(key), tag});
        return result;
    }

    private static void writeStateProperties(StateHolder<?, ?> state, CompoundTag tag) {
        if (!state.isSingletonState()) {
            CompoundTag properties = new CompoundTag();
            state.getValues().forEach(value -> properties.putString(value.property().getName(), value.valueName()));
            tag.put("properties", properties);
        }
    }

    public static CompoundTag writeBlockState(BlockState state) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        NbtUtils.writeStateProperties(state, tag);
        return tag;
    }

    public static Component toPrettyComponent(Tag tag) {
        return new TextComponentTagVisitor("").visit(tag);
    }

    public static String structureToSnbt(CompoundTag structure) {
        return new SnbtPrinterTagVisitor().visit(NbtUtils.packStructureTemplate(structure));
    }

    public static CompoundTag snbtToStructure(String snbt) throws CommandSyntaxException {
        return NbtUtils.unpackStructureTemplate(TagParser.parseCompoundFully(snbt));
    }

    @VisibleForTesting
    static CompoundTag packStructureTemplate(CompoundTag snbt) {
        Optional<ListTag> oldEntities;
        int templateVersion = NbtUtils.getDataVersion(snbt);
        Optional<ListTag> palettes = snbt.getList("palettes");
        ListTag palette = palettes.isPresent() ? palettes.get().getListOrEmpty(0) : snbt.getListOrEmpty("palette");
        ListTag deflatedPalette = palette.compoundStream().map(compound -> NbtUtils.packBlockState(compound, templateVersion)).map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new));
        snbt.put("palette", deflatedPalette);
        if (palettes.isPresent()) {
            ListTag newPalettes = new ListTag();
            palettes.get().stream().flatMap(tag -> tag.asList().stream()).forEach(oldPalette -> {
                CompoundTag newPalette = new CompoundTag();
                for (int i = 0; i < oldPalette.size(); ++i) {
                    newPalette.putString(deflatedPalette.getString(i).orElseThrow(), NbtUtils.packBlockState(oldPalette.getCompound(i).orElseThrow(), templateVersion));
                }
                newPalettes.add(newPalette);
            });
            snbt.put("palettes", newPalettes);
        }
        if ((oldEntities = snbt.getList("entities")).isPresent()) {
            ListTag newEntities = oldEntities.get().compoundStream().sorted(Comparator.comparing(tag -> tag.getList("pos"), Comparators.emptiesLast(YXZ_LISTTAG_DOUBLE_COMPARATOR))).collect(Collectors.toCollection(ListTag::new));
            snbt.put("entities", newEntities);
        }
        ListTag blockData = snbt.getList("blocks").stream().flatMap(ListTag::compoundStream).sorted(Comparator.comparing(tag -> tag.getList("pos"), Comparators.emptiesLast(YXZ_LISTTAG_INT_COMPARATOR))).peek(block -> block.putString("state", deflatedPalette.getString(block.getIntOr("state", 0)).orElseThrow())).collect(Collectors.toCollection(ListTag::new));
        snbt.put(SNBT_DATA_TAG, blockData);
        snbt.remove("blocks");
        return snbt;
    }

    @VisibleForTesting
    static CompoundTag unpackStructureTemplate(CompoundTag template) {
        int templateVersion = NbtUtils.getDataVersion(template);
        ListTag packedPalette = template.getListOrEmpty("palette");
        Map palette = (Map)packedPalette.stream().flatMap(tag -> tag.asString().stream()).collect(ImmutableMap.toImmutableMap(Function.identity(), compound -> NbtUtils.unpackBlockState(compound, templateVersion)));
        Optional<ListTag> oldPalettes = template.getList("palettes");
        if (oldPalettes.isPresent()) {
            template.put("palettes", oldPalettes.get().compoundStream().map(oldPalette -> palette.keySet().stream().map(key -> oldPalette.getString((String)key).orElseThrow()).map(compound -> NbtUtils.unpackBlockState(compound, templateVersion)).collect(Collectors.toCollection(ListTag::new))).collect(Collectors.toCollection(ListTag::new)));
            template.remove("palette");
        } else {
            template.put("palette", palette.values().stream().collect(Collectors.toCollection(ListTag::new)));
        }
        Optional<ListTag> maybeBlocks = template.getList(SNBT_DATA_TAG);
        if (maybeBlocks.isPresent()) {
            Object2IntOpenHashMap paletteToId = new Object2IntOpenHashMap();
            paletteToId.defaultReturnValue(-1);
            for (int i = 0; i < packedPalette.size(); ++i) {
                paletteToId.put((Object)packedPalette.getString(i).orElseThrow(), i);
            }
            ListTag blocks = maybeBlocks.get();
            for (int i = 0; i < blocks.size(); ++i) {
                CompoundTag block = blocks.getCompound(i).orElseThrow();
                String stateName = block.getString("state").orElseThrow();
                int stateId = paletteToId.getInt((Object)stateName);
                if (stateId == -1) {
                    throw new IllegalStateException("Entry " + stateName + " missing from palette");
                }
                block.putInt("state", stateId);
            }
            template.put("blocks", blocks);
            template.remove(SNBT_DATA_TAG);
        }
        return template;
    }

    @VisibleForTesting
    static String packBlockState(CompoundTag compound, int version) {
        String propertiesTag;
        String idTag;
        if (version >= 5006) {
            idTag = "id";
            propertiesTag = "properties";
        } else {
            idTag = LEGACY_BLOCK_STATE_ID_TAG;
            propertiesTag = LEGACY_BLOCKSTATE_PROPERTY_TAG;
        }
        StringBuilder builder = new StringBuilder(compound.getString(idTag).orElseThrow());
        compound.getCompound(propertiesTag).ifPresent(properties -> {
            String keyValues = properties.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> (String)entry.getKey() + ":" + ((Tag)entry.getValue()).asString().orElseThrow()).collect(Collectors.joining(ELEMENT_SEPARATOR));
            builder.append('{').append(keyValues).append('}');
        });
        return builder.toString();
    }

    @VisibleForTesting
    static CompoundTag unpackBlockState(String compound, int version) {
        String name;
        CompoundTag tag = new CompoundTag();
        int openIndex = compound.indexOf(123);
        CompoundTag properties = new CompoundTag();
        if (openIndex >= 0) {
            name = compound.substring(0, openIndex);
            if (openIndex + 2 <= compound.length()) {
                String values = compound.substring(openIndex + 1, compound.indexOf(125, openIndex));
                COMMA_SPLITTER.split((CharSequence)values).forEach(keyValue -> {
                    List parts = COLON_SPLITTER.splitToList((CharSequence)keyValue);
                    if (parts.size() == 2) {
                        properties.putString((String)parts.get(0), (String)parts.get(1));
                    } else {
                        LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", (Object)compound);
                    }
                });
            }
        } else {
            name = compound;
        }
        if (version >= 5006) {
            tag.putString("id", name);
            if (!properties.isEmpty()) {
                tag.put("properties", properties);
            }
        } else {
            tag.putString(LEGACY_BLOCK_STATE_ID_TAG, name);
            if (!properties.isEmpty()) {
                tag.put(LEGACY_BLOCKSTATE_PROPERTY_TAG, properties);
            }
        }
        return tag;
    }

    public static CompoundTag addCurrentDataVersion(CompoundTag tag) {
        int version = SharedConstants.getCurrentVersion().dataVersion().version();
        return NbtUtils.addDataVersion(tag, version);
    }

    public static CompoundTag addDataVersion(CompoundTag tag, int version) {
        tag.putInt("DataVersion", version);
        return tag;
    }

    public static <T> Dynamic<T> addDataVersion(Dynamic<T> tag, int version) {
        return tag.set("DataVersion", tag.createInt(version));
    }

    public static void addCurrentDataVersion(ValueOutput output) {
        int version = SharedConstants.getCurrentVersion().dataVersion().version();
        NbtUtils.addDataVersion(output, version);
    }

    public static void addDataVersion(ValueOutput output, int version) {
        output.putInt("DataVersion", version);
    }

    public static int getDataVersion(CompoundTag tag) {
        return NbtUtils.getDataVersion(tag, -1);
    }

    public static int getDataVersion(CompoundTag tag, int _default) {
        return tag.getIntOr("DataVersion", _default);
    }

    public static int getDataVersion(Dynamic<?> dynamic) {
        return NbtUtils.getDataVersion(dynamic, -1);
    }

    public static int getDataVersion(Dynamic<?> dynamic, int _default) {
        return dynamic.get("DataVersion").asInt(_default);
    }
}

