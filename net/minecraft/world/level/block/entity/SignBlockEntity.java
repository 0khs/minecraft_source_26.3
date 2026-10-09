/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.ResolutionContext;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SignBlockEntity
extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TEXT_LINE_WIDTH = 90;
    private static final int TEXT_LINE_HEIGHT = 10;
    private static final boolean DEFAULT_IS_WAXED = false;
    private static final boolean DEFAULT_ALLOW_OP_FEATURES = false;
    private static final Component CLICK_ACTIONS_DISABLED = Component.translatable("sign.click_actions_disabled").withStyle(ChatFormatting.RED);
    private @Nullable UUID playerWhoMayEdit;
    private SignText frontText = SignText.EMPTY;
    private SignText backText = SignText.EMPTY;
    private boolean isWaxed = false;
    private boolean allowOpFeatures = false;

    public SignBlockEntity(BlockPos worldPosition, BlockState blockState) {
        this((BlockEntityType<? extends SignBlockEntity>)BlockEntityTypes.SIGN, worldPosition, blockState);
    }

    public SignBlockEntity(BlockEntityType<? extends SignBlockEntity> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public SignTextSlot getSlotPlayerIsFacing(Player player) {
        Block block = this.getBlockState().getBlock();
        if (block instanceof SignBlock) {
            float playerYRot;
            SignBlock sign = (SignBlock)block;
            Vec3 signPositionOffset = sign.getSignHitboxCenterPosition(this.getBlockState());
            double xd = player.getX() - ((double)this.getBlockPos().getX() + signPositionOffset.x);
            double zd = player.getZ() - ((double)this.getBlockPos().getZ() + signPositionOffset.z);
            float signYRot = sign.getYRotationDegrees(this.getBlockState());
            return Mth.degreesDifferenceAbs(signYRot, playerYRot = (float)(Mth.atan2(zd, xd) * 57.2957763671875) - 90.0f) <= 90.0f ? SignTextSlot.FRONT : SignTextSlot.BACK;
        }
        return SignTextSlot.FRONT;
    }

    public SignText getText(SignTextSlot slot) {
        return switch (slot) {
            default -> throw new MatchException(null, null);
            case SignTextSlot.FRONT -> this.frontText;
            case SignTextSlot.BACK -> this.backText;
        };
    }

    public int getTextLineHeight() {
        return 10;
    }

    public int getMaxTextLineWidth() {
        return 90;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.allowOpFeatures) {
            output.putBoolean("allow_op_features", this.allowOpFeatures);
        }
        output.store("front_text", SignText.CODEC, this.frontText);
        output.store("back_text", SignText.CODEC, this.backText);
        output.putBoolean("is_waxed", this.isWaxed);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.allowOpFeatures = input.getBooleanOr("allow_op_features", false);
        this.frontText = input.read("front_text", SignText.CODEC).map(this::resolveLines).orElse(SignText.EMPTY);
        this.backText = input.read("back_text", SignText.CODEC).map(this::resolveLines).orElse(SignText.EMPTY);
        this.isWaxed = input.getBooleanOr("is_waxed", false);
    }

    private SignText resolveLines(SignText data) {
        if (!this.allowOpFeatures) {
            return data;
        }
        return data.asMutable().modifyLines(this::resolveLine).asImmutable();
    }

    private Component resolveLine(Component component) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            try {
                return ComponentUtils.resolve(ResolutionContext.create(SignBlockEntity.createCommandSourceStack(null, serverLevel, this.worldPosition)), component);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }
        return component;
    }

    public void updateSignText(Player player, SignTextSlot slot, List<FilteredText> lines) {
        if (this.isWaxed() || !player.getUUID().equals(this.getPlayerWhoMayEdit()) || this.level == null) {
            LOGGER.warn("Player {} just tried to change non-editable sign", (Object)player.getPlainTextName());
            return;
        }
        this.updateText(text -> SignBlockEntity.updateMessages(lines, text, player.isTextFilteringEnabled()), slot);
        this.setAllowedPlayerEditor(null);
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean updateText(UnaryOperator<SignText> function, SignTextSlot slot) {
        SignText newText;
        SignText currentText = this.getText(slot);
        if (currentText != (newText = (SignText)function.apply(currentText))) {
            this.setText(newText, slot);
            return true;
        }
        return false;
    }

    private static SignText updateMessages(List<FilteredText> newLines, SignText originalText, boolean shouldFilter) {
        SignText.Mutable result = originalText.asMutable();
        List<Component> originalLines = originalText.getMessages(shouldFilter);
        for (int i = 0; i < newLines.size(); ++i) {
            FilteredText line = newLines.get(i);
            Style currentTextStyle = originalLines.get(i).getStyle();
            if (shouldFilter) {
                result.setLine(i, Component.literal(line.filteredOrEmpty()).setStyle(currentTextStyle));
                continue;
            }
            result.setLine(i, Component.literal(line.raw()).setStyle(currentTextStyle), Component.literal(line.filteredOrEmpty()).setStyle(currentTextStyle));
        }
        return result.asImmutable();
    }

    public void setText(SignText text, SignTextSlot slot) {
        switch (slot) {
            case FRONT: {
                this.frontText = text;
                break;
            }
            case BACK: {
                this.backText = text;
            }
        }
        this.markUpdated();
    }

    public boolean canExecuteClickCommands(SignTextSlot slot, Player player) {
        return this.isWaxed() && this.getText(slot).hasAnyClickCommands(player.isTextFilteringEnabled());
    }

    public boolean executeClickCommandsIfPresent(ServerLevel level, Player player, BlockPos pos, SignTextSlot slot) {
        boolean hasAnyClickCommand = false;
        for (Component message : this.getText(slot).getMessages(player.isTextFilteringEnabled())) {
            ClickEvent event;
            Style style = message.getStyle();
            ClickEvent clickEvent = event = style.getClickEvent();
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClickEvent.RunCommand.class, ClickEvent.ShowDialog.class, ClickEvent.Custom.class}, (ClickEvent)clickEvent, n)) {
                case 0: {
                    ClickEvent.RunCommand command = (ClickEvent.RunCommand)clickEvent;
                    if (this.allowOpFeatures) {
                        level.getServer().getCommands().performPrefixedCommand(SignBlockEntity.createCommandSourceStack(player, level, pos), command.command());
                    }
                    hasAnyClickCommand = true;
                    break;
                }
                case 1: {
                    ClickEvent.ShowDialog dialog = (ClickEvent.ShowDialog)clickEvent;
                    if (this.allowOpFeatures) {
                        player.openDialog(dialog.dialog());
                    }
                    hasAnyClickCommand = true;
                    break;
                }
                case 2: {
                    ClickEvent.Custom custom = (ClickEvent.Custom)clickEvent;
                    if (this.allowOpFeatures) {
                        level.getServer().handleCustomClickAction(custom.id(), custom.payload());
                    }
                    hasAnyClickCommand = true;
                    break;
                }
            }
        }
        if (!this.allowOpFeatures && hasAnyClickCommand) {
            player.sendOverlayMessage(CLICK_ACTIONS_DISABLED);
        }
        return hasAnyClickCommand;
    }

    private static CommandSourceStack createCommandSourceStack(@Nullable Player player, ServerLevel level, BlockPos pos) {
        if (player != null) {
            return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(pos), Vec2.ZERO, level, (PermissionSet)LevelBasedPermissionSet.GAMEMASTER, level.getServer(), player);
        }
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(pos), Vec2.ZERO, level, (PermissionSet)LevelBasedPermissionSet.GAMEMASTER, Component.literal("Sign"), level.getServer());
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public void setAllowedPlayerEditor(@Nullable UUID playerUUID) {
        this.playerWhoMayEdit = playerUUID;
    }

    public @Nullable UUID getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean isWaxed() {
        return this.isWaxed;
    }

    public boolean setWaxed(boolean isWaxed) {
        if (this.isWaxed != isWaxed) {
            this.isWaxed = isWaxed;
            this.markUpdated();
            return true;
        }
        return false;
    }

    public boolean playerIsTooFarAwayToEdit(UUID player) {
        Player editingPlayer = this.level.getPlayerByUUID(player);
        return editingPlayer == null || !editingPlayer.isWithinBlockInteractionRange(this.getBlockPos(), 4.0);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SignBlockEntity signBlockEntity) {
        UUID playerWhoMayEdit = signBlockEntity.getPlayerWhoMayEdit();
        if (playerWhoMayEdit != null) {
            signBlockEntity.clearInvalidPlayerWhoMayEdit(signBlockEntity, level, playerWhoMayEdit);
        }
    }

    private void clearInvalidPlayerWhoMayEdit(SignBlockEntity signBlockEntity, Level level, UUID playerWhoMayEdit) {
        if (signBlockEntity.playerIsTooFarAwayToEdit(playerWhoMayEdit)) {
            signBlockEntity.setAllowedPlayerEditor(null);
        }
    }

    public SoundEvent getSignInteractionFailedSoundEvent() {
        return SoundEvents.WAXED_SIGN_INTERACT_FAIL;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.frontText = components.getOrDefault(DataComponents.SIGN_TEXT_FRONT, SignText.EMPTY);
        this.backText = components.getOrDefault(DataComponents.SIGN_TEXT_BACK, SignText.EMPTY);
        this.isWaxed = components.get(DataComponents.WAXED) != null;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.SIGN_TEXT_FRONT, this.frontText);
        components.set(DataComponents.SIGN_TEXT_BACK, this.backText);
        if (this.isWaxed) {
            components.set(DataComponents.WAXED, Unit.INSTANCE);
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("front_text");
        output.discard("back_text");
        output.discard("is_waxed");
    }
}

