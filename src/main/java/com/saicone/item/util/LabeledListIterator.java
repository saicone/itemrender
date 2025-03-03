package com.saicone.item.util;

import org.jetbrains.annotations.NotNull;

import java.util.ListIterator;
import java.util.Map;

public class LabeledListIterator<E> implements ListIterator<E> {

    private final ListIterator<Map.Entry<String, E>> iterator;

    public LabeledListIterator(@NotNull ListIterator<Map.Entry<String, E>> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public E next() {
        return iterator.next().getValue();
    }

    @Override
    public boolean hasPrevious() {
        return iterator.hasPrevious();
    }

    @Override
    public E previous() {
        return iterator.previous().getValue();
    }

    @Override
    public int nextIndex() {
        return iterator.nextIndex();
    }

    @Override
    public int previousIndex() {
        return iterator.previousIndex();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public void set(E e) {
        throw new UnsupportedOperationException();
    }

    public void set(@NotNull String key, E e) {
        iterator.set(Map.entry(key, e));
    }

    @Override
    public void add(E e) {
        throw new UnsupportedOperationException();
    }

    public void add(@NotNull String key, E e) {
        iterator.set(Map.entry(key, e));
    }
}
