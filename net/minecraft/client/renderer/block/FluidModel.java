/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import org.jspecify.annotations.Nullable;

public record FluidModel(ChunkSectionLayer layer, Material.Baked stillMaterial, Material.Baked flowingMaterial, @Nullable Material.Baked overlayMaterial, @Nullable BlockTintSource tintSource) {

    public record Unbaked(Material stillMaterial, Material flowingMaterial, @Nullable Material overlayMaterial, @Nullable BlockTintSource tintSource) {
        public FluidModel bake(MaterialBaker materials, ModelDebugName modelName) {
            Material.Baked stillMaterial = this.getAndValidateMaterial(this.stillMaterial, materials, "still", modelName);
            Material.Baked flowingMaterial = this.getAndValidateMaterial(this.flowingMaterial, materials, "flowing", modelName);
            Material.Baked overlayMaterial = this.overlayMaterial != null ? this.getAndValidateMaterial(this.overlayMaterial, materials, "overlay", modelName) : null;
            Transparency transparency = Unbaked.getTransparency(stillMaterial).or(Unbaked.getTransparency(flowingMaterial));
            if (overlayMaterial != null) {
                transparency = transparency.or(Unbaked.getTransparency(overlayMaterial));
            }
            return new FluidModel(ChunkSectionLayer.byTransparency(transparency), stillMaterial, flowingMaterial, overlayMaterial, this.tintSource);
        }

        private Material.Baked getAndValidateMaterial(Material material, MaterialBaker materials, String textureName, ModelDebugName modelName) {
            Material.Baked baked = materials.get(material, modelName);
            if (!baked.sprite().atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
                return materials.reportMissingReference(textureName, modelName);
            }
            return baked;
        }

        private static Transparency getTransparency(Material.Baked material) {
            if (material.forceTranslucent()) {
                return Transparency.TRANSLUCENT;
            }
            return material.sprite().transparency();
        }
    }
}

