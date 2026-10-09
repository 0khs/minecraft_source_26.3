/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.Multimaps
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model.sprite;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MaterialBaker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final SpriteLoader.Preparations blockAtlas;
    private final SpriteLoader.Preparations itemAtlas;
    private final Material.Baked missingSprite;
    private final Material.Baked missingSpriteForceTranslucent;
    private final Multimap<String, Identifier> missingSprites = Multimaps.synchronizedMultimap((Multimap)HashMultimap.create());
    private final Multimap<String, String> missingReferences = Multimaps.synchronizedMultimap((Multimap)HashMultimap.create());
    private final Map<Material, @Nullable Material.Baked> bakedMaterials = new ConcurrentHashMap<Material, Material.Baked>();
    private final Function<Material, @Nullable Material.Baked> bakerFunction = this::bake;

    public MaterialBaker(SpriteLoader.Preparations blockAtlas, SpriteLoader.Preparations itemAtlas) {
        this.blockAtlas = blockAtlas;
        this.itemAtlas = itemAtlas;
        this.missingSprite = new Material.Baked(blockAtlas.missing(), false);
        this.missingSpriteForceTranslucent = new Material.Baked(blockAtlas.missing(), true);
    }

    private Material.Baked replacementForMissingMaterial(Material material) {
        return material.forceTranslucent() ? this.missingSpriteForceTranslucent : this.missingSprite;
    }

    public Material.Baked get(Material material, ModelDebugName name) {
        if (material.sprite().equals(MissingTextureAtlasSprite.getLocation())) {
            return this.replacementForMissingMaterial(material);
        }
        Material.Baked baked = this.bakedMaterials.computeIfAbsent(material, this.bakerFunction);
        if (baked == null) {
            this.missingSprites.put((Object)name.debugName(), (Object)material.sprite());
            return this.replacementForMissingMaterial(material);
        }
        return baked;
    }

    private @Nullable Material.Baked bake(Material material) {
        Material.Baked itemMaterial = MaterialBaker.bakeForAtlas(material, this.itemAtlas);
        if (itemMaterial != null) {
            return itemMaterial;
        }
        return MaterialBaker.bakeForAtlas(material, this.blockAtlas);
    }

    private static @Nullable Material.Baked bakeForAtlas(Material material, SpriteLoader.Preparations atlas) {
        TextureAtlasSprite sprite = atlas.getSprite(material.sprite());
        if (sprite != null) {
            return new Material.Baked(sprite, material.forceTranslucent());
        }
        return null;
    }

    public Material.Baked resolveSlot(TextureSlots slots, String id, ModelDebugName name) {
        Material resolvedMaterial = slots.getMaterial(id);
        return resolvedMaterial != null ? this.get(resolvedMaterial, name) : this.reportMissingReference(id, name);
    }

    public Material.Baked reportMissingReference(String reference, ModelDebugName responsibleModel) {
        this.missingReferences.put((Object)responsibleModel.debugName(), (Object)reference);
        return this.missingSprite;
    }

    public void logMissingTextures() {
        this.missingSprites.asMap().forEach((location, sprites) -> LOGGER.warn("Missing textures in model {}:\n{}", location, (Object)sprites.stream().sorted().map(sprite -> "    " + String.valueOf(sprite)).collect(Collectors.joining("\n"))));
        this.missingReferences.asMap().forEach((location, references) -> LOGGER.warn("Missing texture references in model {}:\n{}", location, (Object)references.stream().sorted().map(reference -> "    " + reference).collect(Collectors.joining("\n"))));
    }
}

