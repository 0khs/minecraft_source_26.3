/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.AbstractRealmsCodeScreen;
import com.mojang.realmsclient.util.RealmsUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class RealmsJoinRealmWithCodeScreen
extends AbstractRealmsCodeScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.selectServer.joinCode.title");
    private static final Component JOIN_CODE_LABEL = Component.translatable("mco.selectServer.joinCode").withColor(-6250336);
    private static final Component JOIN_CODE_HINT = Component.translatable("mco.selectServer.joinCode.hint");
    private static final Component JOIN_REALM_TEXT = Component.translatable("mco.selectServer.joinCode.select");
    private static final Component VALIDATING_CODE_TEXT = Component.translatable("mco.selectServer.joinCode.validating").withColor(-1);
    private static final Component INVALID_CODE_TEXT = Component.translatable("mco.selectServer.joinCode.invalid").withColor(-65536);
    private final Screen lastScreen;
    private final Runnable onJoin;
    private @Nullable EditBox joinCode;
    private @Nullable Button joinButton;
    private @Nullable Component message;

    public RealmsJoinRealmWithCodeScreen(Screen lastScreen, Runnable onJoin) {
        super(TITLE);
        this.lastScreen = lastScreen;
        this.onJoin = onJoin;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        LinearLayout content = this.layout.addToContents(LinearLayout.vertical().spacing(8));
        this.joinCode = new EditBox(this.font, 200, 20, JOIN_CODE_LABEL);
        this.joinCode.setHint(JOIN_CODE_HINT);
        content.addChild(CommonLayouts.labeledElement(this.font, this.joinCode, JOIN_CODE_LABEL));
        this.joinButton = content.addChild(Button.builder(JOIN_REALM_TEXT, button -> this.joinRealm()).width(200).build());
        this.joinButton.active = false;
        this.joinCode.setResponder(value -> {
            this.joinButton.active = !StringUtil.isBlank(value);
            this.message = null;
        });
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(200).build());
        RealmsJoinRealmWithCodeScreen realmsJoinRealmWithCodeScreen = this;
        this.layout.visitWidgets(x$0 -> realmsJoinRealmWithCodeScreen.addRenderableWidget(x$0));
        this.repositionElements();
    }

    @Override
    protected void setInitialFocus() {
        if (this.joinCode != null) {
            this.setInitialFocus(this.joinCode);
        }
    }

    private void joinRealm() {
        if (this.joinCode == null || this.joinButton == null) {
            return;
        }
        String code = this.joinCode.getValue().trim();
        if (StringUtil.isBlank(code)) {
            return;
        }
        this.joinCode.setEditable(false);
        this.joinButton.active = false;
        this.showMessage(VALIDATING_CODE_TEXT);
        RealmsUtil.runAsync(client -> client.redeemInviteCode(code), this::onJoinFailure).thenRunAsync(() -> {
            if (this.minecraft.gui.screen() == this) {
                this.onJoin.run();
            }
        }, this.screenExecutor);
    }

    private void onJoinFailure(RealmsServiceException exception) {
        LOGGER.error("Couldn't redeem invite code", (Throwable)exception);
        this.screenExecutor.execute(() -> {
            if (this.minecraft.gui.screen() == this && this.joinCode != null && this.joinButton != null) {
                this.joinCode.setEditable(true);
                this.joinButton.active = !StringUtil.isBlank(this.joinCode.getValue());
                this.showMessage(INVALID_CODE_TEXT);
            }
        });
    }

    private void showMessage(Component message) {
        this.message = message;
        this.minecraft.getNarrator().saySystemNow(message);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if (this.message != null && this.joinButton != null) {
            graphics.centeredText(this.font, this.message, this.width / 2, this.joinButton.getY() + this.joinButton.getHeight() + 8, -1);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.lastScreen);
    }
}

