/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HexFormat;

public record PngInfo(int width, int height, byte bitDepth, byte colorType) {
    private static final HexFormat FORMAT = HexFormat.of().withUpperCase().withPrefix("0x");
    private static final int PNG_HEADER_SIZE = 8;
    private static final long PNG_HEADER = -8552249625308161526L;
    private static final int PNG_CHUNK_HEADER_SIZE = 8;
    private static final int PNG_CHUNK_TRAILER_SIZE = 4;
    private static final int IHDR_TYPE = 1229472850;
    private static final int IHDR_SIZE = 13;
    private static final byte COLOR_TYPE_GRAYSCALE = 0;
    private static final byte COLOR_TYPE_TRUECOLOR = 2;
    private static final byte COLOR_TYPE_INDEXED = 3;
    private static final byte COLOR_TYPE_GRAYSCALE_ALPHA = 4;
    private static final byte COLOR_TYPE_TRUECOLOR_ALPHA = 6;

    public static PngInfo fromStream(InputStream inputStream) throws IOException {
        DataInputStream stream = new DataInputStream(inputStream);
        long magic = stream.readLong();
        if (magic != -8552249625308161526L) {
            throw new IOException("Bad PNG Signature: " + FORMAT.toHexDigits(magic));
        }
        int headerSize = stream.readInt();
        if (headerSize != 13) {
            throw new IOException("Bad length for IHDR chunk: " + headerSize);
        }
        int headerType = stream.readInt();
        if (headerType != 1229472850) {
            throw new IOException("Bad type for IHDR chunk: " + FORMAT.toHexDigits(headerType));
        }
        int width = stream.readInt();
        int height = stream.readInt();
        byte bitDepth = stream.readByte();
        if (!PngInfo.validateBitDepth(bitDepth)) {
            throw new IOException("Invalid bit depth: " + bitDepth);
        }
        byte colorType = stream.readByte();
        if (!PngInfo.validateColorType(colorType)) {
            throw new IOException("Invalid color type: " + colorType);
        }
        if (!PngInfo.validateBounds(width, height, bitDepth, colorType)) {
            throw new IOException("Bounds check failed, " + width + "x" + height + "@" + bitDepth + ", type: " + colorType);
        }
        return new PngInfo(width, height, bitDepth, colorType);
    }

    public static PngInfo fromBytes(byte[] bytes) throws IOException {
        return PngInfo.fromStream(new ByteArrayInputStream(bytes));
    }

    public static void validateHeader(ByteBuffer buffer) throws IOException {
        ByteOrder order = buffer.order();
        buffer.order(ByteOrder.BIG_ENDIAN);
        if (buffer.limit() < 33) {
            throw new IOException("PNG header missing");
        }
        if (buffer.getLong(0) != -8552249625308161526L) {
            throw new IOException("Bad PNG Signature");
        }
        if (buffer.getInt(8) != 13) {
            throw new IOException("Bad length for IHDR chunk!");
        }
        if (buffer.getInt(12) != 1229472850) {
            throw new IOException("Bad type for IHDR chunk!");
        }
        int width = buffer.getInt(16);
        int height = buffer.getInt(20);
        byte bitDepth = buffer.get(24);
        if (!PngInfo.validateBitDepth(bitDepth)) {
            throw new IOException("Invalid bit depth: " + bitDepth);
        }
        byte colorType = buffer.get(25);
        if (!PngInfo.validateColorType(colorType)) {
            throw new IOException("Invalid color type: " + colorType);
        }
        if (!PngInfo.validateBounds(width, height, bitDepth, colorType)) {
            throw new IOException("Bounds check failed, " + width + "x" + height + "@" + bitDepth + ", type: " + colorType);
        }
        buffer.order(order);
    }

    private static boolean validateBitDepth(int bitDepth) {
        return switch (bitDepth) {
            case 1, 2, 4, 8, 16 -> true;
            default -> false;
        };
    }

    private static boolean validateColorType(int colorType) {
        return switch (colorType) {
            case 0, 2, 3, 4, 6 -> true;
            default -> false;
        };
    }

    private static boolean validateBounds(int width, int height, byte bitDepth, byte colorType) {
        if (bitDepth == 16) {
            long componentCount = 4L;
            long bufferEstimate = 4L * (long)width * (long)height * 2L;
            if (bufferEstimate != (long)((int)bufferEstimate)) {
                return false;
            }
        }
        return true;
    }
}

