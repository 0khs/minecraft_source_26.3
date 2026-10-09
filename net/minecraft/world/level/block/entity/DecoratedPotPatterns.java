/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;

public class DecoratedPotPatterns {
    public static final ResourceKey<DecoratedPotPattern> ANGLER = DecoratedPotPatterns.create("angler");
    public static final ResourceKey<DecoratedPotPattern> ARCHER = DecoratedPotPatterns.create("archer");
    public static final ResourceKey<DecoratedPotPattern> ARMS_UP = DecoratedPotPatterns.create("arms_up");
    public static final ResourceKey<DecoratedPotPattern> BLADE = DecoratedPotPatterns.create("blade");
    public static final ResourceKey<DecoratedPotPattern> BREWER = DecoratedPotPatterns.create("brewer");
    public static final ResourceKey<DecoratedPotPattern> BURN = DecoratedPotPatterns.create("burn");
    public static final ResourceKey<DecoratedPotPattern> DANGER = DecoratedPotPatterns.create("danger");
    public static final ResourceKey<DecoratedPotPattern> EXPLORER = DecoratedPotPatterns.create("explorer");
    public static final ResourceKey<DecoratedPotPattern> FLOW = DecoratedPotPatterns.create("flow");
    public static final ResourceKey<DecoratedPotPattern> FRIEND = DecoratedPotPatterns.create("friend");
    public static final ResourceKey<DecoratedPotPattern> GUSTER = DecoratedPotPatterns.create("guster");
    public static final ResourceKey<DecoratedPotPattern> HEART = DecoratedPotPatterns.create("heart");
    public static final ResourceKey<DecoratedPotPattern> HEARTBREAK = DecoratedPotPatterns.create("heartbreak");
    public static final ResourceKey<DecoratedPotPattern> HOWL = DecoratedPotPatterns.create("howl");
    public static final ResourceKey<DecoratedPotPattern> MINER = DecoratedPotPatterns.create("miner");
    public static final ResourceKey<DecoratedPotPattern> MOURNER = DecoratedPotPatterns.create("mourner");
    public static final ResourceKey<DecoratedPotPattern> PLENTY = DecoratedPotPatterns.create("plenty");
    public static final ResourceKey<DecoratedPotPattern> PRIZE = DecoratedPotPatterns.create("prize");
    public static final ResourceKey<DecoratedPotPattern> SCRAPE = DecoratedPotPatterns.create("scrape");
    public static final ResourceKey<DecoratedPotPattern> SHEAF = DecoratedPotPatterns.create("sheaf");
    public static final ResourceKey<DecoratedPotPattern> SHELTER = DecoratedPotPatterns.create("shelter");
    public static final ResourceKey<DecoratedPotPattern> SKULL = DecoratedPotPatterns.create("skull");
    public static final ResourceKey<DecoratedPotPattern> SNORT = DecoratedPotPatterns.create("snort");
    public static final Codec<Holder<DecoratedPotPattern>> CODEC = RegistryCodecs.holder(Registries.DECORATED_POT_PATTERN);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DecoratedPotPattern>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.DECORATED_POT_PATTERN);

    private static ResourceKey<DecoratedPotPattern> create(String id) {
        return ResourceKey.create(Registries.DECORATED_POT_PATTERN, Identifier.withDefaultNamespace(id));
    }

    public static void bootstrap(BootstrapContext<DecoratedPotPattern> registry) {
        DecoratedPotPatterns.registerWithDefaultAsset(registry, ANGLER);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, ARCHER);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, ARMS_UP);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, BLADE);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, BREWER);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, BURN);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, DANGER);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, EXPLORER);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, FLOW);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, FRIEND);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, GUSTER);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, HEART);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, HEARTBREAK);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, HOWL);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, MINER);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, MOURNER);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, PLENTY);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, PRIZE);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, SCRAPE);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, SHEAF);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, SHELTER);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, SKULL);
        DecoratedPotPatterns.registerWithDefaultAsset(registry, SNORT);
    }

    private static void registerWithDefaultAsset(BootstrapContext<DecoratedPotPattern> registry, ResourceKey<DecoratedPotPattern> key) {
        registry.register(key, new DecoratedPotPattern(key.identifier().withSuffix("_pottery_pattern")));
    }
}

