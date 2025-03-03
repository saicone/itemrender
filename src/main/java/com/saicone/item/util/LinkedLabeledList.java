package com.saicone.item.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;

public class LinkedLabeledList<E> extends AbstractSequentialList<E> implements LabeledList<E>, LabeledDeque<E> {

    private final LinkedList<Map.Entry<String, E>> list;

    public LinkedLabeledList() {
        this(new LinkedList<>());
    }

    public LinkedLabeledList(@NotNull LinkedList<Map.Entry<String, E>> list) {
        this.list = list;
    }

    @Override
    public E getFirst() {
        return list.getFirst().getValue();
    }

    @Override
    public E getLast() {
        return list.getLast().getValue();
    }

    @Override
    public E removeFirst() {
        return list.removeFirst().getValue();
    }

    @Override
    public E removeLast() {
        return list.removeLast().getValue();
    }

    @Override
    public void addFirst(E e) {
        LabeledList.super.addFirst(e);
    }

    @Override
    public void addFirst(@NotNull String key, E e) {
        list.addFirst(Map.entry(key, e));
    }

    @Override
    public void addLast(E e) {
        LabeledList.super.addLast(e);
    }

    @Override
    public void addLast(@NotNull String key, E e) {
        list.addLast(Map.entry(key, e));
    }

    @Override
    public void addBefore(@NotNull String existingKey, @NotNull String key, E e) {
        final ListIterator<Map.Entry<String, E>> iterator = list.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().getKey().equals(existingKey)) {
                iterator.previous();
                iterator.add(Map.entry(key, e));
                return;
            }
        }
        throw new NoSuchElementException("Key not found: " + existingKey);
    }

    @Override
    public void addAfter(@NotNull String existingKey, @NotNull String key, E e) {
        final ListIterator<Map.Entry<String, E>> iterator = list.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().getKey().equals(existingKey)) {
                iterator.add(Map.entry(key, e));
                return;
            }
        }
        throw new NoSuchElementException("Key not found: " + existingKey);
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Map.Entry<?,?>) {
            return list.contains(o);
        } else {
            for (Map.Entry<String, E> entry : list) {
                if (entry.getValue().equals(o) && o.equals(entry.getValue())) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean contains(@NotNull String key, Object o) {
        return list.contains(Map.entry(key, o));
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Map.Entry<?,?>) {
            return list.remove(o);
        } else if (o instanceof String) {
            return removeKey((String) o);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean removeKey(@NotNull String key) {
        return list.removeIf(entry -> entry.getKey().equals(key));
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public E get(int index) {
        return list.get(index).getValue();
    }

    @Override
    public E getValue(@NotNull String key) {
        for (Map.Entry<String, E> entry : list) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public E set(int index, @NotNull String key, E element) {
        return list.set(index, Map.entry(key, element)).getValue();
    }

    @Override
    public void add(int index, @NotNull String key, E element) {
        list.add(index, Map.entry(key, element));
    }

    @Override
    public E remove(int index) {
        return list.remove(index).getValue();
    }

    @Override
    public int indexOf(Object o) {
        if (o instanceof Map.Entry<?,?>) {
            return list.indexOf(o);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int indexOf(@NotNull String key, Object o) {
        return list.indexOf(Map.entry(key, o));
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o instanceof Map.Entry<?,?>) {
            return list.lastIndexOf(o);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int lastIndexOf(@NotNull String key, Object o) {
        return list.lastIndexOf(Map.entry(key, o));
    }

    @Override
    public @NotNull List<String> labels() {
        final List<String> list = new ArrayList<>();
        for (Map.Entry<String, E> entry : this.list) {
            list.add(entry.getKey());
        }
        return list;
    }

    @Override
    public E peek() {
        final Map.Entry<String, E> entry = list.peek();
        return entry == null ? null : entry.getValue();
    }

    @Override
    public E element() {
        return list.element().getValue();
    }

    @Override
    public E poll() {
        final Map.Entry<String, E> entry = list.poll();
        return entry == null ? null : entry.getValue();
    }

    @Override
    public E remove() {
        return list.remove().getValue();
    }

    @Override
    public boolean offer(@NotNull String key, E e) {
        return list.offer(Map.entry(key, e));
    }

    @Override
    public boolean offerFirst(@NotNull String key, E e) {
        return list.offerFirst(Map.entry(key, e));
    }

    @Override
    public boolean offerLast(@NotNull String key, E e) {
        return list.offerLast(Map.entry(key, e));
    }

    @Override
    public E peekFirst() {
        final Map.Entry<String, E> entry = list.peekFirst();
        return entry == null ? null : entry.getValue();
    }

    @Override
    public E peekLast() {
        final Map.Entry<String, E> entry = list.peekLast();
        return entry == null ? null : entry.getValue();
    }

    @Override
    public E pollFirst() {
        final Map.Entry<String, E> entry = list.pollFirst();
        return entry == null ? null : entry.getValue();
    }

    @Override
    public E pollLast() {
        final Map.Entry<String, E> entry = list.pollLast();
        return entry == null ? null : entry.getValue();
    }

    @Override
    public void push(@NotNull String key, E e) {
        list.push(Map.entry(key, e));
    }

    @Override
    public E pop() {
        return list.pop().getValue();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        if (o instanceof Map.Entry<?,?>) {
            return list.removeFirstOccurrence(o);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        if (o instanceof Map.Entry<?,?>) {
            return list.removeLastOccurrence(o);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new LabeledListIterator<>(list.listIterator(index));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new Iterator<>() {
            private final Iterator<Map.Entry<String, E>> iterator = list.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                return iterator.next().getValue();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        return new LinkedLabeledList<>((LinkedList<Map.Entry<String, E>>) list.clone());
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size()];
        int i = 0;
        for (Map.Entry<String, E> entry : list) {
            result[i++] = entry.getValue();
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size())
            a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size());
        int i = 0;
        Object[] result = a;
        for (Map.Entry<String, E> entry : list) {
            result[i++] = entry.getValue();
        }
        if (a.length > size()) {
            a[size()] = null;
        }
        return a;
    }

    @Override
    public Spliterator<E> spliterator() {
        return new LabeledSpliterator<>(list.spliterator());
    }

    public LinkedList<E> reversed() {
        throw new UnsupportedOperationException();
    }
}
