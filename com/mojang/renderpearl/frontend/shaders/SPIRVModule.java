/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceMap
 *  it.unimi.dsi.fastutil.objects.ReferenceArrayList
 *  it.unimi.dsi.fastutil.objects.ReferenceList
 *  it.unimi.dsi.fastutil.objects.ReferenceLists
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.spvc.Spvc
 *  org.lwjgl.util.spvc.SpvcReflectedResource
 *  org.lwjgl.util.spvc.SpvcReflectedResource$Buffer
 */
package com.mojang.renderpearl.frontend.shaders;

import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.backend.api.SpvModule;
import com.mojang.renderpearl.frontend.shaders.SpvUtil;
import com.mojang.renderpearl.util.ShaderCompileException;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import it.unimi.dsi.fastutil.objects.ReferenceLists;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.util.spvc.SpvcReflectedResource;

public class SPIRVModule
implements SpvModule {
    private final ByteBuffer spv;
    private final ShaderType type;
    private @Nullable SpvModule.Reflection reflection;
    private long spvcContext;

    public SPIRVModule(ByteBuffer spv, ShaderType type) {
        this.spv = spv;
        this.type = type;
    }

    @Override
    public void close() {
        MemoryUtil.memFree((ByteBuffer)this.spv);
        if (this.spvcContext != 0L) {
            Spvc.spvc_context_destroy((long)this.spvcContext);
        }
    }

    @Override
    public ByteBuffer spv() {
        return this.spv;
    }

    @Override
    public ShaderType type() {
        return this.type;
    }

    @Override
    public SpvModule.Reflection reflect() throws ShaderCompileException {
        if (this.reflection == null) {
            this.doReflection();
        }
        return this.reflection;
    }

    @Override
    public @Nullable SpvModule.Reflection getReflectionInfoIfAvailable() {
        return this.reflection;
    }

    private void doReflection() throws ShaderCompileException {
        IntBuffer spirvBuffer = this.spv.asIntBuffer();
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer pointer = stack.callocPointer(1);
            SpvUtil.throwIfError(Spvc.spvc_context_create((PointerBuffer)pointer), "Couldn't create spvc context");
            this.spvcContext = pointer.get(0);
        }
        stack = MemoryStack.stackPush();
        try {
            PointerBuffer pointerReturnBuffer = stack.callocPointer(1);
            SpvUtil.throwIfError(Spvc.spvc_context_parse_spirv((long)this.spvcContext, (IntBuffer)spirvBuffer, (long)spirvBuffer.remaining(), (PointerBuffer)pointerReturnBuffer), "Couldn't parse spirv");
            long ir = pointerReturnBuffer.get(0);
            SpvUtil.throwIfError(Spvc.spvc_context_create_compiler((long)this.spvcContext, (int)0, (long)ir, (int)0, (PointerBuffer)pointerReturnBuffer), "Couldn't create compiler");
            long compiler = pointerReturnBuffer.get(0);
            SpvUtil.throwIfError(Spvc.spvc_compiler_create_shader_resources((long)compiler, (PointerBuffer)pointerReturnBuffer), "Couldn't create resource list");
            long spvcResources = pointerReturnBuffer.get(0);
            final List<SpvModule.Reflection.InterfaceVariable> inputs = SPIRVModule.generateInterfaceVariableList(compiler, spvcResources, spirvBuffer, 3);
            final List<SpvModule.Reflection.InterfaceVariable> outputs = SPIRVModule.generateInterfaceVariableList(compiler, spvcResources, spirvBuffer, 4);
            Int2ReferenceArrayMap descriptors = new Int2ReferenceArrayMap();
            for (int i = 0; i < SpvUtil.DESCRIPTOR_TYPES.size(); ++i) {
                int descriptorType = SpvUtil.DESCRIPTOR_TYPES.getInt(i);
                descriptors.put(descriptorType, SPIRVModule.generateDescriptorList(compiler, spvcResources, spirvBuffer, descriptorType));
            }
            List allDescriptors = descriptors.values().stream().flatMap(Collection::stream).toList();
            List<SpvModule.Reflection.PushConstant> pushConstants = SPIRVModule.generatePushConstantList(compiler, spvcResources);
            this.reflection = new SpvModule.Reflection(){
                final /* synthetic */ Int2ReferenceMap val$descriptors;
                final /* synthetic */ List val$allDescriptors;
                final /* synthetic */ List val$pushConstants;
                {
                    this.val$descriptors = int2ReferenceMap;
                    this.val$allDescriptors = list3;
                    this.val$pushConstants = list4;
                    Objects.requireNonNull(this$0);
                }

                @Override
                public List<SpvModule.Reflection.InterfaceVariable> inputs() {
                    return inputs;
                }

                @Override
                public List<SpvModule.Reflection.InterfaceVariable> outputs() {
                    return outputs;
                }

                @Override
                public List<SpvModule.Reflection.Descriptor> descriptors(int descriptorType) {
                    return (List)this.val$descriptors.getOrDefault(descriptorType, List.of());
                }

                @Override
                public List<SpvModule.Reflection.Descriptor> descriptors() {
                    return this.val$allDescriptors;
                }

                @Override
                public List<SpvModule.Reflection.PushConstant> pushConstants() {
                    return this.val$pushConstants;
                }
            };
        }
        finally {
            if (stack != null) {
                stack.close();
            }
        }
    }

    private static List<SpvModule.Reflection.InterfaceVariable> generateInterfaceVariableList(final long compiler, long spvcResources, final IntBuffer spirv, int resourceType) throws ShaderCompileException {
        ReferenceArrayList list = new ReferenceArrayList();
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer pointerReturnBuffer = stack.callocPointer(1);
            IntBuffer intReturnBuffer = stack.callocInt(1);
            PointerBuffer countPointer = stack.callocPointer(1);
            SpvUtil.throwIfError(Spvc.spvc_resources_get_resource_list_for_type((long)spvcResources, (int)resourceType, (PointerBuffer)pointerReturnBuffer, (PointerBuffer)countPointer), "Couldn't list input variables");
            long spvcList = pointerReturnBuffer.get(0);
            long spvcCount = countPointer.get(0);
            SpvcReflectedResource.Buffer resources = SpvcReflectedResource.create((long)spvcList, (int)((int)spvcCount));
            int i = 0;
            while ((long)i < spvcCount) {
                final SpvcReflectedResource resource = (SpvcReflectedResource)resources.get(i);
                final String name = resource.nameString();
                if (!Spvc.spvc_compiler_get_binary_offset_for_decoration((long)compiler, (int)resource.id(), (int)30, (IntBuffer)intReturnBuffer)) {
                    throw new ShaderCompileException("Couldn't find byte offset for location decoration of " + name);
                }
                final int locationOffset = intReturnBuffer.get(0);
                final TypeHandle typeHandle = new TypeHandle(Spvc.spvc_compiler_get_type_handle((long)compiler, (int)resource.type_id()));
                list.add((Object)new SpvModule.Reflection.InterfaceVariable(){

                    @Override
                    public String name() {
                        return name;
                    }

                    @Override
                    public SpvModule.Reflection.Type type() {
                        return typeHandle;
                    }

                    @Override
                    public int location() {
                        return spirv.get(locationOffset);
                    }

                    @Override
                    public void location(int location) {
                        spirv.put(locationOffset, location);
                    }

                    @Override
                    public int decoration(int decoration) {
                        return Spvc.spvc_compiler_get_decoration((long)compiler, (int)resource.id(), (int)decoration);
                    }
                });
                ++i;
            }
        }
        return ReferenceLists.unmodifiable((ReferenceList)list);
    }

    private static List<SpvModule.Reflection.Descriptor> generateDescriptorList(long compiler, long spvcResources, final IntBuffer spirv, final int resourceType) throws ShaderCompileException {
        ReferenceArrayList list = new ReferenceArrayList();
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer pointerReturnBuffer = stack.callocPointer(1);
            IntBuffer intReturnBuffer = stack.callocInt(1);
            PointerBuffer countPointer = stack.callocPointer(1);
            SpvUtil.throwIfError(Spvc.spvc_resources_get_resource_list_for_type((long)spvcResources, (int)resourceType, (PointerBuffer)pointerReturnBuffer, (PointerBuffer)countPointer), "Couldn't list input variables");
            long spvcList = pointerReturnBuffer.get(0);
            long spvcCount = countPointer.get(0);
            SpvcReflectedResource.Buffer resources = SpvcReflectedResource.create((long)spvcList, (int)((int)spvcCount));
            int i = 0;
            while ((long)i < spvcCount) {
                SpvcReflectedResource resource = (SpvcReflectedResource)resources.get(i);
                final String name = resource.nameString();
                if (!Spvc.spvc_compiler_get_binary_offset_for_decoration((long)compiler, (int)resource.id(), (int)34, (IntBuffer)intReturnBuffer)) {
                    throw new ShaderCompileException("Couldn't find byte offset for set decoration of " + name);
                }
                final int setOffset = intReturnBuffer.get(0);
                if (!Spvc.spvc_compiler_get_binary_offset_for_decoration((long)compiler, (int)resource.id(), (int)33, (IntBuffer)intReturnBuffer)) {
                    throw new ShaderCompileException("Couldn't find byte offset for binding decoration of " + name);
                }
                final TypeHandle typeHandle = new TypeHandle(Spvc.spvc_compiler_get_type_handle((long)compiler, (int)resource.type_id()));
                final int bindingOffset = intReturnBuffer.get(0);
                list.add((Object)new SpvModule.Reflection.Descriptor(){

                    @Override
                    public String name() {
                        return name;
                    }

                    @Override
                    public SpvModule.Reflection.Type type() {
                        return typeHandle;
                    }

                    @Override
                    public int resourceType() {
                        return resourceType;
                    }

                    @Override
                    public int descriptorSetIndex() {
                        return spirv.get(setOffset);
                    }

                    @Override
                    public void descriptorSetIndex(int index) {
                        spirv.put(setOffset, index);
                    }

                    @Override
                    public int binding() {
                        return spirv.get(bindingOffset);
                    }

                    @Override
                    public void binding(int binding) {
                        spirv.put(bindingOffset, binding);
                    }
                });
                ++i;
            }
        }
        return ReferenceLists.unmodifiable((ReferenceList)list);
    }

    private static List<SpvModule.Reflection.PushConstant> generatePushConstantList(long compiler, long spvcResources) throws ShaderCompileException {
        ReferenceArrayList list = new ReferenceArrayList();
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer pointerReturnBuffer = stack.callocPointer(1);
            PointerBuffer countPointer = stack.callocPointer(1);
            SpvUtil.throwIfError(Spvc.spvc_resources_get_resource_list_for_type((long)spvcResources, (int)9, (PointerBuffer)pointerReturnBuffer, (PointerBuffer)countPointer), "Couldn't list input variables");
            long spvcList = pointerReturnBuffer.get(0);
            long spvcCount = countPointer.get(0);
            SpvcReflectedResource.Buffer resources = SpvcReflectedResource.create((long)spvcList, (int)((int)spvcCount));
            int i = 0;
            while ((long)i < spvcCount) {
                SpvcReflectedResource resource = (SpvcReflectedResource)resources.get(i);
                TypeHandle typeHandle = new TypeHandle(Spvc.spvc_compiler_get_type_handle((long)compiler, (int)resource.type_id()));
                Spvc.spvc_compiler_get_declared_struct_size((long)compiler, (long)typeHandle.typeHandle, (PointerBuffer)pointerReturnBuffer);
                int structSize = Math.toIntExact(pointerReturnBuffer.get(0));
                list.add(() -> structSize);
                ++i;
            }
        }
        return ReferenceLists.unmodifiable((ReferenceList)list);
    }

    private record TypeHandle(long typeHandle) implements SpvModule.Reflection.Type
    {
        @Override
        public int baseType() {
            return Spvc.spvc_type_get_basetype((long)this.typeHandle);
        }

        @Override
        public int dimensions() {
            int baseType = this.baseType();
            if (baseType != 16 && baseType != 17) {
                return Integer.MAX_VALUE;
            }
            return Spvc.spvc_type_get_image_dimension((long)this.typeHandle);
        }

        @Override
        public int vectorSize() {
            return Spvc.spvc_type_get_vector_size((long)this.typeHandle);
        }

        @Override
        public int arrayDimensions() {
            return Spvc.spvc_type_get_num_array_dimensions((long)this.typeHandle);
        }

        @Override
        public int arrayLength(int dimensionIndex) {
            return Spvc.spvc_type_get_array_dimension((long)this.typeHandle, (int)dimensionIndex);
        }
    }
}

