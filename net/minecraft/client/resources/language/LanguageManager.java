/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.EmptyTranslationsException;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class LanguageManager
implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LanguageInfo DEFAULT_LANGUAGE = new LanguageInfo("US", "English", false);
    private final Minecraft minecraft;
    private Map<String, LanguageInfo> languages = ImmutableMap.of((Object)"en_us", (Object)DEFAULT_LANGUAGE);
    private String currentCode;
    private final Consumer<ClientLanguage> reloadCallback;

    public LanguageManager(Minecraft minecraft, String languageCode, Consumer<ClientLanguage> reloadCallback) {
        this.minecraft = minecraft;
        this.currentCode = languageCode;
        this.reloadCallback = reloadCallback;
    }

    private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> resourcePacks) {
        HashMap result = Maps.newHashMap();
        resourcePacks.forEach(resourcePack -> {
            try {
                LanguageMetadataSection languageMetadataSection = resourcePack.getMetadataSection(LanguageMetadataSection.TYPE);
                if (languageMetadataSection != null) {
                    languageMetadataSection.languages().forEach(result::putIfAbsent);
                }
            }
            catch (Exception e) {
                LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", (Object)resourcePack.packId(), (Object)e);
            }
        });
        return ImmutableMap.copyOf((Map)result);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        LanguageInfo currentLanguage;
        this.languages = LanguageManager.extractLanguages(resourceManager.listPacks());
        ArrayList<String> languageStack = new ArrayList<String>(2);
        boolean defaultRightToLeft = DEFAULT_LANGUAGE.bidirectional();
        languageStack.add("en_us");
        if (!this.currentCode.equals("en_us") && (currentLanguage = this.languages.get(this.currentCode)) != null) {
            languageStack.add(this.currentCode);
            defaultRightToLeft = currentLanguage.bidirectional();
        }
        try {
            ClientLanguage locale = ClientLanguage.loadFrom(resourceManager, languageStack, defaultRightToLeft);
            Language.inject(locale);
            this.reloadCallback.accept(locale);
        }
        catch (EmptyTranslationsException ex) {
            this.minecraft.gui.setScreen(new ErrorScreen(Component.translatable("options.language.load_translations_failed"), Component.translatable("options.language.empty_or_missing_translation", ex.getLanguageCode())));
            LOGGER.warn("Skipped unable to find any translations for {}", (Object)ex.getLanguageCode());
        }
        catch (Exception ex) {
            LOGGER.warn("Unable to load languages: {} ({})", languageStack, (Object)ex.toString());
        }
    }

    public void setSelected(String code) {
        this.currentCode = code;
    }

    public String getSelected() {
        return this.currentCode;
    }

    public SortedMap<String, LanguageInfo> getLanguages() {
        return new TreeMap<String, LanguageInfo>(this.languages);
    }

    public @Nullable LanguageInfo getLanguage(String code) {
        return this.languages.get(code);
    }
}

