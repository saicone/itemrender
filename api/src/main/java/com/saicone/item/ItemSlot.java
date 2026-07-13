package com.saicone.item;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public interface ItemSlot {

    @NotNull
    static <T> Any<T> any(@NotNull T any) {
        return new Any<>(any);
    }

    @NotNull
    static ItemSlot integer(int i) {
        if (i <= Cache.HIGH && i >= Cache.LOW) {
            return Cache.INTEGER[i - Cache.LOW];
        }
        return new Any<>(i);
    }

    @NotNull
    static <A, B> Pair<A, B> pair(@NotNull A anyA, @NotNull B anyB) {
        return new Pair<>(anyA, anyB);
    }

    @NotNull
    static IntRange range(int from, int to) {
        return new IntRange(from, to);
    }

    @NotNull
    static Compose compose(@NotNull ItemSlot... slots) {
        return new Compose(slots);
    }

    boolean matches(Object object);

    class Cache {

        private static final int HIGH = 1024;
        private static final int LOW = -128;

        private static final ItemSlot[] INTEGER = new ItemSlot[HIGH + Math.abs(LOW) + 1];

        static {
            for (int i = 0; i < INTEGER.length; i++) {
                INTEGER[i] = new Any<>(i + LOW);
            }
        }
    }

    class Any<T> implements ItemSlot {

        protected final T any;

        public Any(@NotNull T any) {
            this.any = any;
        }

        @NotNull
        public T value() {
            return any;
        }

        @Override
        public boolean matches(Object object) {
            if (object instanceof Any<?>) {
                return Objects.equals(this.any, ((Any<?>) object).any);
            }
            return any.equals(object);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Any)) return false;

            Any<?> any1 = (Any<?>) object;
            return any.equals(any1.any);
        }

        @Override
        public int hashCode() {
            return any.hashCode();
        }
    }

    class Pair<A, B> extends Any<A> {

        private final B anyB;

        public Pair(@NotNull A anyA, @NotNull B anyB) {
            super(anyA);
            this.anyB = anyB;
        }

        @NotNull
        public B valueB() {
            return anyB;
        }

        @Override
        public boolean matches(Object object) {
            if (object instanceof Pair<?,?>) {
                return Objects.equals(this.any, ((Pair<?, ?>) object).any) || Objects.equals(this.anyB, ((Pair<?, ?>) object).anyB);
            }
            return super.matches(object);
        }

        @Override
        public final boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Pair)) return false;
            if (!super.equals(object)) return false;

            Pair<?, ?> pair = (Pair<?, ?>) object;
            return anyB.equals(pair.anyB);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + anyB.hashCode();
            return result;
        }
    }

    class IntRange extends Pair<Integer, Integer> {

        public IntRange(@NotNull Integer from, @NotNull Integer to) {
            super(from, to);
        }

        @Override
        public boolean matches(Object object) {
            if (object instanceof Pair<?, ?>) {
                if (((Pair<?, ?>) object).valueB() instanceof Number) {
                    if (matches(((Number) ((Pair<?, ?>) object).valueB()).intValue())) {
                        return true;
                    }
                }
                if (((Pair<?, ?>) object).value() instanceof Number) {
                    return matches(((Number) ((Pair<?, ?>) object).value()).intValue());
                }
            } else if (object instanceof Any<?>) {
                if (((Any<?>) object).value() instanceof Number) {
                    return matches(((Number) ((Any<?>) object).value()).intValue());
                }
            } else if (object instanceof Number) {
                return matches(((Number) object).intValue());
            }
            return false;
        }

        public boolean matches(int number) {
            return number >= value() && number <= valueB();
        }
    }

    class Compose implements ItemSlot {

        private final ItemSlot[] slots;

        @ApiStatus.Internal
        public Compose(@NotNull ItemSlot[] slots) {
            this.slots = slots;
        }

        @NotNull
        @ApiStatus.Internal
        public ItemSlot[] slots() {
            return slots;
        }

        @Override
        public boolean matches(Object object) {
            for (ItemSlot slot : slots) {
                if (slot.matches(object)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public final boolean equals(Object object) {
            if (this == object) return true;
            if (!(object instanceof Compose)) return false;

            Compose compose = (Compose) object;
            return Arrays.equals(slots, compose.slots);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(slots);
        }
    }

    class Window {
        public static final ItemSlot CURSOR = integer(-1);
    }

    enum Equipment implements ItemSlot {

        MAINHAND("HAND"),
        OFFHAND,
        FEET,
        LEGS,
        CHEST,
        HEAD;

        public static final Equipment[] VALUES = values();
        public static final ItemSlot HAND = compose(MAINHAND, OFFHAND);
        public static final ItemSlot ARMOR = compose(FEET, LEGS, CHEST, HEAD);

        private final Set<String> aliases = new HashSet<>();

        Equipment(@NotNull String... aliases) {
            this.aliases.add(name());
            Collections.addAll(this.aliases, aliases);
        }

        @Override
        public boolean matches(Object object) {
            if (object instanceof Enum<?>) {
                return ((Enum<?>) object).ordinal() == ordinal() || aliases.contains(((Enum<?>) object).name().replace('_', '\0').toUpperCase());
            } else if (object instanceof Number) {
                return ((Number) object).intValue() == ordinal();
            }
            return false;
        }

        @NotNull
        public static <E extends Enum<E>> Equipment of(@NotNull E e) {
            return VALUES[e.ordinal()];
        }
    }

    class Merchant {
        public static final ItemSlot COST_A = pair("cost", "A");
        public static final ItemSlot COST_B = pair("cost", "B");
        public static final ItemSlot RESULT = any("result");
    }
}
