package com.saicone.item.util;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public class LabeledSpliterator<E> implements Spliterator<E> {

    private final Spliterator<Map.Entry<String, E>> spliterator;

    public LabeledSpliterator(@NotNull Spliterator<Map.Entry<String, E>> spliterator) {
        this.spliterator = spliterator;
    }


    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        return spliterator.tryAdvance((entry) -> action.accept(entry.getValue()));
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        spliterator.forEachRemaining((entry) -> action.accept(entry.getValue()));
    }

    @Override
    public Spliterator<E> trySplit() {
        return new LabeledSpliterator<>(spliterator.trySplit());
    }

    @Override
    public long estimateSize() {
        return spliterator.estimateSize();
    }

    @Override
    public long getExactSizeIfKnown() {
        return spliterator.getExactSizeIfKnown();
    }

    @Override
    public int characteristics() {
        return spliterator.characteristics();
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        return spliterator.hasCharacteristics(characteristics);
    }

    @Override
    public Comparator<? super E> getComparator() {
        throw new UnsupportedOperationException();
    }
}
