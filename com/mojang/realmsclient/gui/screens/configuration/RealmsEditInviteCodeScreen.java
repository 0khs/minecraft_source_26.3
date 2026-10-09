/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.screens.configuration;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.InviteCode;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.AbstractRealmsCodeScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.configuration.InviteCodeExpiration;
import com.mojang.realmsclient.gui.screens.configuration.RealmsInviteCodesScreen;
import com.mojang.realmsclient.util.RealmsUtil;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class RealmsEditInviteCodeScreen
extends AbstractRealmsCodeScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.configure.world.invite_codes.edit.title");
    private static final Component ACTIVE_LABEL = Component.translatable("mco.configure.world.invite_codes.edit.active");
    private static final Component EXPIRATION_DATE_LABEL = Component.translatable("mco.configure.world.invite_codes.edit.expiration_date");
    private static final Component COPY_LABEL = Component.translatable("mco.configure.world.invite_codes.copy");
    private static final Component DELETE_LABEL = Component.translatable("mco.configure.world.invite_codes.delete");
    private static final Component NEVER_EXPIRES_LABEL = Component.translatable("mco.configure.world.invite_codes.edit.expires", InviteCodeExpiration.NEVER.getLabel()).withColor(-6250336);
    private static final DateTimeFormatter DATE_FORMAT = Util.localizedDateFormatter(FormatStyle.SHORT);
    private static final int SPACING = 8;
    private static final int ACTIVE_BUTTON_WIDTH = 100;
    private static final int CONTENT_WIDTH = 308;
    private static final int CODE_COLUMN_WIDTH = 200;
    private static final int FOOTER_HEIGHT = 64;
    private final RealmsInviteCodesScreen parentScreen;
    private final long realmId;
    private final InviteCode inviteCode;
    private boolean active;
    private InviteCodeExpiration expiration;
    private boolean expirationChanged = false;
    private @Nullable CycleButton<Boolean> activeButton;
    private @Nullable ExpirationSlider expirationSlider;
    private @Nullable Button deleteButton;
    private @Nullable Button saveButton;
    private @Nullable Button backButton;

    public RealmsEditInviteCodeScreen(RealmsInviteCodesScreen parentScreen, long realmId, InviteCode inviteCode) {
        super(TITLE);
        this.parentScreen = parentScreen;
        this.realmId = realmId;
        this.inviteCode = inviteCode;
        this.active = inviteCode.enabled();
        this.expiration = inviteCode.expirationDate() != null ? InviteCodeExpiration.closestTo(inviteCode.expirationDate()) : InviteCodeExpiration.NEVER;
    }

    @Override
    public void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        LinearLayout content = this.layout.addToContents(LinearLayout.vertical().spacing(8));
        content.defaultCellSetting().alignHorizontallyLeft();
        GridLayout topRow = content.addChild(new GridLayout().spacing(8));
        LinearLayout codeColumn = topRow.addChild(LinearLayout.vertical().spacing(4), 0, 0, topRow.newCellSettings().alignVerticallyMiddle());
        codeColumn.defaultCellSetting().alignHorizontallyLeft();
        codeColumn.addChild(new StringWidget(Component.literal(this.inviteCode.code()), this.font).setMaxWidth(200));
        codeColumn.addChild(new StringWidget(this.getExpiresLabel(), this.font).setMaxWidth(200));
        topRow.addChild(SpacerElement.width(200), 1, 0);
        this.activeButton = CycleButton.onOffBuilder(this.active).create(0, 0, 100, 20, ACTIVE_LABEL, (cycleButton, value) -> this.setActive((boolean)value));
        topRow.addChild(this.activeButton, 0, 1, topRow.newCellSettings().alignVerticallyMiddle());
        this.expirationSlider = content.addChild(new ExpirationSlider(this, 308));
        GridLayout footer = this.layout.addToFooter(new GridLayout().spacing(8));
        footer.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper rowHelper = footer.createRowHelper(2);
        rowHelper.addChild(Button.builder(COPY_LABEL, button -> this.onCopy()).width(150).build());
        this.deleteButton = rowHelper.addChild(Button.builder(DELETE_LABEL, button -> this.onDelete()).width(150).build());
        this.saveButton = rowHelper.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onSave()).width(150).build());
        this.backButton = rowHelper.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(150).build());
        this.layout.setFooterHeight(64);
        this.setControlsActive(true);
        RealmsEditInviteCodeScreen realmsEditInviteCodeScreen = this;
        this.layout.visitWidgets(x$0 -> realmsEditInviteCodeScreen.addRenderableWidget(x$0));
        this.repositionElements();
    }

    private void setActive(boolean value) {
        this.active = value;
        if (this.expirationSlider != null) {
            this.expirationSlider.active = value;
        }
    }

    private void setControlsActive(boolean controlsActive) {
        if (this.activeButton != null) {
            this.activeButton.active = controlsActive;
        }
        if (this.expirationSlider != null) {
            boolean bl = this.expirationSlider.active = controlsActive && this.active;
        }
        if (this.deleteButton != null) {
            this.deleteButton.active = controlsActive;
        }
        if (this.saveButton != null) {
            this.saveButton.active = controlsActive;
        }
        if (this.backButton != null) {
            this.backButton.active = controlsActive;
        }
    }

    private Component getExpiresLabel() {
        Instant expirationDate = this.inviteCode.expirationDate();
        if (expirationDate == null) {
            return NEVER_EXPIRES_LABEL;
        }
        String formattedDate = ZonedDateTime.ofInstant(expirationDate, ZoneId.systemDefault()).format(DATE_FORMAT);
        return Component.translatable("mco.configure.world.invite_codes.edit.expires", formattedDate).withColor(-6250336);
    }

    private void onCopy() {
        this.minecraft.keyboardHandler.setClipboard(this.inviteCode.code());
    }

    private void onDelete() {
        this.setControlsActive(false);
        RealmsUtil.runAsync(client -> client.deleteInviteCode(this.inviteCode.code()), exception -> this.handleFailure((RealmsServiceException)exception, "Couldn't delete invite code")).thenRunAsync(() -> {
            this.minecraft.gui.setScreen(this.parentScreen);
            this.parentScreen.refreshInviteCodes();
        }, this.screenExecutor);
    }

    private void onSave() {
        if (this.active == this.inviteCode.enabled() && !this.expirationChanged) {
            this.minecraft.gui.setScreen(this.parentScreen);
            return;
        }
        this.setControlsActive(false);
        Instant expirationDate = InviteCodeExpiration.resolveExpirationDate(this.inviteCode.expirationDate(), this.expiration, this.expirationChanged, Instant.now());
        RealmsUtil.runAsync(client -> client.updateInviteCode(this.realmId, this.inviteCode.code(), this.active, expirationDate), exception -> this.handleFailure((RealmsServiceException)exception, "Couldn't update invite code")).thenRunAsync(() -> {
            this.minecraft.gui.setScreen(this.parentScreen);
            this.parentScreen.refreshInviteCodes();
        }, this.screenExecutor);
    }

    private void handleFailure(RealmsServiceException exception, String message) {
        LOGGER.error("{}", (Object)message, (Object)exception);
        this.screenExecutor.execute(() -> {
            this.setControlsActive(true);
            this.minecraft.gui.setScreen(new RealmsGenericErrorScreen(exception, (Screen)this));
        });
    }

    @Override
    public void onClose() {
        if (this.backButton == null || this.backButton.active) {
            this.minecraft.gui.setScreen(this.parentScreen);
        }
    }

    private class ExpirationSlider
    extends AbstractSliderButton {
        private final InviteCodeExpiration[] values;
        final /* synthetic */ RealmsEditInviteCodeScreen this$0;

        private ExpirationSlider(RealmsEditInviteCodeScreen realmsEditInviteCodeScreen, int width) {
            RealmsEditInviteCodeScreen realmsEditInviteCodeScreen2 = realmsEditInviteCodeScreen;
            Objects.requireNonNull(realmsEditInviteCodeScreen2);
            this.this$0 = realmsEditInviteCodeScreen2;
            super(0, 0, width, 20, CommonComponents.EMPTY, (double)realmsEditInviteCodeScreen.expiration.ordinal() / (double)(InviteCodeExpiration.values().length - 1));
            this.values = InviteCodeExpiration.values();
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(CommonComponents.optionNameValue(EXPIRATION_DATE_LABEL, this.this$0.expiration.getLabel()));
        }

        @Override
        protected void applyValue() {
            int index = Mth.clamp((int)Math.round(this.value * (double)(this.values.length - 1)), 0, this.values.length - 1);
            InviteCodeExpiration selectedExpiration = this.values[index];
            this.value = this.sliderValue(index);
            if (selectedExpiration != this.this$0.expiration) {
                this.this$0.expiration = selectedExpiration;
                this.this$0.expirationChanged = true;
            }
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (this.canChangeValue && (event.isLeft() || event.isRight())) {
                int direction = event.isLeft() ? -1 : 1;
                int index = Mth.clamp(this.this$0.expiration.ordinal() + direction, 0, this.values.length - 1);
                this.setValue(this.sliderValue(index));
                return true;
            }
            return super.keyPressed(event);
        }

        private double sliderValue(int index) {
            return (double)index / (double)(this.values.length - 1);
        }
    }
}

