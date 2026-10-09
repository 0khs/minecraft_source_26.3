/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.world.level.storage.loot;

import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.ValidationContext;

public interface Validatable {
    public void validate(ValidationContext var1);

    public static void validate(ValidationContext context, String name, Validatable v) {
        v.validate(context.forField(name));
    }

    public static void validate(ValidationContext context, String name, Optional<? extends Validatable> optional) {
        optional.ifPresent(v -> v.validate(context.forField(name)));
    }

    public static void validateHolder(ValidationContext context, Holder<? extends Validatable> holder) {
        ValidationContext elementContext;
        if (holder instanceof Holder.Reference) {
            Holder.Reference reference = (Holder.Reference)holder;
            ResourceKey id = reference.key();
            if (context.hasVisitedElement(id)) {
                context.reportProblem(new ValidationContext.RecursiveElementReferenceProblem(id));
                return;
            }
            elementContext = context.enterElement(new ProblemReporter.ElementReferencePathElement(id), id);
        } else {
            elementContext = context;
        }
        holder.value().validate(elementContext);
    }

    public static void validateHolder(ValidationContext context, String name, Holder<? extends Validatable> holder) {
        Validatable.validateHolder(context.forField(name), holder);
    }

    public static void validateHolder(ValidationContext context, String name, Optional<? extends Holder<? extends Validatable>> optional) {
        optional.ifPresent(v -> Validatable.validateHolder(context, name, v));
    }

    public static void validateHolder(ValidationContext context, String name, List<? extends Holder<? extends Validatable>> list) {
        for (int i = 0; i < list.size(); ++i) {
            list.get(i).value().validate(context.forIndexedField(name, i));
        }
    }

    public static void validateHolderSet(ValidationContext context, String name, HolderSet<? extends Validatable> holderSet, int minSize) {
        ValidationContext namedContext = context.forField(name);
        Validatable.validateHolderSet(namedContext, holderSet);
        if (holderSet.size() < minSize) {
            namedContext.reportProblem(new ValidationContext.MinBoundsProblem(minSize));
        }
    }

    public static void validateHolderSet(ValidationContext context, String name, HolderSet<? extends Validatable> holderSet) {
        Validatable.validateHolderSet(context.forField(name), holderSet);
    }

    private static void validateHolderSet(ValidationContext context, HolderSet<? extends Validatable> holderSet) {
        ValidationContext collectionContext;
        if (!holderSet.isBound()) {
            return;
        }
        if (holderSet instanceof HolderSet.Named) {
            HolderSet.Named reference = (HolderSet.Named)holderSet;
            TagKey id = reference.key();
            if (context.hasVisitedTag(id)) {
                context.reportProblem(new ValidationContext.RecursiveTagReferenceProblem(id));
                return;
            }
            collectionContext = context.enterTag(new ProblemReporter.CollectionReferencePathElement(id), id);
        } else {
            collectionContext = context;
        }
        for (int i = 0; i < holderSet.size(); ++i) {
            Validatable.validateHolder(collectionContext.forChild(new ProblemReporter.IndexedPathElement(i)), holderSet.get(i));
        }
    }

    public static void validate(ValidationContext context, String name, List<? extends Validatable> list) {
        for (int i = 0; i < list.size(); ++i) {
            list.get(i).validate(context.forIndexedField(name, i));
        }
    }

    public static void validate(ValidationContext context, List<? extends Validatable> list) {
        for (int i = 0; i < list.size(); ++i) {
            list.get(i).validate(context.forChild(new ProblemReporter.IndexedPathElement(i)));
        }
    }

    public static <T extends Validatable> Function<T, DataResult<T>> validatorForContext(ContextKeySet params) {
        return v -> {
            ProblemReporter.Collector problemCollector = new ProblemReporter.Collector();
            ValidationContext validationContext = new ValidationContext(problemCollector, params);
            v.validate(validationContext);
            if (!problemCollector.isEmpty()) {
                return DataResult.error(() -> "Validation error: " + problemCollector.getReport());
            }
            return DataResult.success((Object)v);
        };
    }

    public static <T extends Validatable> Function<List<T>, DataResult<List<T>>> listValidatorForContext(ContextKeySet params) {
        return v -> {
            ProblemReporter.Collector problemCollector = new ProblemReporter.Collector();
            ValidationContext validationContext = new ValidationContext(problemCollector, params);
            Validatable.validate(validationContext, v);
            if (!problemCollector.isEmpty()) {
                return DataResult.error(() -> "Validation error: " + problemCollector.getReport());
            }
            return DataResult.success((Object)v);
        };
    }
}

