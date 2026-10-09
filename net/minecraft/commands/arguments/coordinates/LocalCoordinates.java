/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record LocalCoordinates(double left, double up, double forwards) implements Coordinates
{
    public static final char PREFIX_LOCAL_COORDINATE = '^';

    @Override
    public Vec3 getPosition(CommandSourceStack sender) {
        Vec3 source = sender.getAnchor().apply(sender);
        return this.apply(source, sender.getRotation());
    }

    public Vec3 apply(Vec3 source, Vec2 rotation) {
        float yCos = Mth.cos((rotation.y + 90.0f) * ((float)Math.PI / 180));
        float ySin = Mth.sin((rotation.y + 90.0f) * ((float)Math.PI / 180));
        float xCos = Mth.cos(-rotation.x * ((float)Math.PI / 180));
        float xSin = Mth.sin(-rotation.x * ((float)Math.PI / 180));
        float xCosUp = Mth.cos((-rotation.x + 90.0f) * ((float)Math.PI / 180));
        float xSinUp = Mth.sin((-rotation.x + 90.0f) * ((float)Math.PI / 180));
        Vec3 forwards = new Vec3(yCos * xCos, xSin, ySin * xCos);
        Vec3 up = new Vec3(yCos * xCosUp, xSinUp, ySin * xCosUp);
        Vec3 left = forwards.cross(up).scale(-1.0);
        return source.add(forwards.x * this.forwards + up.x * this.up + left.x * this.left, forwards.y * this.forwards + up.y * this.up + left.y * this.left, forwards.z * this.forwards + up.z * this.up + left.z * this.left);
    }

    @Override
    public Vec2 getRotation(CommandSourceStack sender) {
        return Vec2.ZERO;
    }

    @Override
    public boolean isXRelative() {
        return true;
    }

    @Override
    public boolean isYRelative() {
        return true;
    }

    @Override
    public boolean isZRelative() {
        return true;
    }

    public static LocalCoordinates parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        double left = LocalCoordinates.readDouble(reader, start);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(start);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)reader);
        }
        reader.skip();
        double up = LocalCoordinates.readDouble(reader, start);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(start);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)reader);
        }
        reader.skip();
        double forwards = LocalCoordinates.readDouble(reader, start);
        return new LocalCoordinates(left, up, forwards);
    }

    private static double readDouble(StringReader reader, int start) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext((ImmutableStringReader)reader);
        }
        if (reader.peek() != '^') {
            reader.setCursor(start);
            throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext((ImmutableStringReader)reader);
        }
        reader.skip();
        return reader.canRead() && reader.peek() != ' ' ? reader.readDouble() : 0.0;
    }
}

