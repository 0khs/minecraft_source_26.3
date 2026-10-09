/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.language;

class EmptyTranslationsException
extends RuntimeException {
    private final String languageCode;

    EmptyTranslationsException(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }
}

