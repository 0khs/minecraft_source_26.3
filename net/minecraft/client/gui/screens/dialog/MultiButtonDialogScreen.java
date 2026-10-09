/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.dialog;

import java.util.stream.Stream;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.ButtonListDialogScreen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.MultiActionDialog;
import org.jspecify.annotations.Nullable;

public class MultiButtonDialogScreen
extends ButtonListDialogScreen<MultiActionDialog> {
    public MultiButtonDialogScreen(@Nullable Screen previousScreen, MultiActionDialog dialog, DialogConnectionAccess connectionAccess) {
        super(previousScreen, dialog, connectionAccess);
    }

    @Override
    protected Stream<ActionButton> createListActions(MultiActionDialog dialog, DialogConnectionAccess connectionAccess) {
        return dialog.actions().stream();
    }
}

