/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.DiscardItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.FillPlayerHead;
import net.minecraft.world.level.storage.loot.functions.FilteredFunction;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.ModifyContainerContents;
import net.minecraft.world.level.storage.loot.functions.SequenceFunction;
import net.minecraft.world.level.storage.loot.functions.SetAttributesFunction;
import net.minecraft.world.level.storage.loot.functions.SetBannerPatternFunction;
import net.minecraft.world.level.storage.loot.functions.SetBookCoverFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.functions.SetContainerLootTable;
import net.minecraft.world.level.storage.loot.functions.SetCustomDataFunction;
import net.minecraft.world.level.storage.loot.functions.SetCustomModelDataFunction;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetFireworkExplosionFunction;
import net.minecraft.world.level.storage.loot.functions.SetFireworksFunction;
import net.minecraft.world.level.storage.loot.functions.SetInstrumentFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetLoreFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.functions.SetOminousBottleAmplifierFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.functions.SetRandomDyesFunction;
import net.minecraft.world.level.storage.loot.functions.SetRandomPotionFunction;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.functions.SetWritableBookPagesFunction;
import net.minecraft.world.level.storage.loot.functions.SetWrittenBookPagesFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.functions.ToggleTooltips;

public class LootItemFunctions {
    public static final Codec<LootItemFunction> TYPED_CODEC = BuiltInRegistries.LOOT_FUNCTION_TYPE.byNameCodec().dispatch(LootItemFunction::codec, c -> c);
    public static final Codec<LootItemFunction> DIRECT_CODEC = Codec.lazyInitialized(() -> Codec.either(TYPED_CODEC, SequenceFunction.INLINE_CODEC).xmap(typedOrList -> (LootItemFunction)typedOrList.map(f -> f, f -> f), function -> {
        SequenceFunction sequence;
        return function instanceof SequenceFunction && (sequence = (SequenceFunction)function).canUseInlineCodec() ? Either.right((Object)sequence) : Either.left((Object)function);
    }));
    public static final Codec<Holder<LootItemFunction>> CODEC = RegistryCodecs.holder(Registries.ITEM_MODIFIER, DIRECT_CODEC);
    public static final Codec<HolderSet<LootItemFunction>> LIST_CODEC = RegistryCodecs.holderSet(Registries.ITEM_MODIFIER, TYPED_CODEC);

    public static MapCodec<? extends LootItemFunction> bootstrap(Registry<MapCodec<? extends LootItemFunction>> registry) {
        Registry.register(registry, "set_count", SetItemCountFunction.MAP_CODEC);
        Registry.register(registry, "set_item", SetItemFunction.MAP_CODEC);
        Registry.register(registry, "enchant_with_levels", EnchantWithLevelsFunction.MAP_CODEC);
        Registry.register(registry, "enchant_randomly", EnchantRandomlyFunction.MAP_CODEC);
        Registry.register(registry, "set_enchantments", SetEnchantmentsFunction.MAP_CODEC);
        Registry.register(registry, "set_custom_data", SetCustomDataFunction.MAP_CODEC);
        Registry.register(registry, "set_components", SetComponentsFunction.MAP_CODEC);
        Registry.register(registry, "furnace_smelt", SmeltItemFunction.MAP_CODEC);
        Registry.register(registry, "enchanted_count_increase", EnchantedCountIncreaseFunction.MAP_CODEC);
        Registry.register(registry, "set_damage", SetItemDamageFunction.MAP_CODEC);
        Registry.register(registry, "set_attributes", SetAttributesFunction.MAP_CODEC);
        Registry.register(registry, "set_name", SetNameFunction.MAP_CODEC);
        Registry.register(registry, "exploration_map", ExplorationMapFunction.MAP_CODEC);
        Registry.register(registry, "set_stew_effect", SetStewEffectFunction.MAP_CODEC);
        Registry.register(registry, "copy_name", CopyNameFunction.MAP_CODEC);
        Registry.register(registry, "set_contents", SetContainerContents.MAP_CODEC);
        Registry.register(registry, "modify_contents", ModifyContainerContents.MAP_CODEC);
        Registry.register(registry, "filtered", FilteredFunction.MAP_CODEC);
        Registry.register(registry, "limit_count", LimitCount.MAP_CODEC);
        Registry.register(registry, "apply_bonus", ApplyBonusCount.MAP_CODEC);
        Registry.register(registry, "set_loot_table", SetContainerLootTable.MAP_CODEC);
        Registry.register(registry, "explosion_decay", ApplyExplosionDecay.MAP_CODEC);
        Registry.register(registry, "set_lore", SetLoreFunction.MAP_CODEC);
        Registry.register(registry, "fill_player_head", FillPlayerHead.MAP_CODEC);
        Registry.register(registry, "copy_custom_data", CopyCustomDataFunction.MAP_CODEC);
        Registry.register(registry, "copy_state", CopyBlockState.MAP_CODEC);
        Registry.register(registry, "set_banner_pattern", SetBannerPatternFunction.MAP_CODEC);
        Registry.register(registry, "set_potion", SetPotionFunction.MAP_CODEC);
        Registry.register(registry, "set_random_dyes", SetRandomDyesFunction.MAP_CODEC);
        Registry.register(registry, "set_random_potion", SetRandomPotionFunction.MAP_CODEC);
        Registry.register(registry, "set_instrument", SetInstrumentFunction.MAP_CODEC);
        Registry.register(registry, "sequence", SequenceFunction.MAP_CODEC);
        Registry.register(registry, "copy_components", CopyComponentsFunction.MAP_CODEC);
        Registry.register(registry, "set_fireworks", SetFireworksFunction.MAP_CODEC);
        Registry.register(registry, "set_firework_explosion", SetFireworkExplosionFunction.MAP_CODEC);
        Registry.register(registry, "set_book_cover", SetBookCoverFunction.MAP_CODEC);
        Registry.register(registry, "set_written_book_pages", SetWrittenBookPagesFunction.MAP_CODEC);
        Registry.register(registry, "set_writable_book_pages", SetWritableBookPagesFunction.MAP_CODEC);
        Registry.register(registry, "toggle_tooltips", ToggleTooltips.MAP_CODEC);
        Registry.register(registry, "set_ominous_bottle_amplifier", SetOminousBottleAmplifierFunction.MAP_CODEC);
        Registry.register(registry, "set_custom_model_data", SetCustomModelDataFunction.MAP_CODEC);
        return Registry.register(registry, "discard", DiscardItem.MAP_CODEC);
    }
}

