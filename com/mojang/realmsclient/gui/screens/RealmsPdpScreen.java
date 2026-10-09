/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RealmsHeader;
import com.mojang.realmsclient.gui.task.DataFetcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.RealmsButton;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonLinks;
import org.jspecify.annotations.Nullable;

public class RealmsPdpScreen
extends RealmsScreen {
    private static final Component TITLE = Component.translatable("mco.selectServer.purchase");
    private static final Component TRY_FOR_FREE = Component.translatable("mco.pdp.tryForFree");
    private static final Component TRIAL_TITLE = Component.translatable("mco.pdp.trial.title").withStyle(ChatFormatting.UNDERLINE);
    private static final Component TRIAL_DESCRIPTION = Component.translatable("mco.pdp.trial.description");
    private static final Component FRIENDS_TITLE = Component.translatable("mco.pdp.friends.title").withStyle(ChatFormatting.BOLD);
    private static final Component FRIENDS_DESCRIPTION = Component.translatable("mco.pdp.friends.description");
    private static final Component MINIGAMES_TITLE = Component.translatable("mco.pdp.minigames.title").withStyle(ChatFormatting.BOLD);
    private static final Component MINIGAMES_DESCRIPTION = Component.translatable("mco.pdp.minigames.description");
    private static final Component PRIVATE_SERVER_TITLE = Component.translatable("mco.pdp.private.title").withStyle(ChatFormatting.BOLD);
    private static final Component PRIVATE_SERVER_BACKUPS = Component.translatable("mco.pdp.private.backups");
    private static final Component PRIVATE_SERVER_EASY_TO_MANAGE = Component.translatable("mco.pdp.private.easyToManage");
    private static final Component PRIVATE_SERVER_WORLD_SLOTS = Component.translatable("mco.pdp.private.worldSlots");
    private static final Component PRIVATE_SERVER_SECURE = Component.translatable("mco.pdp.private.secure");
    private static final Identifier FRIENDS_IMAGE = Identifier.withDefaultNamespace("textures/gui/realms/friends.png");
    private static final Identifier MINIGAMES_IMAGE = Identifier.withDefaultNamespace("textures/gui/realms/minigames.png");
    private static final Identifier PRIVATE_SERVER_IMAGE = Identifier.withDefaultNamespace("textures/gui/realms/private_server.png");
    private static final Identifier TRIAL_AVAILABLE_SPRITE = Identifier.withDefaultNamespace("icon/trial_available");
    private static final int HEADER_HEIGHT = 44;
    private static final int ROW_WIDTH = 290;
    private static final int IMAGE_WIDTH = 80;
    private static final int IMAGE_HEIGHT = 45;
    private static final int SPACING = 16;
    private static final int TEXT_WIDTH = 182;
    private static final int HEADER_BUTTON_SPACING = 4;
    private final Screen lastScreen;
    private boolean trialAvailable;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final RealmsHeader header;
    private final @Nullable Runnable openRealmsScreenWhenAvailable;
    private @Nullable PdpList pdpList;
    private @Nullable DataFetcher.Subscription dataSubscription;
    private boolean realmsAvailable;
    private boolean redirectedToRealms;

    public RealmsPdpScreen(Screen lastScreen, boolean trialAvailable, Runnable openJoinRealmScreen) {
        this(lastScreen, trialAvailable, openJoinRealmScreen, null);
    }

    public RealmsPdpScreen(Screen lastScreen, boolean trialAvailable, Runnable openJoinRealmScreen, @Nullable Runnable openRealmsScreenWhenAvailable) {
        super(TITLE);
        this.lastScreen = lastScreen;
        this.trialAvailable = trialAvailable;
        this.openRealmsScreenWhenAvailable = openRealmsScreenWhenAvailable;
        this.header = new RealmsHeader(this, openJoinRealmScreen);
    }

    @Override
    public void init() {
        this.layout.removeChildren();
        this.layout.setHeaderHeight(44);
        this.layout.addToHeader(this.header.createLayout(RealmsPdpScreen.realmsLogo(), 44, 4));
        this.dataSubscription = this.initDataFetcher(this.minecraft.realmsDataFetcher());
        this.pdpList = this.layout.addToContents(new PdpList(this, this.minecraft));
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        if (this.trialAvailable) {
            footer.addChild(new RealmsButton(0, 0, 150, 20, TRY_FOR_FREE, ConfirmLinkScreen.confirmLink(this, CommonLinks.START_REALMS_TRIAL)));
        } else {
            footer.addChild(Button.builder(TITLE, ConfirmLinkScreen.confirmLink(this, CommonLinks.BUY_REALMS)).build());
        }
        footer.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).build());
        RealmsPdpScreen realmsPdpScreen = this;
        this.layout.visitWidgets(x$0 -> realmsPdpScreen.addRenderableWidget(x$0));
        this.repositionElements();
    }

    @Override
    public void tick() {
        super.tick();
        boolean previousTrialAvailable = this.trialAvailable;
        if (this.dataSubscription != null) {
            this.dataSubscription.tick();
        }
        if (previousTrialAvailable != this.trialAvailable && this.minecraft.gui.screen() == this) {
            this.rebuildWidgets();
            return;
        }
        if (!this.redirectedToRealms && this.realmsAvailable && this.openRealmsScreenWhenAvailable != null && this.minecraft.gui.screen() == this) {
            this.redirectedToRealms = true;
            this.openRealmsScreenWhenAvailable.run();
        }
    }

    private DataFetcher.Subscription initDataFetcher(RealmsDataFetcher dataSource) {
        DataFetcher.Subscription result = dataSource.dataFetcher.createSubscription();
        if (this.openRealmsScreenWhenAvailable != null) {
            result.subscribe(dataSource.serverListUpdateTask, serverListData -> {
                this.realmsAvailable = !serverListData.serverList().isEmpty() || !serverListData.availableSnapshotServers().isEmpty();
            });
        }
        result.subscribe(dataSource.pendingInvitesTask, this.header::setPendingInvites);
        result.subscribe(dataSource.trialAvailabilityTask, trialAvailable -> {
            this.trialAvailable = trialAvailable;
        });
        result.subscribe(dataSource.newsTask, news -> {
            dataSource.newsManager.updateUnreadNews((RealmsNews)news);
            this.header.setNews(dataSource.newsManager.newsLink(), dataSource.newsManager.hasUnreadNews());
        });
        return result;
    }

    @Override
    protected void repositionElements() {
        if (this.pdpList != null) {
            this.pdpList.updateSize(this.width, this.layout);
        }
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.lastScreen);
    }

    private class PdpList
    extends ObjectSelectionList<AbstractPdpEntry> {
        final /* synthetic */ RealmsPdpScreen this$0;

        private PdpList(RealmsPdpScreen realmsPdpScreen, Minecraft minecraft) {
            RealmsPdpScreen realmsPdpScreen2 = realmsPdpScreen;
            Objects.requireNonNull(realmsPdpScreen2);
            this.this$0 = realmsPdpScreen2;
            super(minecraft, realmsPdpScreen.width, realmsPdpScreen.layout.getContentHeight(), realmsPdpScreen.layout.getHeaderHeight(), 45);
            if (realmsPdpScreen.trialAvailable) {
                this.addHeader(TRIAL_TITLE, new PdpHeaderEntry.Subtitle(TRIAL_DESCRIPTION, TRIAL_AVAILABLE_SPRITE));
            } else {
                this.addHeader(TRIAL_TITLE, null);
            }
            this.addFeature(FRIENDS_IMAGE, FRIENDS_TITLE, FRIENDS_DESCRIPTION);
            this.addFeature(MINIGAMES_IMAGE, MINIGAMES_TITLE, MINIGAMES_DESCRIPTION);
            this.addFeature(PRIVATE_SERVER_IMAGE, PRIVATE_SERVER_TITLE, PRIVATE_SERVER_BACKUPS, PRIVATE_SERVER_EASY_TO_MANAGE, PRIVATE_SERVER_WORLD_SLOTS, PRIVATE_SERVER_SECURE);
        }

        private void addHeader(Component title, @Nullable PdpHeaderEntry.Subtitle subTitle) {
            PdpHeaderEntry entry = new PdpHeaderEntry(this.this$0, title, subTitle);
            this.addEntry(entry, entry.entryHeight());
        }

        private void addFeature(Identifier image, Component title, Component ... descriptions) {
            PdpFeatureEntry entry = new PdpFeatureEntry(this.this$0, image, title, descriptions);
            this.addEntry(entry, entry.entryHeight());
        }

        @Override
        public int getRowWidth() {
            return 290;
        }
    }

    private class PdpFeatureEntry
    extends AbstractPdpEntry {
        private final Identifier image;
        private final List<Component> descriptions;
        final /* synthetic */ RealmsPdpScreen this$0;

        private PdpFeatureEntry(RealmsPdpScreen realmsPdpScreen, Identifier image, Component title, Component ... descriptions) {
            RealmsPdpScreen realmsPdpScreen2 = realmsPdpScreen;
            Objects.requireNonNull(realmsPdpScreen2);
            this.this$0 = realmsPdpScreen2;
            super(title);
            this.image = image;
            this.descriptions = List.of(descriptions);
        }

        private int entryHeight() {
            return Math.max(45, this.textHeight()) + 16;
        }

        private int textHeight() {
            int height = this.this$0.font.wordWrapHeight(this.title, 182) + 4;
            for (Component description : this.descriptions) {
                height += 2;
                height += this.this$0.font.wordWrapHeight(description, 182);
            }
            return height;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            int contentY = this.getContentY() + 4;
            graphics.blit(RenderPipelines.GUI_TEXTURED, this.image, this.getContentX() + 4, contentY, 0.0f, 0.0f, 80, 45, 80, 45);
            int textY = graphics.textWithWordWrap(this.this$0.font, this.title, this.titleX(), contentY, 182, -1) + 4;
            for (Component description : this.descriptions) {
                textY = graphics.textWithWordWrap(this.this$0.font, description, this.titleX(), textY, 182, -1) + 2;
            }
        }

        @Override
        public Component getNarration() {
            ArrayList<Component> lines = new ArrayList<Component>(this.descriptions.size() + 1);
            lines.add(this.title);
            lines.addAll(this.descriptions);
            return Component.translatable("narrator.select", CommonComponents.joinLines(lines));
        }

        private int titleX() {
            return this.getContentX() + 4 + 80 + 16;
        }
    }

    private class PdpHeaderEntry
    extends AbstractPdpEntry {
        private static final int DESCRIPTION_ICON_SIZE = 8;
        private final @Nullable Subtitle subtitle;
        final /* synthetic */ RealmsPdpScreen this$0;

        public PdpHeaderEntry(RealmsPdpScreen realmsPdpScreen, @Nullable Component title, Subtitle subtitle) {
            RealmsPdpScreen realmsPdpScreen2 = realmsPdpScreen;
            Objects.requireNonNull(realmsPdpScreen2);
            this.this$0 = realmsPdpScreen2;
            super(title);
            this.subtitle = subtitle;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            int textY = graphics.textWithWordWrap(this.this$0.font, this.title, this.titleX(), this.getContentY() + 4, 182, -1) + 4;
            if (this.subtitle != null) {
                int iconX = this.getContentXMiddle() - Math.min(182, this.this$0.font.width(this.subtitle.subTitle)) / 2 - 12;
                int iconY = textY + (((RealmsPdpScreen)this.this$0).font.lineHeight - 8) / 2;
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.subtitle.icon, iconX, iconY, 8, 8);
                int subtitleX = this.getContentXMiddle() - Math.min(182, this.this$0.font.width(this.subtitle.subTitle)) / 2;
                graphics.textWithWordWrap(this.this$0.font, this.subtitle.subTitle, subtitleX, textY, 182, -1);
            }
        }

        private int entryHeight() {
            return this.textHeight() + 16;
        }

        private int textHeight() {
            int height = this.this$0.font.wordWrapHeight(this.title, 182) + 4;
            if (this.subtitle != null) {
                height += 2;
                height += this.this$0.font.wordWrapHeight(this.subtitle.subTitle, 182);
            }
            return height;
        }

        @Override
        public Component getNarration() {
            ArrayList<Component> lines = new ArrayList<Component>();
            lines.add(this.title);
            if (this.subtitle != null) {
                lines.add(this.subtitle.subTitle);
            }
            return Component.translatable("narrator.select", CommonComponents.joinLines(lines));
        }

        private int titleX() {
            return this.getContentXMiddle() - Math.min(182, this.this$0.font.width(this.title)) / 2;
        }

        private record Subtitle(Component subTitle, Identifier icon) {
        }
    }

    private static abstract class AbstractPdpEntry
    extends ObjectSelectionList.Entry<AbstractPdpEntry> {
        protected final Component title;

        public AbstractPdpEntry(Component title) {
            this.title = title;
        }
    }
}

