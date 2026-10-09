/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class CommandResponseTracker<Element> {
    private int totalValue;
    private @Nullable Element onlyElement;
    private int elementCount;
    private @Nullable Element onlyNonZeroElement;
    private int nonZeroElementCount;

    public static <T> CommandResponseTracker<T> create() {
        return new CommandResponseTracker();
    }

    public void track(Element element, int value) {
        this.totalValue += value;
        this.onlyElement = ++this.elementCount == 1 ? element : null;
        if (value != 0) {
            this.onlyNonZeroElement = ++this.nonZeroElementCount == 1 ? element : null;
        }
    }

    public void track(Element element, boolean value) {
        this.track(element, value ? 1 : 0);
    }

    public void track(Element element) {
        this.track(element, 1);
    }

    private @Nullable Element firstElement(ElementType type) {
        return switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.onlyElement;
            case 1 -> this.onlyNonZeroElement;
        };
    }

    private int elementCount(ElementType type) {
        return switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.elementCount;
            case 1 -> this.nonZeroElementCount;
        };
    }

    public int totalValue() {
        return this.totalValue;
    }

    public static <Element> Messages<Element> messages(SingleHandler<Component, Element> onSingle, MultipleHandler<Component> onMultiple) {
        return new Messages<Element>(null, new Dispatch<Component, Element>(onSingle, onMultiple));
    }

    public static <Element> Messages<Element> messages(ErrorHandler onZero, SingleHandler<Component, Element> onSingle, MultipleHandler<Component> onMultiple) {
        return new Messages<Element>(onZero, new Dispatch<Component, Element>(onSingle, onMultiple));
    }

    public static <Element> Messages<Element> messages(SimpleCommandExceptionType onZero, SingleHandler<Component, Element> onSingle, MultipleHandler<Component> onMultiple) {
        return new Messages<Element>(() -> ((SimpleCommandExceptionType)onZero).create(), new Dispatch<Component, Element>(onSingle, onMultiple));
    }

    public <Result> Result dispatch(ElementType elementType, Dispatch<Result, ? super Element> dispatch) {
        Element firstElement = this.firstElement(elementType);
        return firstElement != null ? dispatch.onSingle().create(firstElement, this.totalValue) : dispatch.onMultiple().create(this.elementCount(elementType), this.totalValue);
    }

    public int sendFeedback(CommandSourceStack sourceStack, boolean broadcast, ElementType elementType, Messages<? super Element> messages) throws CommandSyntaxException {
        messages.throwIfZero(this.elementCount(elementType));
        sourceStack.sendSuccess(() -> this.dispatch(elementType, messages.onSuccess()), broadcast);
        return this.totalValue;
    }

    public int sendFeedback(CommandSourceStack sourceStack, boolean broadcast, Messages<? super Element> messages) throws CommandSyntaxException {
        return this.sendFeedback(sourceStack, broadcast, ElementType.NON_ZERO, messages);
    }

    public static <Element, Arg> MessagesWithArg<Element, Arg> messages(SingleHandlerWithArg<Component, Element, Arg> onSingle, MultipleHandlerWithArg<Component, Arg> onMultiple) {
        return new MessagesWithArg<Element, Arg>(null, new DispatchWithArg<Component, Element, Arg>(onSingle, onMultiple));
    }

    public static <Element, Arg> MessagesWithArg<Element, Arg> messages(ErrorHandlerWithArg<Arg> onZero, SingleHandlerWithArg<Component, Element, Arg> onSingle, MultipleHandlerWithArg<Component, Arg> onMultiple) {
        return new MessagesWithArg<Element, Arg>(onZero, new DispatchWithArg<Component, Element, Arg>(onSingle, onMultiple));
    }

    public static <Element, Arg> MessagesWithArg<Element, Arg> messages(SimpleCommandExceptionType onZero, SingleHandlerWithArg<Component, Element, Arg> onSingle, MultipleHandlerWithArg<Component, Arg> onMultiple) {
        return new MessagesWithArg<Element, Object>(object -> onZero.create(), new DispatchWithArg<Component, Element, Arg>(onSingle, onMultiple));
    }

    public <Result, Arg> Result dispatch(ElementType elementType, DispatchWithArg<Result, ? super Element, Arg> dispatch, Arg argument) {
        Element firstElement = this.firstElement(elementType);
        return firstElement != null ? dispatch.onSingle().create(firstElement, this.totalValue, argument) : dispatch.onMultiple().create(this.elementCount(elementType), this.totalValue, argument);
    }

    public <Arg> int sendFeedback(CommandSourceStack sourceStack, boolean broadcast, ElementType elementType, MessagesWithArg<? super Element, Arg> messages, Arg argument) throws CommandSyntaxException {
        messages.throwIfZero(this.elementCount(elementType), argument);
        sourceStack.sendSuccess(() -> this.dispatch(elementType, messages.onSuccess(), argument), broadcast);
        return this.totalValue;
    }

    public <Arg> int sendFeedback(CommandSourceStack sourceStack, boolean broadcast, MessagesWithArg<? super Element, Arg> messages, Arg argument) throws CommandSyntaxException {
        return this.sendFeedback(sourceStack, broadcast, ElementType.NON_ZERO, messages, argument);
    }

    public static <Element, Arg0, Arg1> MessagesWithArgs<Element, Arg0, Arg1> messages(SingleHandlerWithArgs<Component, Element, Arg0, Arg1> onSingle, MultipleHandlerWithArgs<Component, Arg0, Arg1> onMultiple) {
        return new MessagesWithArgs<Element, Arg0, Arg1>(null, new DispatchWithArgs<Component, Element, Arg0, Arg1>(onSingle, onMultiple));
    }

    public static <Element, Arg0, Arg1> MessagesWithArgs<Element, Arg0, Arg1> messages(ErrorHandlerWithArgs<Arg0, Arg1> onZero, SingleHandlerWithArgs<Component, Element, Arg0, Arg1> onSingle, MultipleHandlerWithArgs<Component, Arg0, Arg1> onMultiple) {
        return new MessagesWithArgs<Element, Arg0, Arg1>(onZero, new DispatchWithArgs<Component, Element, Arg0, Arg1>(onSingle, onMultiple));
    }

    public static <Element, Arg0, Arg1> MessagesWithArgs<Element, Arg0, Arg1> messages(SimpleCommandExceptionType onZero, SingleHandlerWithArgs<Component, Element, Arg0, Arg1> onSingle, MultipleHandlerWithArgs<Component, Arg0, Arg1> onMultiple) {
        return new MessagesWithArgs<Element, Object, Object>((object, object2) -> onZero.create(), new DispatchWithArgs<Component, Element, Arg0, Arg1>(onSingle, onMultiple));
    }

    public <Result, Arg0, Arg1> Result dispatch(ElementType elementType, DispatchWithArgs<Result, ? super Element, Arg0, Arg1> messages, Arg0 argument0, Arg1 argument1) {
        Element firstElement = this.firstElement(elementType);
        return firstElement != null ? messages.onSingle().create(firstElement, this.totalValue, argument0, argument1) : messages.onMultiple().create(this.elementCount(elementType), this.totalValue, argument0, argument1);
    }

    public <Arg0, Arg1> int sendFeedback(CommandSourceStack sourceStack, boolean broadcast, ElementType elementType, MessagesWithArgs<? super Element, Arg0, Arg1> messages, Arg0 argument0, Arg1 argument1) throws CommandSyntaxException {
        messages.throwIfZero(this.elementCount(elementType), argument0, argument1);
        sourceStack.sendSuccess(() -> this.dispatch(elementType, messages.onSuccess(), argument0, argument1), broadcast);
        return this.totalValue;
    }

    public <Arg0, Arg1> int sendFeedback(CommandSourceStack sourceStack, boolean broadcast, MessagesWithArgs<? super Element, Arg0, Arg1> messages, Arg0 argument0, Arg1 argument1) throws CommandSyntaxException {
        return this.sendFeedback(sourceStack, broadcast, ElementType.NON_ZERO, messages, argument0, argument1);
    }

    public static enum ElementType {
        ANY,
        NON_ZERO;

    }

    public record Messages<Element>(@Nullable ErrorHandler onZero, Dispatch<Component, Element> onSuccess) {
        public void throwIfZero(int value) throws CommandSyntaxException {
            if (this.onZero != null && value == 0) {
                throw this.onZero.get();
            }
        }
    }

    public record Dispatch<Result, Element>(SingleHandler<Result, Element> onSingle, MultipleHandler<Result> onMultiple) {
    }

    @FunctionalInterface
    public static interface SingleHandler<Result, Element> {
        public Result create(Element var1, int var2);
    }

    @FunctionalInterface
    public static interface MultipleHandler<Result> {
        public Result create(int var1, int var2);
    }

    @FunctionalInterface
    public static interface ErrorHandler {
        public CommandSyntaxException get();
    }

    public record MessagesWithArg<Element, Arg>(@Nullable ErrorHandlerWithArg<Arg> onZero, DispatchWithArg<Component, Element, Arg> onSuccess) {
        public void throwIfZero(int value, Arg argument) throws CommandSyntaxException {
            if (this.onZero != null && value == 0) {
                throw this.onZero.get(argument);
            }
        }
    }

    public record DispatchWithArg<Result, Element, Arg>(SingleHandlerWithArg<Result, Element, Arg> onSingle, MultipleHandlerWithArg<Result, Arg> onMultiple) {
    }

    @FunctionalInterface
    public static interface SingleHandlerWithArg<Result, Element, Arg> {
        public Result create(Element var1, int var2, Arg var3);
    }

    @FunctionalInterface
    public static interface MultipleHandlerWithArg<Result, Arg> {
        public Result create(int var1, int var2, Arg var3);
    }

    @FunctionalInterface
    public static interface ErrorHandlerWithArg<Arg> {
        public CommandSyntaxException get(Arg var1);
    }

    public record MessagesWithArgs<Element, Arg0, Arg1>(@Nullable ErrorHandlerWithArgs<Arg0, Arg1> onZero, DispatchWithArgs<Component, Element, Arg0, Arg1> onSuccess) {
        public void throwIfZero(int value, Arg0 argument0, Arg1 argument1) throws CommandSyntaxException {
            if (this.onZero != null && value == 0) {
                throw this.onZero.get(argument0, argument1);
            }
        }
    }

    public record DispatchWithArgs<Result, Element, Arg0, Arg1>(SingleHandlerWithArgs<Result, Element, Arg0, Arg1> onSingle, MultipleHandlerWithArgs<Result, Arg0, Arg1> onMultiple) {
    }

    @FunctionalInterface
    public static interface SingleHandlerWithArgs<Result, Element, Arg0, Arg1> {
        public Result create(Element var1, int var2, Arg0 var3, Arg1 var4);
    }

    @FunctionalInterface
    public static interface MultipleHandlerWithArgs<Result, Arg0, Arg1> {
        public Result create(int var1, int var2, Arg0 var3, Arg1 var4);
    }

    @FunctionalInterface
    public static interface ErrorHandlerWithArgs<Arg0, Arg1> {
        public CommandSyntaxException get(Arg0 var1, Arg1 var2);
    }
}

