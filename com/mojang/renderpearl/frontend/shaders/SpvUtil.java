/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntList
 *  org.lwjgl.util.spvc.Spvc
 *  org.lwjgl.util.spvc.SpvcReflectedResource
 */
package com.mojang.renderpearl.frontend.shaders;

import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.util.ShaderCompileException;
import it.unimi.dsi.fastutil.ints.IntList;
import java.nio.IntBuffer;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.util.spvc.SpvcReflectedResource;

public class SpvUtil {
    public static final IntList DESCRIPTOR_TYPES = IntList.of((int[])new int[]{1, 2, 6, 7, 10, 11});

    public static void crashIfError(int result, String message) {
        if (result == 0) {
            return;
        }
        throw new IllegalStateException(message + " (" + SpvUtil.spvcErrorString(result) + ")");
    }

    public static void throwIfError(int result, String message) throws ShaderCompileException {
        if (result == 0) {
            return;
        }
        throw new ShaderCompileException(message + " (" + SpvUtil.spvcErrorString(result) + ")");
    }

    public static String spvcErrorString(int result) {
        return switch (result) {
            case -1 -> "SPVC_ERROR_INVALID_SPIRV";
            case -2 -> "SPVC_ERROR_UNSUPPORTED_SPIRV";
            case -3 -> "SPVC_ERROR_OUT_OF_MEMORY";
            case -4 -> "SPVC_ERROR_INVALID_ARGUMENT";
            default -> Integer.toString(result);
        };
    }

    public static int getDecorationOffset(long compiler, SpvcReflectedResource resource, int decoration, IntBuffer returnBuffer) throws ShaderCompileException {
        if (!Spvc.spvc_compiler_get_binary_offset_for_decoration((long)compiler, (int)resource.id(), (int)decoration, (IntBuffer)returnBuffer)) {
            throw new ShaderCompileException("Couldn't find byte offset for location decoration of " + resource.nameString());
        }
        return returnBuffer.get(0);
    }

    public static int resourceType(UniformType uniformType) {
        return switch (uniformType) {
            default -> throw new MatchException(null, null);
            case UniformType.COMBINED_IMAGE_SAMPLER -> 7;
            case UniformType.UNIFORM_BUFFER -> 1;
            case UniformType.TEXEL_BUFFER -> 7;
        };
    }

    public static String baseTypeString(int baseType) {
        return switch (baseType) {
            case 0 -> "unknown";
            case 1 -> "void";
            case 2 -> "bool";
            case 3 -> "int8";
            case 4 -> "uint8";
            case 5 -> "int16";
            case 6 -> "uint16";
            case 7 -> "int32";
            case 8 -> "uint32";
            case 9 -> "int64";
            case 10 -> "uint64";
            case 11 -> "atomic_counter";
            case 12 -> "fp16";
            case 13 -> "fp32";
            case 14 -> "fp64";
            case 15 -> "struct";
            case 16 -> "image";
            case 17 -> "sampled_image";
            case 18 -> "sampler";
            case 19 -> "acceleration_structure";
            default -> "UNKNOWN_TYPE";
        };
    }
}

