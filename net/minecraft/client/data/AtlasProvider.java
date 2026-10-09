/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SpriteMapper;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.trim.TrimMaterials;

public class AtlasProvider
implements DataProvider {
    private static final Identifier TRIM_PALETTE_KEY = Identifier.withDefaultNamespace("trim_base");
    private final PackOutput.PathProvider pathProvider;

    public AtlasProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "atlases");
    }

    private static SpriteSource forMaterial(SpriteId sprite) {
        return new SingleFile(sprite.texture());
    }

    private static SpriteSource forMapper(SpriteMapper mapper) {
        return new DirectoryLister(mapper.prefix(), mapper.prefix() + "/");
    }

    private static List<SpriteSource> simpleMapper(SpriteMapper mapper) {
        return List.of(AtlasProvider.forMapper(mapper));
    }

    private static List<SpriteSource> noPrefixMapper(String directory) {
        return List.of(new DirectoryLister(directory, ""));
    }

    private static List<SpriteSource> blocksList() {
        return List.of(AtlasProvider.forMapper(Sheets.BLOCKS_MAPPER), AtlasProvider.forMapper(ConduitRenderer.MAPPER), AtlasProvider.forMaterial(BellRenderer.BELL_TEXTURE), AtlasProvider.forMaterial(EnchantTableRenderer.BOOK_TEXTURE));
    }

    private static List<SpriteSource> itemsList() {
        return List.of(AtlasProvider.forMapper(Sheets.ITEMS_MAPPER), new PalettedPermutations(List.of(ItemModelGenerators.TRIM_PREFIX_HELMET, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE, ItemModelGenerators.TRIM_PREFIX_LEGGINGS, ItemModelGenerators.TRIM_PREFIX_BOOTS), TRIM_PALETTE_KEY, Arrays.stream(TrimMaterials.Palette.values()).collect(Collectors.toMap(TrimMaterials.Palette::suffix, TrimMaterials.Palette::id))));
    }

    private static List<SpriteSource> bannerPatterns() {
        return List.of(AtlasProvider.forMapper(Sheets.BANNER_MAPPER));
    }

    private static List<SpriteSource> shieldPatterns() {
        return List.of(AtlasProvider.forMapper(Sheets.SHIELD_MAPPER));
    }

    private static List<SpriteSource> guiSprites() {
        return List.of(new DirectoryLister("gui/sprites", ""), new DirectoryLister("mob_effect", "mob_effect/"));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(this.storeAtlas(cache, AtlasIds.BANNER_PATTERNS, AtlasProvider.bannerPatterns()), this.storeAtlas(cache, AtlasIds.BLOCKS, AtlasProvider.blocksList()), this.storeAtlas(cache, AtlasIds.ITEMS, AtlasProvider.itemsList()), this.storeAtlas(cache, AtlasIds.CHESTS, AtlasProvider.simpleMapper(Sheets.CHEST_MAPPER)), this.storeAtlas(cache, AtlasIds.DECORATED_POT, AtlasProvider.simpleMapper(Sheets.DECORATED_POT_MAPPER)), this.storeAtlas(cache, AtlasIds.GUI, AtlasProvider.guiSprites()), this.storeAtlas(cache, AtlasIds.MAP_DECORATIONS, AtlasProvider.noPrefixMapper("map/decorations")), this.storeAtlas(cache, AtlasIds.PAINTINGS, AtlasProvider.noPrefixMapper("painting")), this.storeAtlas(cache, AtlasIds.PARTICLES, AtlasProvider.noPrefixMapper("particle")), this.storeAtlas(cache, AtlasIds.SHIELD_PATTERNS, AtlasProvider.shieldPatterns()), this.storeAtlas(cache, AtlasIds.SHULKER_BOXES, AtlasProvider.simpleMapper(Sheets.SHULKER_MAPPER)), this.storeAtlas(cache, AtlasIds.CELESTIALS, AtlasProvider.noPrefixMapper("environment/celestial")));
    }

    private CompletableFuture<?> storeAtlas(CachedOutput cache, Identifier atlasId, List<SpriteSource> contents) {
        return DataProvider.saveStable(cache, SpriteSources.FILE_CODEC, contents, this.pathProvider.json(atlasId));
    }

    @Override
    public String getName() {
        return "Atlas Definitions";
    }
}

