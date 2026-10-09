/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootContextUser;

public class ValidationContext {
    private final ProblemReporter reporter;
    private final ContextKeySet contextKeySet;
    private final Optional<HolderGetter.Provider> resolver;
    private final Set<Object> visitedObjects;

    public ValidationContext(ProblemReporter reporter, ContextKeySet contextKeySet, HolderGetter.Provider resolver) {
        this(reporter, contextKeySet, Optional.of(resolver), Set.of());
    }

    public ValidationContext(ProblemReporter reporter, ContextKeySet contextKeySet) {
        this(reporter, contextKeySet, Optional.empty(), Set.of());
    }

    private ValidationContext(ProblemReporter reporter, ContextKeySet contextKeySet, Optional<HolderGetter.Provider> resolver, Set<Object> visitedObjects) {
        this.reporter = reporter;
        this.contextKeySet = contextKeySet;
        this.resolver = resolver;
        this.visitedObjects = visitedObjects;
    }

    public ValidationContext forChild(ProblemReporter.PathElement subContext) {
        return new ValidationContext(this.reporter.forChild(subContext), this.contextKeySet, this.resolver, this.visitedObjects);
    }

    public ValidationContext forField(String name) {
        return this.forChild(new ProblemReporter.FieldPathElement(name));
    }

    public ValidationContext forIndexedField(String name, int index) {
        return this.forChild(new ProblemReporter.IndexedFieldPathElement(name, index));
    }

    public ValidationContext forMapField(String name, String key) {
        return this.forChild(new ProblemReporter.MapEntryPathElement(name, key));
    }

    private ValidationContext enterObject(ProblemReporter.PathElement subContext, Object object) {
        ImmutableSet newVisitedElements = ImmutableSet.builder().addAll(this.visitedObjects).add(object).build();
        return new ValidationContext(this.reporter.forChild(subContext), this.contextKeySet, this.resolver, (Set<Object>)newVisitedElements);
    }

    public ValidationContext enterElement(ProblemReporter.PathElement subContext, ResourceKey<?> element) {
        return this.enterObject(subContext, element);
    }

    public boolean hasVisitedElement(ResourceKey<?> element) {
        return this.visitedObjects.contains(element);
    }

    public ValidationContext enterTag(ProblemReporter.PathElement subContext, TagKey<?> tag) {
        return this.enterObject(subContext, tag);
    }

    public boolean hasVisitedTag(TagKey<?> tag) {
        return this.visitedObjects.contains(tag);
    }

    public void reportProblem(ProblemReporter.Problem description) {
        this.reporter.report(description);
    }

    public void validateContextUsage(LootContextUser lootContextUser) {
        Set<ContextKey<?>> allReferenced = lootContextUser.getReferencedContextParams();
        Sets.SetView notProvided = Sets.difference(allReferenced, this.contextKeySet.allowed());
        if (!notProvided.isEmpty()) {
            this.reporter.report(new ParametersNotProvidedProblem((Set<ContextKey<?>>)notProvided));
        }
    }

    public HolderGetter.Provider resolver() {
        return this.resolver.orElseThrow(() -> new UnsupportedOperationException("References not allowed"));
    }

    public ProblemReporter reporter() {
        return this.reporter;
    }

    public record ParametersNotProvidedProblem(Set<ContextKey<?>> notProvided) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Parameters " + String.valueOf(this.notProvided) + " are not provided in this context";
        }
    }

    public record MinBoundsProblem(int minBounds) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "List must contain at least " + this.minBounds + " element(s)";
        }
    }

    public record RecursiveTagReferenceProblem(TagKey<?> referenced) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "#" + String.valueOf(this.referenced.location()) + " of type " + String.valueOf(this.referenced.registry()) + " is recursively called";
        }

        @Override
        public boolean isFatal() {
            return true;
        }
    }

    public record RecursiveElementReferenceProblem(ResourceKey<?> referenced) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return String.valueOf(this.referenced.identifier()) + " of type " + String.valueOf(this.referenced.registry()) + " is recursively called";
        }

        @Override
        public boolean isFatal() {
            return true;
        }
    }
}

