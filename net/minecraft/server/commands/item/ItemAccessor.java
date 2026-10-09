/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.slot.SlotCollection;
import net.minecraft.world.item.slot.SlotSource;
import org.jspecify.annotations.Nullable;

public interface ItemAccessor<Target> {
    public void setItems(CommandSourceStack var1, SlotSource var2, SetterFunction<Target> var3) throws CommandSyntaxException;

    public SlotCollection getSlots(CommandSourceStack var1, SlotSource var2) throws CommandSyntaxException;

    public int getReplaceSuccess(CommandSourceStack var1, CommandResponseTracker<Target> var2, @Nullable ItemStack var3) throws CommandSyntaxException;

    public int getModifySuccess(CommandSourceStack var1, CommandResponseTracker<Target> var2) throws CommandSyntaxException;

    @FunctionalInterface
    public static interface SetterFunction<Target> {
        public int apply(Target var1, SlotCollection var2);
    }
}

