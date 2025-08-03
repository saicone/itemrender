package com.saicone.item.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface LabeledList<E> extends List<E> {

    @Override
    default void addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    void addFirst(@NotNull String key, E e);

    @Override
    default void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    void addLast(@NotNull String key, E e);

    void addBefore(@NotNull String existingKey, @NotNull String key, E e);

    void addAfter(@NotNull String existingKey, @NotNull String key, E e);

    @Override
    default boolean add(E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    boolean contains(@NotNull String key, Object o);

    E getValue(@NotNull String key);

    @Override
    default E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    E set(int index, @NotNull String key, E element);

    @Override
    default void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    void add(int index, @NotNull String key, E element);

    @NotNull
    String getKey(int index);

    boolean removeKey(@NotNull String key);

    int indexOf(@NotNull String key, Object o);

    int lastIndexOf(@NotNull String key, Object o);

    @NotNull
    List<String> labels();

}
