/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P4
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class UniformContainerBase
extends LootPoolEntryContainer {
    public static final int DEFAULT_WEIGHT = 1;
    public static final int DEFAULT_QUALITY = 0;
    protected final int weight;
    protected final int quality;

    protected UniformContainerBase(int weight, int quality, Optional<Holder<LootItemCondition>> condition, Optional<Holder<LootItemFunction>> modifier) {
        super(condition, modifier);
        this.weight = weight;
        this.quality = quality;
    }

    public abstract MapCodec<? extends UniformContainerBase> codec();

    protected static <T extends UniformContainerBase> Products.P4<RecordCodecBuilder.Mu<T>, Integer, Integer, Optional<Holder<LootItemCondition>>, Optional<Holder<LootItemFunction>>> uniformFields(RecordCodecBuilder.Instance<T> i) {
        return i.group((App)Codec.INT.optionalFieldOf("weight", (Object)1).forGetter(e -> e.weight), (App)Codec.INT.optionalFieldOf("quality", (Object)0).forGetter(e -> e.quality)).and(UniformContainerBase.commonFields(i).t1()).and((App)LootItemFunctions.CODEC.optionalFieldOf("modifier").forGetter(e -> e.modifier));
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        Validatable.validateHolder(context, "modifier", this.modifier);
    }

    public static Builder<?> simpleBuilder(EntryConstructor constructor) {
        return new DummyBuilder(constructor);
    }

    private static class DummyBuilder
    extends Builder<DummyBuilder> {
        private final EntryConstructor constructor;

        public DummyBuilder(EntryConstructor constructor) {
            this.constructor = constructor;
        }

        @Override
        protected DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return this.constructor.build(this.weight, this.quality, this.getCondition(), this.getModifier());
        }
    }

    @FunctionalInterface
    public static interface EntryConstructor {
        public UniformContainerBase build(int var1, int var2, Optional<Holder<LootItemCondition>> var3, Optional<Holder<LootItemFunction>> var4);
    }

    protected abstract class EntryBase
    implements LootPoolEntry {
        final /* synthetic */ UniformContainerBase this$0;

        protected EntryBase(UniformContainerBase this$0) {
            UniformContainerBase uniformContainerBase = this$0;
            Objects.requireNonNull(uniformContainerBase);
            this.this$0 = uniformContainerBase;
        }

        @Override
        public int getWeight(float luck) {
            return Math.max(Mth.floor((float)this.this$0.weight + (float)this.this$0.quality * luck), 0);
        }
    }

    public static abstract class Builder<T extends Builder<T>>
    extends LootPoolEntryContainer.Builder<T>
    implements FunctionUserBuilder<T> {
        protected int weight = 1;
        protected int quality = 0;

        public T setWeight(int weight) {
            this.weight = weight;
            return (T)((Builder)this.getThis());
        }

        public T setQuality(int quality) {
            this.quality = quality;
            return (T)((Builder)this.getThis());
        }
    }
}

