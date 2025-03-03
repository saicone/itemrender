package com.saicone.item.util;

import org.jetbrains.annotations.NotNull;

import java.util.Deque;

public interface LabeledDeque<E> extends Deque<E> {

    @Override
    default boolean offer(E e) {
        throw new UnsupportedOperationException();
    }

    boolean offer(@NotNull String key, E e);

    @Override
    default boolean offerFirst(E e) {
        throw new UnsupportedOperationException();
    }

    boolean offerFirst(@NotNull String key, E e);

    @Override
    default boolean offerLast(E e) {
        throw new UnsupportedOperationException();
    }

    boolean offerLast(@NotNull String key, E e);

    @Override
    default void push(E e) {
        throw new UnsupportedOperationException();
    }

    void push(@NotNull String key, E e);
}
