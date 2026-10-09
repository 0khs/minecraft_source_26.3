/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jspecify.annotations.Nullable;

public record StoredNumberAccess(Identifier storage, NbtPathArgument.NbtPath path) {
    public static final MapCodec<StoredNumberAccess> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group((App)Identifier.CODEC.fieldOf("storage").forGetter(StoredNumberAccess::storage), (App)NbtPathArgument.NbtPath.CODEC.fieldOf("path").forGetter(StoredNumberAccess::path)).apply((Applicative)i, StoredNumberAccess::new));

    public @Nullable Number getNumericTag(LootContext context) {
        CompoundTag value = context.getLevel().getServer().getCommandStorage().get(this.storage);
        try {
            Tag tag;
            List<Tag> selectedTags = this.path.get(value);
            if (selectedTags.size() == 1 && (tag = selectedTags.getFirst()) instanceof NumericTag) {
                NumericTag result = (NumericTag)tag;
                return result.box();
            }
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return null;
    }
}

