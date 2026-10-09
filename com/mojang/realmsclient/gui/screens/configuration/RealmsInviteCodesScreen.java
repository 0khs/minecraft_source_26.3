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
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.AbstractRealmsCodeScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.configuration.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.configuration.RealmsEditInviteCodeScreen;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class RealmsInviteCodesScreen
extends AbstractRealmsCodeScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Component TITLE = Component.translatable("mco.configure.world.buttons.invite_codes");
    private static final Component LOADING_TEXT = Component.translatable("mco.configure.world.invite_codes.loading");
    private static final Component COPY_LABEL = Component.translatable("mco.configure.world.invite_codes.copy");
    private static final Component DELETE_LABEL = Component.translatable("mco.configure.world.invite_codes.delete");
    private static final Component EDIT_LABEL = Component.translatable("mco.configure.world.invite_codes.edit");
    private static final Component CREATE_LABEL = Component.translatable("mco.configure.world.invite_codes.create");
    private static final int MAX_INVITE_CODES = 5;
    private static final int SPACING = 8;
    private static final int CONTENT_WIDTH = 308;
    private static final int BOTTOM_BUTTON_WIDTH = 150;
    private static final int TOP_BUTTON_WIDTH = 97;
    private static final int FOOTER_HEIGHT = 64;
    private static final int ENTRY_HEIGHT = 28;
    private final RealmsConfigureWorldScreen configureScreen;
    private final long realmId;
    private @Nullable Button copyButton;
    private @Nullable Button editButton;
    private @Nullable Button deleteButton;
    private @Nullable Button createButton;
    private @Nullable Button backButton;
    private @Nullable InviteCodeSelectionList inviteCodeList;
    private List<InviteCode> inviteCodes = List.of();
    private boolean fetchedInviteCodes = false;

    public RealmsInviteCodesScreen(RealmsConfigureWorldScreen configureScreen, RealmsServer serverData) {
        super(TITLE);
        this.configureScreen = configureScreen;
        this.realmId = serverData.id;
    }

    @Override
    public void init() {
        this.layout.removeChildren();
        LinearLayout header = this.layout.addToHeader(LinearLayout.vertical().spacing(8));
        header.defaultCellSetting().alignHorizontallyCenter();
        Component headerTitle = this.fetchedInviteCodes ? Component.translatable("mco.configure.world.invite_codes.title", this.inviteCodes.size(), 5) : this.getTitle();
        header.addChild(new StringWidget(headerTitle, this.font));
        int headerHeight = 33;
        if (this.fetchedInviteCodes && this.inviteCodes.isEmpty()) {
            MultiLineTextWidget subtitle = header.addChild(new MultiLineTextWidget(Component.translatable("mco.configure.world.invite_codes.subtitle", 5).withColor(-6250336), this.font).setCentered(true).setMaxWidth(308));
            headerHeight += subtitle.getHeight();
        }
        this.layout.setHeaderHeight(headerHeight);
        LinearLayout footer = this.layout.addToFooter(LinearLayout.vertical().spacing(8));
        LinearLayout actionRow = footer.addChild(LinearLayout.horizontal().spacing(8));
        this.copyButton = actionRow.addChild(Button.builder(COPY_LABEL, button -> this.onCopy()).width(97).build());
        this.deleteButton = actionRow.addChild(Button.builder(DELETE_LABEL, button -> this.onDelete()).width(97).build());
        this.editButton = actionRow.addChild(Button.builder(EDIT_LABEL, button -> this.onEdit()).width(97).build());
        LinearLayout manageRow = footer.addChild(LinearLayout.horizontal().spacing(8));
        this.createButton = manageRow.addChild(Button.builder(CREATE_LABEL, button -> this.onCreate()).width(150).build());
        this.backButton = manageRow.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(150).build());
        this.layout.setFooterHeight(64);
        if (this.fetchedInviteCodes) {
            this.inviteCodeList = this.layout.addToContents(new InviteCodeSelectionList(this, this.inviteCodes));
        } else {
            this.inviteCodeList = null;
            this.layout.addToContents(new LoadingDotsWidget(this.font, LOADING_TEXT));
        }
        this.updateButtonStates();
        RealmsInviteCodesScreen realmsInviteCodesScreen = this;
        this.layout.visitWidgets(x$0 -> realmsInviteCodesScreen.addRenderableWidget(x$0));
        this.repositionElements();
        if (!this.fetchedInviteCodes) {
            this.fetchInviteCodes();
        }
    }

    private void fetchInviteCodes() {
        this.setControlsActive(false);
        RealmsScreen errorReturnScreen = this.fetchedInviteCodes ? this : this.configureScreen;
        RealmsUtil.supplyAsync(client -> client.inviteCodes(this.realmId), exception -> this.handleFailure((RealmsServiceException)exception, "Couldn't get invite codes", errorReturnScreen)).thenAcceptAsync(result -> {
            this.inviteCodes = result.inviteCodes();
            this.fetchedInviteCodes = true;
            this.rebuildWidgets();
            this.scheduleNarration();
        }, this.screenExecutor);
    }

    private void updateButtonStates() {
        boolean hasSelection;
        boolean bl = hasSelection = this.inviteCodeList != null && this.inviteCodeList.getSelectedCode() != null;
        if (this.copyButton != null) {
            this.copyButton.active = hasSelection;
        }
        if (this.editButton != null) {
            this.editButton.active = hasSelection;
        }
        if (this.deleteButton != null) {
            this.deleteButton.active = hasSelection;
        }
        if (this.createButton != null) {
            this.createButton.active = this.fetchedInviteCodes && this.inviteCodes.size() < 5;
        }
    }

    private void setControlsActive(boolean controlsActive) {
        if (this.inviteCodeList != null) {
            this.inviteCodeList.active = controlsActive;
        }
        if (controlsActive) {
            this.updateButtonStates();
            return;
        }
        if (this.copyButton != null) {
            this.copyButton.active = false;
        }
        if (this.editButton != null) {
            this.editButton.active = false;
        }
        if (this.deleteButton != null) {
            this.deleteButton.active = false;
        }
        if (this.createButton != null) {
            this.createButton.active = false;
        }
    }

    private void handleFailure(RealmsServiceException exception, String message, Screen errorReturnScreen) {
        LOGGER.error("{}", (Object)message, (Object)exception);
        this.screenExecutor.execute(() -> {
            this.setControlsActive(true);
            if (this.backButton != null) {
                this.backButton.active = true;
            }
            this.minecraft.gui.setScreen(new RealmsGenericErrorScreen(exception, errorReturnScreen));
        });
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        if (this.inviteCodeList != null) {
            this.inviteCodeList.updateSize(this.width, this.layout);
        }
    }

    @Override
    protected boolean shouldRenderListBackgroundAndSeparators() {
        return this.inviteCodeList == null;
    }

    @Override
    public Component getNarrationMessage() {
        return this.fetchedInviteCodes ? super.getNarrationMessage() : CommonComponents.joinForNarration(super.getNarrationMessage(), LOADING_TEXT);
    }

    @Override
    public void onClose() {
        if (this.backButton == null || this.backButton.active) {
            this.minecraft.gui.setScreen(this.configureScreen);
        }
    }

    private void onCopy() {
        if (this.inviteCodeList == null) {
            return;
        }
        InviteCode selected = this.inviteCodeList.getSelectedCode();
        if (selected != null) {
            this.minecraft.keyboardHandler.setClipboard(selected.code());
        }
    }

    private void onDelete() {
        if (this.inviteCodeList == null) {
            return;
        }
        InviteCode selected = this.inviteCodeList.getSelectedCode();
        if (selected == null) {
            return;
        }
        this.setControlsActive(false);
        if (this.backButton != null) {
            this.backButton.active = false;
        }
        RealmsUtil.runAsync(client -> client.deleteInviteCode(selected.code()), exception -> this.handleFailure((RealmsServiceException)exception, "Couldn't delete invite code", this)).thenRunAsync(() -> {
            if (this.backButton != null) {
                this.backButton.active = true;
            }
            this.refreshInviteCodes();
        }, this.screenExecutor);
    }

    private void onCreate() {
        if (!this.fetchedInviteCodes || this.inviteCodes.size() >= 5) {
            return;
        }
        this.setControlsActive(false);
        if (this.backButton != null) {
            this.backButton.active = false;
        }
        RealmsUtil.runAsync(client -> client.createInviteCode(this.realmId, true, null), exception -> this.handleFailure((RealmsServiceException)exception, "Couldn't create invite code", this)).thenRunAsync(() -> {
            if (this.backButton != null) {
                this.backButton.active = true;
            }
            this.refreshInviteCodes();
        }, this.screenExecutor);
    }

    private void onEdit() {
        if (this.inviteCodeList == null) {
            return;
        }
        InviteCode selected = this.inviteCodeList.getSelectedCode();
        if (selected != null) {
            this.minecraft.gui.setScreen(new RealmsEditInviteCodeScreen(this, this.realmId, selected));
        }
    }

    void refreshInviteCodes() {
        if (this.inviteCodeList != null) {
            this.inviteCodeList.setSelected((InviteCodeSelectionList.Entry)null);
        }
        this.fetchInviteCodes();
    }

    private class InviteCodeSelectionList
    extends ObjectSelectionList<Entry> {
        final /* synthetic */ RealmsInviteCodesScreen this$0;

        public InviteCodeSelectionList(RealmsInviteCodesScreen realmsInviteCodesScreen, List<InviteCode> inviteCodes) {
            RealmsInviteCodesScreen realmsInviteCodesScreen2 = realmsInviteCodesScreen;
            Objects.requireNonNull(realmsInviteCodesScreen2);
            this.this$0 = realmsInviteCodesScreen2;
            super(Minecraft.getInstance(), realmsInviteCodesScreen.width, realmsInviteCodesScreen.layout.getContentHeight(), realmsInviteCodesScreen.layout.getHeaderHeight(), 28);
            inviteCodes.forEach(inviteCode -> this.addEntry(new Entry(this, (InviteCode)inviteCode)));
        }

        public @Nullable InviteCode getSelectedCode() {
            Entry selected = (Entry)this.getSelected();
            return selected == null ? null : selected.inviteCode;
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            this.this$0.updateButtonStates();
        }

        @Override
        public int getRowWidth() {
            return 308;
        }

        private class Entry
        extends ObjectSelectionList.Entry<Entry> {
            private final InviteCode inviteCode;
            final /* synthetic */ InviteCodeSelectionList this$1;

            public Entry(InviteCodeSelectionList inviteCodeSelectionList, InviteCode inviteCode) {
                InviteCodeSelectionList inviteCodeSelectionList2 = inviteCodeSelectionList;
                Objects.requireNonNull(inviteCodeSelectionList2);
                this.this$1 = inviteCodeSelectionList2;
                this.inviteCode = inviteCode;
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                this.this$1.setSelected(this);
                return true;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
                int textY = this.getContentYMiddle() - ((RealmsInviteCodesScreen)this.this$1.this$0).font.lineHeight / 2;
                graphics.text(this.this$1.this$0.font, this.inviteCode.code(), this.getContentX() + 8, textY, -1);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.inviteCode.code());
            }
        }
    }
}

