package com.saicone.item;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface ItemSlot {

    @NotNull
    static <T> Any<T> any(@NotNull T any) {
        return new Any<>(any);
    }

    @NotNull
    static <A, B> Pair<A, B> pair(@NotNull A anyA, @NotNull B anyB) {
        return new Pair<>(anyA, anyB);
    }

    boolean isValid(Object object);

    class Any<T> implements ItemSlot {

        protected final T any;

        public Any(@NotNull T any) {
            this.any = any;
        }

        @Override
        public boolean isValid(Object object) {
            if (object instanceof Any<?>) {
                return Objects.equals(this.any, ((Any<?>) object).any);
            }
            return any.equals(object);
        }
    }

    class Pair<A, B> extends Any<A> {

        private final B anyB;

        public Pair(@NotNull A anyA, @NotNull B anyB) {
            super(anyA);
            this.anyB = anyB;
        }

        @Override
        public boolean isValid(Object object) {
            if (object instanceof Pair<?,?>) {
                return Objects.equals(this.any, ((Pair<?, ?>) object).any) || Objects.equals(this.anyB, ((Pair<?, ?>) object).anyB);
            }
            return super.isValid(object);
        }
    }

    enum Equipment implements ItemSlot {

        MAINHAND,
        OFFHAND,
        FEET,
        LEGS,
        CHEST,
        HEAD;

        public static final Equipment[] HAND = new Equipment[] { MAINHAND, OFFHAND };
        public static final Equipment[] ARMOR = new Equipment[] { FEET, LEGS, CHEST, HEAD };

        @Override
        public boolean isValid(Object object) {
            if (object instanceof Enum<?>) {
                return ((Enum<?>) object).ordinal() == ordinal();
            } else if (object instanceof Number) {
                return ((Number) object).intValue() == ordinal();
            }
            return false;
        }
    }

    class Recipe {
        public static final ItemSlot COOKING_INGREDIENT = any("cooking:ingredient");
        public static final ItemSlot COOKING_RESULT = any("cooking:result");
        public static final ItemSlot[] COOKING = new ItemSlot[] { COOKING_INGREDIENT, COOKING_RESULT };

        public static final ItemSlot[] SHAPED_INGREDIENT = new ItemSlot[9];
        public static final ItemSlot SHAPED_RESULT = any("shaped:result");
        public static final ItemSlot[] SHAPED = new ItemSlot[] { any("shaped:ingredient"), SHAPED_RESULT };

        public static final ItemSlot[] SHAPELESS_INGREDIENT = new ItemSlot[9];
        public static final ItemSlot SHAPELESS_RESULT = any("shapeless:result");
        public static final ItemSlot[] SHAPELESS = new ItemSlot[] { any("shapeless:ingredient"), SHAPELESS_RESULT };

        public static final ItemSlot TRANSFORM_TEMPLATE = any("transform:template");
        public static final ItemSlot TRANSFORM_BASE = any("transform:base");
        public static final ItemSlot TRANSFORM_ADDITION = any("transform:addition");
        public static final ItemSlot TRANSFORM_RESULT = any("transform:result");
        public static final ItemSlot[] TRANSFORM = new ItemSlot[] { TRANSFORM_TEMPLATE, TRANSFORM_BASE, TRANSFORM_ADDITION, TRANSFORM_RESULT };

        public static final ItemSlot TRIM_TEMPLATE = any("trim:template");
        public static final ItemSlot TRIM_BASE = any("trim:base");
        public static final ItemSlot TRIM_ADDITION = any("trim:addition");
        public static final ItemSlot[] TRIM = new ItemSlot[] { TRIM_TEMPLATE, TRIM_BASE, TRIM_ADDITION };

        public static final ItemSlot STONECUTTER_INGREDIENT = any("stonecutter:ingredient");
        public static final ItemSlot STONECUTTER_RESULT = any("stonecutter:result");
        public static final ItemSlot[] STONECUTTER = new ItemSlot[] { STONECUTTER_INGREDIENT, STONECUTTER_RESULT };
        
        static {
            for (int i = 0; i < 9; i++) {
                SHAPED_INGREDIENT[i] = pair("shaped:ingredient", i);
                SHAPELESS_INGREDIENT[i] = pair("shapeless:ingredient", i);
            }
        }
    }

    class Merchant {
        public static final ItemSlot COST_A = pair("cost", "A");
        public static final ItemSlot COST_B = pair("cost", "B");
        public static final ItemSlot RESULT = any("result");
    }
}
