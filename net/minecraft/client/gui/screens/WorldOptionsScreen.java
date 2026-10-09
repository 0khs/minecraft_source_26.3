/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.RestrictionsScreen;
import net.minecraft.client.gui.screens.options.HasDifficultyReaction;
import net.minecraft.client.gui.screens.options.HasGamemasterPermissionReaction;
import net.minecraft.client.gui.screens.options.InWorldGameRulesScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class WorldOptionsScreen
extends Screen
implements HasGamemasterPermissionReaction,
HasDifficultyReaction {
    private static final Component TITLE = Component.translatable("options.worldOptions.title");
    private static final Component GENERAL_TITLE = Component.translatable("options.worldOptions.general.title").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
    private static final Component GAME_RULES = Component.translatable("editGamerule.inGame.button");
    private static final Tooltip GAMERULES_DISABLED_TOOLTIP = Tooltip.create(Component.translatable("editGamerule.inGame.disabled.tooltip"));
    private static final Tooltip GAMERULES_DISABLED_HARDCORE_TOOLTIP = Tooltip.create(Component.translatable("editGamerule.inGame.disabled.hardcore.tooltip"));
    private static final Component DEFAULT_GAME_MODE = Component.translatable("options.worldOptions.game_mode");
    private static final Tooltip DEFAULT_GAME_MODE_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.game_mode.tooltip"));
    private static final Component PERSONAL_GAME_MODE = Component.translatable("options.worldOptions.personal_game_mode");
    private static final Tooltip PERSONAL_GAME_MODE_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.personal_game_mode.tooltip"));
    private static final Tooltip GAME_MODE_DISABLED_OPERATOR_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.game_mode.disabled.operator.tooltip"));
    private static final Tooltip GAME_MODE_DISABLED_HARDCORE_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.game_mode.disabled.tooltip"));
    private static final Component ALLOW_COMMANDS = Component.translatable("selectWorld.allowCommands");
    private static final Tooltip ALLOW_COMMANDS_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.allow_commands.tooltip"));
    private static final Tooltip ALLOW_COMMANDS_DISABLED_HARDCORE_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.allow_commands.disabled.tooltip"));
    private static final Tooltip ALLOW_COMMANDS_DISABLED_DEMO_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.allow_commands.disabled.demo.tooltip"));
    private static final Component RESTRICTIONS = Component.translatable("restrictions_screen.button");
    private static final Component MULTIPLAYER_TITLE = Component.translatable("options.worldOptions.multiplayer.title").withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
    private static final Component GUEST_COMMAND_ACCESS = Component.translatable("options.worldOptions.guest.command_access");
    private static final Tooltip GUEST_COMMAND_ACCESS_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.guest.command_access.tooltip"));
    private static final Tooltip GUEST_COMMAND_ACCESS_DISABLED_MULTIPLAYER_SCOPE_OFF = Tooltip.create(Component.translatable("options.worldOptions.guest.command_access.disabled.scope.tooltip"));
    private static final Tooltip GUEST_COMMAND_ACCESS_DISABLED_COMMANDS_OFF = Tooltip.create(Component.translatable("options.worldOptions.guest.command_access.disabled.commands.tooltip"));
    private static final Component FORCE_GAME_MODE = Component.translatable("options.worldOptions.guest.force_game_mode");
    private static final Tooltip FORCE_GAME_MODE_ON_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.guest.force_game_mode.on.tooltip"));
    private static final Tooltip FORCE_GAME_MODE_OFF_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.guest.force_game_mode.off.tooltip"));
    private static final Tooltip FORCE_GAME_MODE_OFF_MULTIPLAYER_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.guest.force_game_mode.off.scope.tooltip"));
    private static final Tooltip FORCE_GAME_MODE_OFF_HARDCORE_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.guest.force_game_mode.off.hardcore.tooltip"));
    private static final Tooltip FORCE_GAME_MODE_OFF_COMMANDS_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.guest.force_game_mode.off.commands.tooltip"));
    private static final int PORT_LOWER_BOUND = 1024;
    private static final int PORT_HIGHER_BOUND = 65535;
    private static final Component PORT_INFO_TEXT = Component.translatable("lanServer.port");
    private static final Component PORT_UNAVAILABLE = Component.translatable("lanServer.port.unavailable", 1024, 65535);
    private static final Component INVALID_PORT = Component.translatable("lanServer.port.invalid", 1024, 65535);
    private static final Component DIFFICULTY_LOCK_TITLE = Component.translatable("difficulty.lock.title");
    private static final Component APPLY_CHANGES = Component.translatable("menu.multiplayerOptions.applyChanges");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private @Nullable ScrollableLayout scrollArea;
    private final DifficultyButtons difficultyButtons;
    private @Nullable Button gameRulesButton;
    private @Nullable CycleButton<GameType> defaultGameModeButton;
    private @Nullable CycleButton<GameType> personalGameModeButton;
    private @Nullable Difficulty initialDifficulty;
    private @Nullable Difficulty wantedDifficulty;
    private @Nullable Boolean initialDifficultyLocked;
    private @Nullable Boolean wantedDifficultyLocked;
    private @Nullable GameType initialDefaultGameMode;
    private @Nullable GameType wantedDefaultGameMode;
    private @Nullable GameType initialPersonalGameMode;
    private @Nullable GameType wantedPersonalGameMode;
    private @Nullable Boolean initialAllowCommands;
    private @Nullable Boolean wantedAllowCommands;
    private  @Nullable MinecraftServer.MultiplayerScope initialMultiplayerScope;
    private  @Nullable MinecraftServer.MultiplayerScope wantedMultiplayerScope;
    private int port = HttpUtil.getAvailablePort();
    private boolean portValid = true;
    private @Nullable EditBox portEdit;
    private int initialPort;
    private @Nullable Boolean initialGuestCommandAccess;
    private @Nullable Boolean wantedGuestCommandAccess;
    private @Nullable CycleButton<Boolean> guestCommandAccessButton;
    private @Nullable Boolean initialForceGameMode;
    private @Nullable Boolean wantedForceGameMode;
    private @Nullable CycleButton<Boolean> forceGameModeButton;
    private final Screen lastScreen;
    private final Level level;
    private @Nullable Button applyChanges;

    public WorldOptionsScreen(Screen lastScreen, Level level) {
        super(TITLE);
        this.lastScreen = lastScreen;
        this.level = level;
        this.difficultyButtons = DifficultyButtons.create(this.minecraft, level, this);
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        IntegratedServer singleplayerServer = this.minecraft.getSingleplayerServer();
        LinearLayout content = LinearLayout.vertical().spacing(8);
        content.defaultCellSetting().padding(8).alignHorizontallyCenter().alignVerticallyTop();
        this.scrollArea = this.layout.addToContents(new ScrollableLayout(this.minecraft, content, this.layout.getContentHeight()));
        this.generalOptions(content, singleplayerServer);
        if (singleplayerServer != null) {
            this.multiplayerOptions(content, singleplayerServer);
        }
        this.applyChanges = Button.builder(APPLY_CHANGES, button -> {
            if (this.wantedDifficultyLocked != this.initialDifficultyLocked) {
                Component difficultyDisplayName = this.wantedDifficulty != null ? this.wantedDifficulty.getDisplayName() : this.level.getDifficulty().getDisplayName();
                this.minecraft.gui.setScreen(new PopupScreen.Builder(this, DIFFICULTY_LOCK_TITLE).addMessage(Component.translatable("difficulty.lock.question", difficultyDisplayName)).addButton(CommonComponents.GUI_YES, popupScreen -> {
                    this.applyChanges(singleplayerServer);
                    this.minecraft.gui.setScreen(this.lastScreen);
                }).addButton(CommonComponents.GUI_NO, popupScreen -> this.minecraft.gui.setScreen(this)).build());
            } else {
                this.applyChanges(singleplayerServer);
                this.minecraft.gui.setScreen(this.lastScreen);
            }
        }).build();
        this.applyChanges.active = false;
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(this.applyChanges);
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    private FocusableTextWidget createCategoryHeader(Component title) {
        return FocusableTextWidget.builder(title, this.font).alwaysShowBorder(false).backgroundFill(FocusableTextWidget.BackgroundFill.ON_FOCUS).build();
    }

    private void generalOptions(LinearLayout content, @Nullable IntegratedServer singleplayerServer) {
        boolean host;
        GridLayout grid = content.addChild(new GridLayout());
        GridLayout.RowHelper rowHelper = grid.columnSpacing(8).rowSpacing(4).createRowHelper(2);
        rowHelper.defaultCellSetting().alignHorizontallyCenter();
        rowHelper.addChild(this.createCategoryHeader(GENERAL_TITLE), 2);
        boolean bl = host = singleplayerServer != null && this.minecraft.player != null && singleplayerServer.isSingleplayerOwner(this.minecraft.player.nameAndId());
        if (host) {
            this.initialDefaultGameMode = this.wantedDefaultGameMode = singleplayerServer.getWorldData().getGameType();
            this.defaultGameModeButton = rowHelper.addChild(this.createGameModeButton(singleplayerServer, DEFAULT_GAME_MODE, DEFAULT_GAME_MODE_TOOLTIP, this.wantedDefaultGameMode, value -> {
                this.wantedDefaultGameMode = value;
            }), 2);
            this.initialPersonalGameMode = this.wantedPersonalGameMode = singleplayerServer.getPersonalGameMode();
            if (this.wantedPersonalGameMode != null) {
                this.personalGameModeButton = rowHelper.addChild(this.createGameModeButton(singleplayerServer, PERSONAL_GAME_MODE, PERSONAL_GAME_MODE_TOOLTIP, this.wantedPersonalGameMode, value -> {
                    this.wantedPersonalGameMode = value;
                }), 2);
            }
            this.initialAllowCommands = this.wantedAllowCommands = Boolean.valueOf(singleplayerServer.getWorldData().isAllowCommands());
            rowHelper.addChild(this.createAllowCommandsButton(singleplayerServer));
        }
        rowHelper.addChild(this.difficultyButtons.layout());
        this.gameRulesButton = rowHelper.addChild(this.createGameRulesButton(singleplayerServer));
        rowHelper.addChild(this.createRestrictionsButton());
    }

    private Button createGameRulesButton(@Nullable IntegratedServer singleplayerServer) {
        Button gameRulesButton = Button.builder(GAME_RULES, button -> {
            if (this.minecraft.player != null) {
                this.minecraft.gui.setScreen(new InWorldGameRulesScreen(this.minecraft.player.connection, optional -> this.minecraft.gui.setScreen(this), this));
            }
        }).build();
        this.updateButton(gameRulesButton, singleplayerServer, null, GAMERULES_DISABLED_TOOLTIP, GAMERULES_DISABLED_HARDCORE_TOOLTIP);
        return gameRulesButton;
    }

    private CycleButton<GameType> createGameModeButton(IntegratedServer singleplayerServer, Component buttonName, Tooltip tooltip, GameType defaultValue, Consumer<GameType> gameTypeToChange) {
        CycleButton<GameType> gameModeButton = CycleButton.builder(GameType::getShortDisplayName, defaultValue).withValues((GameType[])GameType.values()).withTooltip(gameType -> tooltip).create(0, 0, 308, 20, buttonName, (cycleButton, value) -> {
            gameTypeToChange.accept((GameType)value);
            this.updateApplyChangesActiveState();
        });
        this.updateButton(gameModeButton, singleplayerServer, tooltip, GAME_MODE_DISABLED_OPERATOR_TOOLTIP, GAME_MODE_DISABLED_HARDCORE_TOOLTIP);
        return gameModeButton;
    }

    private CycleButton<Boolean> createAllowCommandsButton(IntegratedServer singleplayerServer) {
        CycleButton<Boolean> allowCommandsButton = CycleButton.onOffBuilder(singleplayerServer.getWorldData().isAllowCommands()).withTooltip(bl -> ALLOW_COMMANDS_TOOLTIP).create(ALLOW_COMMANDS, (cycleButton, allowCommands) -> {
            this.wantedAllowCommands = allowCommands;
            this.updateGuestCommandAccessButton(singleplayerServer);
            this.updatePermissionDependentButtons(singleplayerServer, (boolean)allowCommands, false);
            this.updateApplyChangesActiveState();
        });
        if (singleplayerServer.isDemo()) {
            allowCommandsButton.active = false;
            allowCommandsButton.setTooltip(ALLOW_COMMANDS_DISABLED_DEMO_TOOLTIP);
        } else if (singleplayerServer.isHardcore() && (this.minecraft.player == null || !this.minecraft.player.permissions().hasPermission(Permissions.COMMANDS_OWNER))) {
            allowCommandsButton.active = false;
            allowCommandsButton.setTooltip(ALLOW_COMMANDS_DISABLED_HARDCORE_TOOLTIP);
        } else {
            allowCommandsButton.setTooltip(ALLOW_COMMANDS_TOOLTIP);
        }
        return allowCommandsButton;
    }

    private void updateButton(@Nullable AbstractWidget widget, @Nullable IntegratedServer singleplayerServer, @Nullable Tooltip tooltip, Tooltip disabledTooltip, Tooltip hardcoreTooltip) {
        if (widget != null) {
            boolean hardcore;
            boolean bl = hardcore = singleplayerServer != null && singleplayerServer.isHardcore();
            boolean shouldButtonsBeActive = this.minecraft.player != null && (this.wantedAllowCommands == null ? this.minecraft.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) : this.wantedAllowCommands != false);
            boolean bl2 = widget.active = !hardcore && shouldButtonsBeActive;
            widget.setTooltip(hardcore ? hardcoreTooltip : (shouldButtonsBeActive ? tooltip : disabledTooltip));
        }
    }

    private Button createRestrictionsButton() {
        return Button.builder(RESTRICTIONS, button -> {
            if (this.minecraft.player != null) {
                this.minecraft.gui.setScreen(new RestrictionsScreen(this, this.minecraft.player.chatAbilities()));
            }
        }).build();
    }

    private void multiplayerOptions(LinearLayout content, IntegratedServer singleplayerServer) {
        GridLayout grid = content.addChild(new GridLayout());
        grid.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper helper = grid.columnSpacing(8).rowSpacing(4).createRowHelper(2);
        helper.addChild(this.createCategoryHeader(MULTIPLAYER_TITLE), 2);
        this.initialMultiplayerScope = this.wantedMultiplayerScope = singleplayerServer.getMultiplayerScope();
        helper.addChild(CycleButton.onOffBuilder(singleplayerServer.getMultiplayerScope() == MinecraftServer.MultiplayerScope.LAN).withTooltip(value -> Tooltip.create(value != false ? MinecraftServer.MultiplayerScope.LAN.getTooltip() : MinecraftServer.MultiplayerScope.OFF.getTooltip())).create(Component.translatable("menu.multiplayerOptions.lan"), (cycleButton, value) -> {
            this.wantedMultiplayerScope = value != false ? MinecraftServer.MultiplayerScope.LAN : MinecraftServer.MultiplayerScope.OFF;
            this.updateGuestCommandAccessButton(singleplayerServer);
            this.updatePortControlsState();
            this.updateApplyChangesActiveState();
        }));
        if (this.initialMultiplayerScope == MinecraftServer.MultiplayerScope.LAN) {
            this.initialPort = this.port = singleplayerServer.getPort();
        }
        this.portEdit = new EditBox(this.font, PORT_INFO_TEXT);
        this.portEdit.setResponder(value -> {
            this.setPortError(this.portEdit, this.tryParsePort((String)value));
            this.portEdit.setHint(Component.literal(String.valueOf(this.port)));
            this.updateApplyChangesActiveState();
        });
        this.portEdit.setTooltip(Tooltip.create(PORT_INFO_TEXT));
        if (this.initialMultiplayerScope == MinecraftServer.MultiplayerScope.LAN) {
            this.portEdit.setValue(String.valueOf(this.port));
        }
        helper.addChild(this.portEdit);
        this.updatePortControlsState();
        this.initialGuestCommandAccess = this.wantedGuestCommandAccess = Boolean.valueOf(singleplayerServer.getGuestCommandAccess());
        this.guestCommandAccessButton = helper.addChild(CycleButton.onOffBuilder(this.initialGuestCommandAccess).withTooltip(bl -> GUEST_COMMAND_ACCESS_TOOLTIP).create(GUEST_COMMAND_ACCESS, (cycleButton, value) -> {
            this.wantedGuestCommandAccess = value;
            this.updateForceGameModeButton(singleplayerServer);
            this.updateApplyChangesActiveState();
        }));
        this.initialForceGameMode = this.wantedForceGameMode = Boolean.valueOf(singleplayerServer.forceGameMode());
        this.forceGameModeButton = helper.addChild(CycleButton.onOffBuilder(this.initialForceGameMode).withTooltip(value -> value != false ? FORCE_GAME_MODE_ON_TOOLTIP : FORCE_GAME_MODE_OFF_TOOLTIP).create(FORCE_GAME_MODE, (cycleButton, value) -> {
            this.wantedForceGameMode = value;
            this.updateApplyChangesActiveState();
        }));
        this.updateGuestCommandAccessButton(singleplayerServer);
    }

    private void updateGuestCommandAccessButton(IntegratedServer singleplayerServer) {
        if (this.guestCommandAccessButton != null) {
            Tooltip tooltip;
            boolean lanScope = this.wantedMultiplayerScope == MinecraftServer.MultiplayerScope.LAN;
            boolean allowCommands = Boolean.TRUE.equals(this.wantedAllowCommands);
            if (!lanScope) {
                this.wantedGuestCommandAccess = false;
                tooltip = GUEST_COMMAND_ACCESS_DISABLED_MULTIPLAYER_SCOPE_OFF;
            } else if (!allowCommands) {
                this.wantedGuestCommandAccess = false;
                tooltip = GUEST_COMMAND_ACCESS_DISABLED_COMMANDS_OFF;
            } else {
                this.wantedGuestCommandAccess = singleplayerServer.getGuestCommandAccess();
                tooltip = GUEST_COMMAND_ACCESS_TOOLTIP;
            }
            this.guestCommandAccessButton.setValue(this.wantedGuestCommandAccess);
            this.guestCommandAccessButton.setTooltip(tooltip);
            this.guestCommandAccessButton.active = lanScope && allowCommands;
            this.updateForceGameModeButton(singleplayerServer);
        }
    }

    private void updateForceGameModeButton(IntegratedServer singleplayerServer) {
        if (this.forceGameModeButton != null) {
            Tooltip tooltip;
            boolean lanScope = this.wantedMultiplayerScope == MinecraftServer.MultiplayerScope.LAN;
            boolean guestCommandAccess = Boolean.TRUE.equals(this.wantedGuestCommandAccess);
            if (singleplayerServer.isHardcore()) {
                this.wantedForceGameMode = true;
                tooltip = FORCE_GAME_MODE_OFF_HARDCORE_TOOLTIP;
            } else if (!lanScope) {
                this.wantedForceGameMode = true;
                tooltip = FORCE_GAME_MODE_OFF_MULTIPLAYER_TOOLTIP;
            } else if (guestCommandAccess) {
                this.wantedForceGameMode = false;
                tooltip = FORCE_GAME_MODE_OFF_COMMANDS_TOOLTIP;
            } else {
                this.wantedForceGameMode = singleplayerServer.forceGameMode();
                tooltip = this.wantedForceGameMode != false ? FORCE_GAME_MODE_ON_TOOLTIP : FORCE_GAME_MODE_OFF_TOOLTIP;
            }
            this.forceGameModeButton.setValue(this.wantedForceGameMode);
            this.forceGameModeButton.setTooltip(tooltip);
            this.forceGameModeButton.active = lanScope && !guestCommandAccess && !singleplayerServer.isHardcore();
        }
    }

    protected boolean hasChanges() {
        return this.hasSettingsChanges() && (!this.portIsRequired() || this.portValid);
    }

    protected void applyChanges(@Nullable IntegratedServer singleplayerServer) {
        if (this.wantedDifficulty != null && this.wantedDifficulty != this.initialDifficulty && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().send(new ServerboundChangeDifficultyPacket(this.wantedDifficulty));
        }
        if (this.wantedDifficultyLocked != null && this.wantedDifficultyLocked != this.initialDifficultyLocked && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().send(new ServerboundLockDifficultyPacket(true));
            this.difficultyButtons.lockButton.setLocked(true);
            DifficultyButtons.updateDifficultyButtonsState(this.minecraft, this.level, this.difficultyButtons.difficultyButton, this.difficultyButtons.lockButton);
        }
        if (singleplayerServer == null) {
            return;
        }
        if (this.wantedAllowCommands != null && this.wantedAllowCommands != this.initialAllowCommands) {
            singleplayerServer.setWorldAllowCommands(this.wantedAllowCommands);
        }
        if (this.wantedForceGameMode != null && this.wantedForceGameMode != this.initialForceGameMode) {
            singleplayerServer.setForceGameMode(this.wantedForceGameMode);
        }
        if (this.wantedDefaultGameMode != null && this.wantedDefaultGameMode != this.initialDefaultGameMode) {
            singleplayerServer.setWorldGameType(this.wantedDefaultGameMode);
        }
        if (this.wantedPersonalGameMode != null && this.wantedPersonalGameMode != this.initialPersonalGameMode) {
            singleplayerServer.setPersonalGameType(this.wantedPersonalGameMode);
        }
        if (this.wantedGuestCommandAccess != null && this.wantedGuestCommandAccess != this.initialGuestCommandAccess) {
            singleplayerServer.setGuestCommandAccess(this.wantedGuestCommandAccess);
        }
        if (this.wantedMultiplayerScope != this.initialMultiplayerScope || this.lanPortChanged()) {
            this.changeMultiplayerScope(singleplayerServer);
        }
    }

    private void updateApplyChangesActiveState() {
        if (this.applyChanges != null) {
            this.applyChanges.active = this.hasChanges();
        }
    }

    @Override
    protected void repositionElements() {
        this.scrollArea.arrangeElements();
        this.scrollArea.setMaxHeight(this.layout.getContentHeight());
        this.scrollArea.setMinHeight(this.layout.getContentHeight());
        this.layout.arrangeElements();
        this.scrollArea.setPosition(this.width / 2 - this.scrollArea.getWidth() / 2, this.layout.getHeaderHeight());
    }

    @Override
    protected void extractMenuBackground(GuiGraphicsExtractor graphics) {
        super.extractMenuBackground(graphics);
        graphics.blit(RenderPipelines.GUI_TEXTURED, AbstractSelectionList.INWORLD_MENU_LIST_BACKGROUND, this.layout.getX(), this.layout.getHeaderHeight(), (float)this.width, (float)(this.height - this.layout.getFooterHeight() + (int)this.scrollArea.getScrollAmount()), this.width, this.layout.getContentHeight(), 32, 32);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int xm, int ym, float a) {
        super.extractRenderState(graphics, xm, ym, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.INWORLD_HEADER_SEPARATOR, this.layout.getX(), this.layout.getHeaderHeight() - 2, 0.0f, 0.0f, this.width, 2, 32, 2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Screen.INWORLD_FOOTER_SEPARATOR, this.layout.getX(), this.height - this.layout.getFooterHeight(), 0.0f, 0.0f, this.width, 2, 32, 2);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.lastScreen);
    }

    @Override
    public void onGamemasterPermissionChanged(boolean hasGamemasterPermission) {
        IntegratedServer singleplayerServer = this.minecraft.getSingleplayerServer();
        this.updatePermissionDependentButtons(singleplayerServer, hasGamemasterPermission, true);
        if (!hasGamemasterPermission && !this.minecraft.hasSingleplayerServer()) {
            this.minecraft.gui.setScreen(this.lastScreen);
            Screen screen = this.minecraft.gui.screen();
            if (screen instanceof HasGamemasterPermissionReaction) {
                HasGamemasterPermissionReaction screen2 = (HasGamemasterPermissionReaction)((Object)screen);
                screen2.onGamemasterPermissionChanged(hasGamemasterPermission);
            }
        }
    }

    private void updatePermissionDependentButtons(@Nullable IntegratedServer singleplayerServer, boolean allowCommands, boolean affectGameRulesButton) {
        if (!allowCommands) {
            if (this.defaultGameModeButton != null && this.personalGameModeButton != null && this.initialDefaultGameMode != null) {
                this.wantedDefaultGameMode = this.initialDefaultGameMode;
                this.defaultGameModeButton.setValue(this.wantedDefaultGameMode);
                this.wantedPersonalGameMode = this.wantedDefaultGameMode;
                this.personalGameModeButton.setValue(this.wantedDefaultGameMode);
            }
        } else if (this.personalGameModeButton != null && this.wantedPersonalGameMode != null) {
            this.personalGameModeButton.setValue(this.wantedPersonalGameMode);
        }
        if (affectGameRulesButton) {
            this.updateButton(this.gameRulesButton, singleplayerServer, null, GAMERULES_DISABLED_TOOLTIP, GAMERULES_DISABLED_HARDCORE_TOOLTIP);
        }
        this.updateButton(this.defaultGameModeButton, singleplayerServer, DEFAULT_GAME_MODE_TOOLTIP, GAME_MODE_DISABLED_OPERATOR_TOOLTIP, GAME_MODE_DISABLED_HARDCORE_TOOLTIP);
        this.updateButton(this.personalGameModeButton, singleplayerServer, PERSONAL_GAME_MODE_TOOLTIP, GAME_MODE_DISABLED_OPERATOR_TOOLTIP, GAME_MODE_DISABLED_HARDCORE_TOOLTIP);
        this.difficultyButtons.refresh(this.minecraft, this);
    }

    public void updatePersonalGameModeButton(GameType mode) {
        if (this.personalGameModeButton != null) {
            this.initialPersonalGameMode = mode;
            this.personalGameModeButton.setValue(mode);
        }
    }

    @Override
    public void added() {
        this.difficultyButtons.refresh(this.minecraft, this);
    }

    @Override
    public void onDifficultyChanged() {
        this.difficultyButtons.refresh(this.minecraft, this, true);
    }

    public void onDefaultGameModeChanged(GameType mode) {
        if (this.defaultGameModeButton != null) {
            this.initialDefaultGameMode = mode;
            this.defaultGameModeButton.setValue(mode);
        }
    }

    private boolean portIsRequired() {
        return this.wantedMultiplayerScope == MinecraftServer.MultiplayerScope.LAN;
    }

    private boolean hasSettingsChanges() {
        return this.wantedDifficulty != this.initialDifficulty || this.wantedDifficultyLocked != this.initialDifficultyLocked || this.wantedDefaultGameMode != this.initialDefaultGameMode || this.wantedPersonalGameMode != this.initialPersonalGameMode || this.wantedAllowCommands != this.initialAllowCommands || this.wantedMultiplayerScope != this.initialMultiplayerScope || this.wantedGuestCommandAccess != this.initialGuestCommandAccess || this.wantedForceGameMode != this.initialForceGameMode || this.lanPortChanged();
    }

    private boolean lanPortChanged() {
        return this.wantedMultiplayerScope == MinecraftServer.MultiplayerScope.LAN && this.initialMultiplayerScope == MinecraftServer.MultiplayerScope.LAN && this.port != this.initialPort;
    }

    private void changeMultiplayerScope(IntegratedServer singleplayerServer) {
        if (this.wantedMultiplayerScope == null) {
            return;
        }
        if (singleplayerServer.unpublishServer()) {
            this.sendPublishMessage(Component.translatable("menu.multiplayerOptions.publish.stopped"));
        }
        if (this.wantedMultiplayerScope != MinecraftServer.MultiplayerScope.OFF) {
            this.publish(singleplayerServer, this.wantedMultiplayerScope);
        }
        this.minecraft.getPlayerSocialManager().getPresenceHandler().tryUpdatePresence();
    }

    private void publish(IntegratedServer singleplayerServer, MinecraftServer.MultiplayerScope scope) {
        if (!singleplayerServer.publishServer(scope, this.port)) {
            this.sendPublishMessage(Component.translatable("commands.publish.failed"));
            return;
        }
        MutableComponent message = scope == MinecraftServer.MultiplayerScope.LAN ? Component.translatable("menu.multiplayerOptions.publish.started.lan", ComponentUtils.copyOnClickText(String.valueOf(this.port))) : Component.translatable("menu.multiplayerOptions.publish.started.online");
        this.sendPublishMessage(message);
    }

    private void sendPublishMessage(Component message) {
        this.minecraft.gui.hud.getChat().addClientSystemMessage(message);
        this.minecraft.getNarrator().saySystemQueued(message);
        this.minecraft.updateTitle();
    }

    private void updatePortControlsState() {
        boolean lanWanted;
        boolean bl = lanWanted = this.wantedMultiplayerScope == MinecraftServer.MultiplayerScope.LAN;
        if (this.portEdit != null) {
            String desired;
            String string = lanWanted ? (this.initialMultiplayerScope == MinecraftServer.MultiplayerScope.LAN ? String.valueOf(this.initialPort) : "") : (desired = "");
            if (!this.portEdit.getValue().equals(desired)) {
                this.portEdit.setValue(desired);
            }
            this.portEdit.setEditable(lanWanted);
            this.portEdit.active = lanWanted;
            this.portEdit.setHint(lanWanted ? Component.literal(String.valueOf(this.port)) : PORT_INFO_TEXT);
            if (!lanWanted) {
                this.portEdit.setFocused(false);
                this.setPortError(this.portEdit, null);
            }
        }
    }

    private @Nullable Component tryParsePort(String value) {
        if (value.isBlank()) {
            this.port = HttpUtil.getAvailablePort();
            return null;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1024 || parsed > 65535) {
                return INVALID_PORT;
            }
            if (parsed != this.initialPort && !HttpUtil.isPortAvailable(parsed)) {
                return PORT_UNAVAILABLE;
            }
            this.port = parsed;
            return null;
        }
        catch (NumberFormatException e) {
            this.port = HttpUtil.getAvailablePort();
            return INVALID_PORT;
        }
    }

    private void setPortError(EditBox portEdit, @Nullable Component errorMessage) {
        boolean bl = this.portValid = errorMessage == null;
        if (errorMessage == null) {
            portEdit.setTextColor(-2039584);
            portEdit.setTooltip(Tooltip.create(PORT_INFO_TEXT));
        } else {
            portEdit.setTextColor(-2142128);
            portEdit.setTooltip(Tooltip.create(errorMessage));
        }
    }

    private record DifficultyButtons(LayoutElement layout, CycleButton<Difficulty> difficultyButton, LockIconButton lockButton, Level level) {
        private static final Component DIFFICULTY_TITLE = Component.translatable("options.difficulty");
        private static final Tooltip DIFFICULTY_DISABLED_HARDCORE_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.difficulty.disabled.hardcore.tooltip"));
        private static final Tooltip DIFFICULTY_DISABLED_LOCKED_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.difficulty.disabled.locked.tooltip"));
        private static final Tooltip DIFFICULTY_DISABLED_OPERATOR_TOOLTIP = Tooltip.create(Component.translatable("options.worldOptions.difficulty.disabled.operator.tooltip"));

        public static DifficultyButtons create(Minecraft minecraft, Level level, WorldOptionsScreen screen) {
            screen.initialDifficulty = screen.wantedDifficulty = level.getDifficulty();
            CycleButton<Difficulty> difficultyButton = CycleButton.builder(Difficulty::getDisplayName, level.getDifficulty()).withValues((Difficulty[])Difficulty.values()).create(0, 0, 150, 20, DIFFICULTY_TITLE, (cycleButton, value) -> {
                screen.wantedDifficulty = value;
                screen.updateApplyChangesActiveState();
            });
            screen.initialDifficultyLocked = screen.wantedDifficultyLocked = Boolean.valueOf(DifficultyButtons.isDifficultyLocked(level));
            LockIconButton lockButton = new LockIconButton(0, 0, button -> {
                if (button instanceof LockIconButton) {
                    LockIconButton lockIconButton = (LockIconButton)button;
                    lockIconButton.setLocked(screen.wantedDifficultyLocked == false);
                }
                screen.wantedDifficultyLocked = screen.wantedDifficultyLocked == false;
                screen.updateApplyChangesActiveState();
            });
            difficultyButton.setWidth(difficultyButton.getWidth() - lockButton.getWidth());
            lockButton.setLocked(DifficultyButtons.isDifficultyLocked(level));
            DifficultyButtons.updateDifficultyButtonsState(minecraft, level, difficultyButton, lockButton);
            EqualSpacingLayout linearLayout = new EqualSpacingLayout(150, 0, EqualSpacingLayout.Orientation.HORIZONTAL);
            linearLayout.addChild(difficultyButton);
            linearLayout.addChild(lockButton);
            return new DifficultyButtons(linearLayout, difficultyButton, lockButton, level);
        }

        private void refresh(Minecraft minecraft, WorldOptionsScreen worldOptionsScreen) {
            this.refresh(minecraft, worldOptionsScreen, false);
        }

        private void refresh(Minecraft minecraft, WorldOptionsScreen worldOptionsScreen, boolean forceLevelDifficulty) {
            this.difficultyButton.setValue(forceLevelDifficulty ? this.level.getDifficulty() : (worldOptionsScreen.wantedDifficulty != null ? worldOptionsScreen.wantedDifficulty : this.level.getDifficulty()));
            this.lockButton.setLocked(worldOptionsScreen.wantedDifficultyLocked != null ? worldOptionsScreen.wantedDifficultyLocked : DifficultyButtons.isDifficultyLocked(this.level));
            DifficultyButtons.updateDifficultyButtonsState(minecraft, this.level, this.difficultyButton, this.lockButton);
        }

        private static void updateDifficultyButtonsState(Minecraft minecraft, Level level, CycleButton<Difficulty> difficultyButton, LockIconButton lockButton) {
            if (minecraft.player == null || !minecraft.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && (minecraft.getSingleplayerServer() == null || !minecraft.getSingleplayerServer().isSingleplayerOwner(minecraft.player.nameAndId()))) {
                lockButton.active = false;
                difficultyButton.active = false;
                difficultyButton.setTooltip(DIFFICULTY_DISABLED_OPERATOR_TOOLTIP);
            } else if (level.getLevelData().isDifficultyLocked()) {
                lockButton.active = false;
                difficultyButton.active = false;
                difficultyButton.setTooltip(DIFFICULTY_DISABLED_LOCKED_TOOLTIP);
            } else if (level.getLevelData().isHardcore()) {
                lockButton.active = false;
                difficultyButton.active = false;
                difficultyButton.setTooltip(DIFFICULTY_DISABLED_HARDCORE_TOOLTIP);
            } else {
                lockButton.active = true;
                difficultyButton.active = true;
                difficultyButton.setTooltip(null);
            }
        }

        private static boolean isDifficultyLocked(Level level) {
            return level.getLevelData().isDifficultyLocked() || level.getLevelData().isHardcore();
        }
    }
}

